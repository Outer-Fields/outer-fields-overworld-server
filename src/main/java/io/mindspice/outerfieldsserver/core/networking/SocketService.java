package io.mindspice.outerfieldsserver.core.networking;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.protobuf.InvalidProtocolBufferException;
import io.mindspice.mindlib.util.JsonUtils;
import io.mindspice.outerfieldsserver.combat.schema.websocket.incoming.NetCombatAction;
import io.mindspice.outerfieldsserver.core.networking.incoming.NetPlayerActionMsg;
import io.mindspice.outerfieldsserver.core.networking.incoming.NetInPlayerPosition;
import io.mindspice.outerfieldsserver.core.networking.proto.EntityProto;
import io.mindspice.outerfieldsserver.core.singletons.EntityManager;
import io.mindspice.outerfieldsserver.entities.PlayerEntity;
import io.mindspice.outerfieldsserver.enums.NetPlayerAction;
import io.mindspice.outerfieldsserver.networking.incoming.NetMessageIn;
import io.mindspice.outerfieldsserver.systems.event.Event;
import org.jctools.maps.NonBlockingHashMapLong;
import org.jctools.queues.MpscArrayQueue;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.LockSupport;


public class SocketService {
    private final MpscArrayQueue<NetMessageIn> networkInQueue = new MpscArrayQueue<>(10000);
    private final ExecutorService networkInExec = Executors.newSingleThreadExecutor();
    private volatile boolean running = true;
    NonBlockingHashMapLong<PlayerEntity> playerTable;
    EntityManager entityManger = EntityManager.GET();

    public SocketService(NonBlockingHashMapLong<PlayerEntity> playerTable) {
        this.playerTable = playerTable;
        networkInExec.submit(networkInProcessor());
    }

    public void handOffMessageIn(NetMessageIn msg) {
        boolean success = networkInQueue.offer(msg);
        while (!success) {
            LockSupport.parkNanos(100);
            success = networkInQueue.offer(msg);
        }
    }

    public Runnable networkInProcessor() {
        return () -> {
            while (running) {
                NetMessageIn nextMsg;
                while ((nextMsg = networkInQueue.relaxedPoll()) == null) {
                    Thread.onSpinWait();
                }
                handleMsgIn(nextMsg);
            }
        };
    }

    private void handleMsgIn(NetMessageIn msg) {
        try {
            EntityProto.GamePacketIn packetIn = EntityProto.GamePacketIn.parseFrom(msg.data());

            if (packetIn.hasPositionUpdate()) {
                EntityProto.PositionUpdate posUpdate = packetIn.getPositionUpdate();
                NetInPlayerPosition netIn = new NetInPlayerPosition(
                        msg.playerId(), posUpdate.getPosX(), posUpdate.getPosY(), msg.timestamp()
                );
                entityManger.emitEvent(Event.netInPlayerPosition(msg.entityId(), netIn));
            }

            if (!packetIn.getActionsList().isEmpty()) {
                Map<NetPlayerAction, List<NetPlayerActionMsg>> actionMap = new EnumMap<>(NetPlayerAction.class);
                for (EntityProto.Action action : packetIn.getActionsList()) {
                    NetPlayerActionMsg playerAction = NetPlayerActionMsg.fromProto(action);
                    List<NetPlayerActionMsg> existingActions = actionMap.computeIfAbsent(
                            playerAction.actionKey(), k -> new ArrayList<>(10)
                    );
                    existingActions.add(playerAction);
                }
                entityManger.emitEvent(Event.netInPlayerAction(msg.entityId(), actionMap));
            }

            if (packetIn.hasCombatJson()) {
                EntityProto.CombatJson combatJson = packetIn.getCombatJson();
                try {     // This uses playerId vs entityId for recipient addressing
                    NetCombatAction combatAction = JsonUtils.readValue(combatJson.getJson(), NetCombatAction.class);
                    entityManger.emitEvent(Event.netInCombatAction(msg.playerId(), combatAction));
                } catch (JsonProcessingException e) {
                    // TODO log this
                }
            }

            // ... Handle other fields similarly

        } catch (InvalidProtocolBufferException e) {

            // TODO log this
        }
    }


}


