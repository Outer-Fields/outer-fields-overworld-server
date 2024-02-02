package io.mindspice.outerfieldsserver.entities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.mindspice.databaseservice.client.schema.PlayerFunds;
import io.mindspice.databaseservice.client.schema.Results;
import io.mindspice.outerfieldsserver.combat.enums.CardDomain;
import io.mindspice.outerfieldsserver.combat.gameroom.MatchInstance;
import io.mindspice.outerfieldsserver.components.player.PlayerNetOut;
import io.mindspice.outerfieldsserver.core.networking.proto.EntityProto;
import io.mindspice.outerfieldsserver.data.PlayerData;
import io.mindspice.outerfieldsserver.data.PawnSet;
import io.mindspice.outerfieldsserver.combat.schema.websocket.incoming.NetCombatAction;
import io.mindspice.outerfieldsserver.components.player.PlayerSession;
import io.mindspice.outerfieldsserver.core.Settings;
import io.mindspice.outerfieldsserver.data.OverWorldPawnState;
import io.mindspice.outerfieldsserver.enums.*;
import io.mindspice.outerfieldsserver.factory.ComponentFactory;
import io.mindspice.mindlib.data.geometry.IVector2;
import io.mindspice.mindlib.util.JsonUtils;
import io.mindspice.outerfieldsserver.util.AbuseFilters;
import io.mindspice.outerfieldsserver.util.Log;
import org.springframework.web.socket.WebSocketSession;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;


public class PlayerEntity extends CharacterEntity {
    private final int playerId;
    private final PlayerSession playerSession;
    protected volatile MatchInstance matchInstance;

    protected volatile boolean inCombat = false;
    private volatile boolean isPremium = false;
    private boolean fetchedPawnSets = false;

    /* Internal Info*/
    private volatile PlayerData playerData;
    private final boolean isBot = false;
    private final AbuseFilters abuseFilters = new AbuseFilters();

    /* OverWorld Pawns */
    public volatile List<OverWorldPawnState> overWorldPawnStates;

    public PlayerEntity(int entityId, int playerId, String playerName, List<EntityState> initStates,
            ClothingItem[] initOutfit, AreaId currArea, IVector2 currPosition, WebSocketSession webSocketSession) {
        super(entityId, EntityType.PLAYER, currArea, currPosition);
        super.name = playerName;
        this.playerId = playerId;
        factions.add(FactionType.PLAYER);
        ComponentFactory.CompSystem.attachPlayerEntityComponents(
                this, currPosition, currArea, initStates, initOutfit, webSocketSession
        );

        playerSession = ((PlayerNetOut) getComponent(ComponentType.PLAYER_NET_OUT)).playerSession();
    }

    public void onCombatResult() {

    }

    public int playerId() { return playerId; }

    public String toString() {
        JsonNode node = new JsonUtils.ObjectBuilder()
                .put("entityType", entityType)
                .put("entityId", id)
                .put("playerId", playerId)
                .put("name", name)
                .put("areaId", areaId)
                .put("chunkIndex", chunkIndex)
                .put("attachComponents", getAttachedComponentTypes())
                .put("listeningFor", listeningForTypes())
                .put("systemRegistry", systemRegistry != null ? systemRegistry.systemType() : null)
                .buildNode();
        try {
            return JsonUtils.writePretty(node);
        } catch (JsonProcessingException e) {
            return "Error serializing to string";
        }
    }

    public void updateOverWorldPawnStates(List<OverWorldPawnState> pawnStates) {
        this.overWorldPawnStates = pawnStates;
    }

    public PlayerSession playerSession() {
        return playerSession;
    }

    public boolean hasData() {
        return playerData != null;
    }

    public long getLastMsgTime() {
        return abuseFilters.lastMsgTime();
    }

    public void setFetchedPawnSets() {
        fetchedPawnSets = true;
    }

    public boolean haveFetchedPawnSets() {
        return fetchedPawnSets;
    }

    public void setLastMsgTime() {
        abuseFilters.setLastMsgTime(Instant.now().getEpochSecond());
    }

    public void setQueueCoolDown() {
        abuseFilters.setQueueCoolDownTime(Instant.now().getEpochSecond());
    }

    public boolean isQueueCoolDown() {

        return Instant.now().getEpochSecond() - abuseFilters.queueCoolDownTime() < Settings.GET().queueCoolDown;
    }

    public String getDid() {
        return playerData.getDid();
    }

    public PlayerSession getConnection() {
        return playerSession;
    }

    public double getWinRatio() {
        return (double) playerData.getHistoricalResults().wins() /
                playerData.getHistoricalResults().wins() + playerData.getHistoricalResults().losses();
    }

