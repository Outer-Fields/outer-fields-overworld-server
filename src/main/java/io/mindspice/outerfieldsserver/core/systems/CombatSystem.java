package io.mindspice.outerfieldsserver.core.systems;

import com.fasterxml.jackson.databind.JsonNode;

import io.mindspice.mindlib.util.JsonUtils;
import io.mindspice.outerfieldsserver.combat.bot.BotFactory;
import io.mindspice.outerfieldsserver.combat.bot.BotPlayer;
import io.mindspice.outerfieldsserver.combat.gameroom.MatchInstance;
import io.mindspice.outerfieldsserver.combat.gameroom.MatchResult;
import io.mindspice.outerfieldsserver.combat.gameroom.state.PlayerMatchState;
import io.mindspice.outerfieldsserver.combat.schema.websocket.incoming.NetCombatAction;
import io.mindspice.outerfieldsserver.combat.schema.websocket.outgoing.lobby.NetQueueResponse;
import io.mindspice.outerfieldsserver.core.ActiveCombat;
import io.mindspice.outerfieldsserver.core.HttpServiceClient;
import io.mindspice.outerfieldsserver.core.Settings;
import io.mindspice.outerfieldsserver.core.singletons.EntityManager;
import io.mindspice.outerfieldsserver.data.OverWorldPawnState;
import io.mindspice.outerfieldsserver.entities.PlayerEntity;
import io.mindspice.outerfieldsserver.enums.SystemType;
import io.mindspice.outerfieldsserver.systems.event.Event;
import io.mindspice.outerfieldsserver.systems.event.EventData;
import io.mindspice.outerfieldsserver.systems.event.EventType;
import io.mindspice.outerfieldsserver.systems.event.SystemListener;
import io.mindspice.outerfieldsserver.util.Log;
import io.mindspice.outerfieldsserver.util.Utility;
import org.jctools.maps.NonBlockingHashMapLong;

import java.util.*;
import java.util.concurrent.*;


public class CombatSystem extends SystemListener {
    private final ScheduledExecutorService combatExec;
    private final Map<UUID, ActiveCombat> activeCombatTable;
    private final HttpServiceClient serviceClient;
    private final NonBlockingHashMapLong<PlayerEntity> playerTable;

    public CombatSystem(int id, ScheduledExecutorService combatExec, HttpServiceClient serviceClient,
            NonBlockingHashMapLong<PlayerEntity> playerTable, Map<UUID, ActiveCombat> combatTable) {
        super(id, SystemType.COMBAT_SYSTEM, true, Utility.msToNano(10));
        this.combatExec = combatExec;
        this.serviceClient = serviceClient;
        this.playerTable = playerTable;
        this.activeCombatTable = combatTable;
        selfListener.registerInputHook(EventType.COMBAT_INIT_NPC, this::createNPCCombat, true);
        selfListener.registerInputHook(EventType.COMBAT_INIT_PVP, this::createPVPCombat, true);
        selfListener.registerInputHook(EventType.NETWORK_IN_COMBAT_ACTION, this::onNetCombatAction, true);
    }

    // Event uses playerId vs entityId for recipient addressing

    public void onNetCombatAction(Event<NetCombatAction> event) {
        PlayerEntity player = playerTable.get(event.recipientEntityId());
        if (player == null) {
            //TODO log this
            return;
        }
        player.oncombatMessage(event.data());
    }

    public void createPVPCombat(Event<EventData.CombatInit> event) {

    }

    public void createNPCCombat(Event<EventData.CombatInit> event) {
        PlayerEntity player = playerTable.get(event.data().playerEntityId());

        if (Settings.GET().isPaused) {
            Log.SERVER.debug(this.getClass(), "Aborted creating game due to paused Server");
            return;
        }

        PlayerMatchState playerState = new PlayerMatchState(player, player.getOverworldPawnSet());
        PlayerMatchState enemyState = BotFactory.GET().getBotPlayerState(playerState, event.data().enemyEntityId());

        MatchInstance matchInstance = new MatchInstance(playerState, enemyState, true);

        ScheduledFuture<?> gameProc = combatExec.scheduleWithFixedDelay(
                matchInstance,
                0,
                200,
                TimeUnit.MILLISECONDS);

        Log.SERVER.debug(this.getClass(), "Started CombatRoom: " + matchInstance.getRoomId());

        matchInstance.setReady(enemyState.getId());

        player.sendJson(new NetQueueResponse(true));

        ActiveCombat activeCombat = new ActiveCombat(matchInstance, false, gameProc);

        Log.SERVER.debug(this.getClass(), "Initiated bot CombatRoom:" + matchInstance.getRoomId()
                + " for playerIds:" + player.getPlayerId());

        activeCombatTable.put(matchInstance.getRoomId(), activeCombat);
        matchInstance.getResultFuture().thenAccept(result -> {
            finalizeNPCCombat(result, matchInstance, gameProc);
        }).exceptionally(ex -> {
            Log.SERVER.error(this.getClass(), "Error on finalize bot match callback");
            return null;
        });
    }

    private void finalizeNPCCombat(MatchResult matchResult, MatchInstance matchInstance, ScheduledFuture<?> gameProc) {
        PlayerEntity player1 = playerTable.get(matchInstance.getPlayer1().getId());
        PlayerEntity player2 = (PlayerEntity) EntityManager.GET().entityById(matchInstance.getPlayer2().getId());
        player1.updateOverWorldPawnStates(
                matchResult.player1().getPawns().stream().map(OverWorldPawnState::fromPawn).toList()
        );

        if (matchResult.losers().contains(matchResult.player1())) { // emit player death (triggers inv drop)
            EntityManager.GET().emitEvent(Event.characterDeath(
                    player1.areaId(),
                    new EventData.CharacterDeath(player1.entityId(), player2.entityId())
            ));
        }
        if (matchResult.losers().contains(matchResult.player1())) { // emit enemy dead (triggers loot)
            EntityManager.GET().emitEvent(Event.characterDeath(
                    player2.areaId(),
                    new EventData.CharacterDeath(player2.entityId(), player1.entityId())
            ));
        }
    }

    public JsonNode getGameRoomStatusJson(String uuid) {
        return activeCombatTable.get(UUID.fromString(uuid)).matchInstance().getStatusJson();
    }

    public JsonNode getStatusJson() {
        return new JsonUtils.ObjectBuilder()
                .put("active_games_count", activeCombatTable.size())
                .put("active_games", activeCombatTable.values().stream().map(g -> g.matchInstance().getStatusJson()).toList())
//                .put("match_queue_size", matchQueue.size())
//                .put("match_queue", matchQueue.stream().toList())
//                .put("pregame_queue_size", preGameQueue.size())
//                .put("pregame_queue", preGameQueue.values().stream().map(PreGameQueue::getStatusJson).toList())
                .buildNode();
    }
}
