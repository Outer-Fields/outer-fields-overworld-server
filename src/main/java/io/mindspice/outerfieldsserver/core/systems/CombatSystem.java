package io.mindspice.outerfieldsserver.core.systems;

import com.fasterxml.jackson.databind.JsonNode;

import io.mindspice.mindlib.util.JsonUtils;
import io.mindspice.outerfieldsserver.combat.bot.BotFactory;
import io.mindspice.outerfieldsserver.combat.gameroom.MatchInstance;
import io.mindspice.outerfieldsserver.combat.gameroom.state.PlayerGameState;
import io.mindspice.outerfieldsserver.combat.schema.websocket.outgoing.lobby.NetQueueResponse;
import io.mindspice.outerfieldsserver.core.ActiveCombat;
import io.mindspice.outerfieldsserver.core.HttpServiceClient;
import io.mindspice.outerfieldsserver.core.Settings;
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
    private final Map<UUID, ActiveCombat> activeCombatTable = new ConcurrentHashMap<>();
    private final HttpServiceClient serviceClient;
    private final NonBlockingHashMapLong<PlayerEntity> playerTable;

    public CombatSystem(int id, ScheduledExecutorService combatExec,
            HttpServiceClient serviceClient, NonBlockingHashMapLong<PlayerEntity> playerTable) {
        super(id, SystemType.COMBAT_SYSTEM, true, Utility.msToNano(10));
        this.combatExec = combatExec;
        this.serviceClient = serviceClient;
        this.playerTable = playerTable;
        selfListener.registerInputHook(EventType.COMBAT_INIT_NPC, this::createNPCCombat, true);
        selfListener.registerInputHook(EventType.COMBAT_INIT_PVP, this::createPVPCombat, true);
    }

    public void createPVPCombat(Event<EventData.CombatInit> event) {

    }

    public void createNPCCombat(Event<EventData.CombatInit> event) {
        PlayerEntity player = playerTable.get(event.data().playerEntityId());

        if (Settings.GET().isPaused) {
            Log.SERVER.debug(this.getClass(), "Aborted creating game due to paused Server");
            return;
        }

        PlayerGameState playerState = new PlayerGameState(player, player.getOverworldPawnSet());
        PlayerGameState enemyState = BotFactory.GET().getBotPlayerState(playerState, event.data().enemyEntityId());

        MatchInstance matchInstance = new MatchInstance(playerState, enemyState, true);

        ScheduledFuture<?> gameProc = combatExec.scheduleWithFixedDelay(
                matchInstance,
                0,
                200,
                TimeUnit.MILLISECONDS);

        Log.SERVER.debug(this.getClass(), "Started CombatRoom: " + matchInstance.getRoomId());

        matchInstance.setReady(enemyState.getId());

        player.send(new NetQueueResponse(true));

        ActiveCombat activeCombat = new ActiveCombat(matchInstance, false, gameProc);

        Log.SERVER.debug(this.getClass(), "Initiated bot CombatRoom:" + matchInstance.getRoomId()
                + " for playerIds:" + player.getPlayerId());

        activeCombatTable.put(matchInstance.getRoomId(), activeCombat);
        matchInstance.getResultFuture().thenAccept(result -> {
            // FIXME this needs to relay back to the overworld
        }).exceptionally(ex -> {
            Log.SERVER.error(this.getClass(), "Error on finalize bot match callback");
            return null;
        });
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