    // Requests and messages
    public void oncombatMessage(NetCombatAction msg) {
        if (abuseFilters.wsTimeout()) {
            playerSession.close();
            // TODO timeout player table?
            return;
        }
        if (inCombat) {
            matchInstance.addMsg(id, msg);
        } else {
            // if they are not in a game ignore their WS messages
            Log.SERVER.debug(this.getClass(), getLoggable() + " | Websocket message while not in game");
        }
        abuseFilters.setLastMsgTime(Instant.now().getEpochSecond());
    }

    public void sendJson(Object obj) {
        try {
            String jsonString = JsonUtils.writeString(obj);
            byte[] protoBytes = EntityProto.CombatJson.newBuilder()
                    .setJson(jsonString)
                    .build()
                    .toByteArray();

            playerSession.send(protoBytes);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendBytes(byte[] bytes) {
        playerSession.send(bytes);
    }

    public void setFullPlayerData(PlayerData playerData) {
        this.playerData = playerData;
        abuseFilters.setLastLargeReq(Instant.now().getEpochSecond());
    }

    public void setBasicPlayerData(List<String> ownedCards, PlayerFunds funds,
            Results dailyResults, Results historicalResults) {
        abuseFilters.setLastSmallReq(Instant.now().getEpochSecond());
        this.playerData = playerData.getUpdated(ownedCards, funds, dailyResults, historicalResults);
    }

    public PlayerData getPlayerData() {
        return playerData;
    }

    public String getBasicInfo() throws JsonProcessingException {
        return playerData.getBasicJson();
    }

    public String getFullInfo() throws JsonProcessingException {
        return playerData.getFullJson();
    }

    // Abuse Limits

    public boolean allowFullUpdate() {
        long now = Instant.now().getEpochSecond();
        if (now - abuseFilters.lastLargeReq() < 180) {
            Log.SERVER.debug(this.getClass(), "Full updated Disallowed");
            return false;
        }
        return true;
    }

    public boolean allowBasicUpdate() {
        long now = Instant.now().getEpochSecond();
        if (now - abuseFilters.lastSmallReq() < 60) {
            Log.SERVER.debug(this.getClass(), "Small updated Disallowed");
            return false;
        }
        return true;
    }

    public String getLoggable() {
        return "ID:" + playerId + " Name:" + playerData.getDisplayName() + " IP:" + abuseFilters.ips();
    }

    // Getters/Setters

    public int getPlayerId() {
        return playerId;
    }

    public String getName() {
        return playerData.getDisplayName();
    }

    public boolean isConnected() {
        return playerSession.isConnected();
    }

    public boolean disconnect() {
        playerSession.close();
        return true;
    }

    public void setConnection(PlayerSession session) {
        // FIXME implement this, make new immutable
        //playerSession = connection;
    }

    public boolean inCombat() {
        return inCombat;
    }

    public void setInCombat(boolean inCombat) {
        this.inCombat = inCombat;
    }

    public MatchInstance getGameRoom() {
        return matchInstance;
    }

    public void setGameRoom(MatchInstance matchInstance) {
        this.matchInstance = matchInstance;
    }

    public Map<Integer, PawnSet> getPawnSets() {
        return playerData.getPawnSets();
    }

    public PawnSet getOverworldPawnSet() {
        return playerData.getPawnSets().get(1); // FIXME this need to be anplayerIdreserved for their overworld set
    }

    public long getLastMsgEpoch() {
        return abuseFilters.wsMsgEpoch();
    }

    public boolean hasRestTimeout() {
        return abuseFilters.restTimeout();
    }

    public boolean isBot() {
        return isBot;
    }

    public void setIp(String ip) {
        boolean singleIp = abuseFilters.setIp(ip);
        if (!singleIp) {
            Log.ABUSE.info("Multiple ips detected | Player: " + playerId + " | ips: " + abuseFilters.ips());
        }
    }

    public Map<CardDomain, List<String>> getOwnedCards() {
        return playerData.getOwnedCards();
    }

    // Map ownedCards uids are treated different internally and externally, as there are foil
    // ownedCards that are variants of internal ownedCards. The internal representation gets the
    // display type flag stripped

    // we need to copy as we mutate via removal to validate the set
    public Map<CardDomain, List<String>> getValidCards() {
        return playerData.getValidCards().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> new ArrayList<>(entry.getValue())
                ));
    }

    public boolean isPremium() {
        return isPremium;
    }

    public List<String> getIp() {
        return abuseFilters.ips();
    }


}
