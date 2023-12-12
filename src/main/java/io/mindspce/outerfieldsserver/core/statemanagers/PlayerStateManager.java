package io.mindspce.outerfieldsserver.core.statemanagers;

import io.mindspce.outerfieldsserver.area.ChunkData;
import io.mindspce.outerfieldsserver.core.GameSettings;
import io.mindspce.outerfieldsserver.core.WorldState;
import io.mindspce.outerfieldsserver.core.networking.SocketService;
import io.mindspce.outerfieldsserver.entities.item.ItemEntity;
import io.mindspce.outerfieldsserver.entities.item.ItemState;
import io.mindspce.outerfieldsserver.entities.locations.LocationEntity;
import io.mindspce.outerfieldsserver.entities.locations.LocationState;
import io.mindspce.outerfieldsserver.entities.nonplayer.EnemyState;
import io.mindspce.outerfieldsserver.entities.nonplayer.NonPlayerEntity;
import io.mindspce.outerfieldsserver.entities.nonplayer.NpcState;
import io.mindspce.outerfieldsserver.entities.player.*;
import io.mindspce.outerfieldsserver.enums.EntityType;
import io.mindspce.outerfieldsserver.enums.NetMsgType;
import io.mindspce.outerfieldsserver.networking.outgoing.NetEntityUpdate;
import io.mindspce.outerfieldsserver.networking.outgoing.NetMessage;
import io.mindspice.mindlib.data.geometry.IRect2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;


public class PlayerStateManager implements Runnable {
    private volatile Collection<PlayerSession> activePlayers;
    private final WorldState worldState;
    private final SocketService socketService;
    AtomicInteger tickCount = new AtomicInteger(0);

    public PlayerStateManager(WorldState worldState, SocketService socketService) {
        this.worldState = worldState;
        this.socketService = socketService;
    }

    @Override
    public void run() {

        int currTick = tickCount.addAndGet(1);
        if (currTick % 30 == 0) {
            activePlayers = worldState.getPlayerTable().values();
        }

        for (var session : activePlayers) {
            PlayerPosition positionData = session.getCharacter().getPositionData();
            IRect2 updateBounds = positionData.getUpdateBounds();
            ChunkData[][] localGrid = positionData.getLocalChunkGrid();

            for (int x = 0; x < 3; ++x) {
                for (int y = 0; y < 3; ++y) {
                    ChunkData chunk = localGrid[x][y];

                    // Send player other players info (Every Tick)
                    Set<PlayerCharacter> playerEntities = chunk.getActivePlayers();
                    if (!playerEntities.isEmpty()) {

                        List<PlayerEntity> updatesList = new ArrayList<>(playerEntities.size());
                        for (PlayerCharacter character : chunk.getActivePlayers()) {
                            if (updateBounds.withinBounds(character.getGlobalPos())) {
                                updatesList.add(character);
                            }
                        }
                        if (!updatesList.isEmpty()) {
                            NetEntityUpdate<PlayerEntity> playersUpdate = new NetEntityUpdate<>(EntityType.PLAYER, updatesList);
                            session.send(new NetMessage<>(NetMsgType.EntityUpdate, playersUpdate));
                        }
                    }

                    // Send npc/enemy info on non-player tick interval
                    if (currTick % GameSettings.GET().npcTickInterval() == 0) {

                        Set<NpcState> npcEntities = chunk.getActiveNpcs();
                        Set<EnemyState> enemyEntities = chunk.getActiveEnemies();
                        List<NonPlayerEntity> updatesList = new ArrayList<>(npcEntities.size() + enemyEntities.size());

                        if (!npcEntities.isEmpty()) {
                            for (NpcState npc : chunk.getActiveNpcs()) {
                                if (updateBounds.withinBounds(npc.getGlobalPos())) {
                                    updatesList.add(npc);
                                }
                            }
                        }

                        if (!enemyEntities.isEmpty()) {
                            for (EnemyState enemy : chunk.getActiveEnemies()) {
                                if (updateBounds.withinBounds(enemy.getGlobalPos())) {
                                    updatesList.add(enemy);
                                }
                            }
                        }
                        if (!updatesList.isEmpty()) {
                            NetEntityUpdate<NonPlayerEntity> playersUpdate = new NetEntityUpdate<>(EntityType.NON_PLAYER, updatesList);
                            session.send(new NetMessage<>(NetMsgType.EntityUpdate, playersUpdate));
                        }
                    }

                    // Send item update on item ticks
                    if (currTick % GameSettings.GET().itemTickInterval() == 0) {
                        Set<ItemState> itemEntities = chunk.getActiveItems();

                        if (!itemEntities.isEmpty()) {
                            List<ItemEntity> updatesList = new ArrayList<>(itemEntities.size());
                            for (ItemState item : chunk.getActiveItems()) {
                                if (updateBounds.withinBounds(item.getGlobalPos())) {
                                    updatesList.add(item);
                                }
                            }
                            if (!updatesList.isEmpty()) {
                                NetEntityUpdate<ItemEntity> playersUpdate = new NetEntityUpdate<>(EntityType.ITEM, updatesList);
                                session.send(new NetMessage<>(NetMsgType.EntityUpdate, playersUpdate));
                            }
                        }
                    }
                    if (currTick % GameSettings.GET().locationTickInterval() == 0) {

                        Set<LocationState> locationEntities = chunk.getLocationStates();
                        List<LocationEntity> updatesList = new ArrayList<>(locationEntities.size());
                        if (!locationEntities.isEmpty()) {
                            for (LocationState location : chunk.getLocationStates()) {
                                if (updateBounds.withinBounds(location.getGlobalPos())) {
                                    updatesList.add(location);
                                }
                            }
                            if (!updatesList.isEmpty()) {
                                NetEntityUpdate<LocationEntity> playersUpdate = new NetEntityUpdate<>(EntityType.LOCATION, updatesList);
                                session.send(new NetMessage<>(NetMsgType.EntityUpdate, playersUpdate));
                            }
                        }
                    }
                }
            }
        }
    }

    private List<PlayerEntity> getAndSendPlayerUpdate() {
        return null;
    }

}


