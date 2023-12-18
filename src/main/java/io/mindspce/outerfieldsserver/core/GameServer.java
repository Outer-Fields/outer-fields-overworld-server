package io.mindspce.outerfieldsserver.core;

import io.mindspce.outerfieldsserver.area.AreaInstance;
import io.mindspce.outerfieldsserver.area.ChunkData;
import io.mindspce.outerfieldsserver.area.TileData;
import io.mindspce.outerfieldsserver.core.networking.SocketInQueue;
import io.mindspce.outerfieldsserver.entities.player.PlayerSession;
import io.mindspce.outerfieldsserver.entities.player.PlayerState;
import io.mindspce.outerfieldsserver.enums.AreaId;
import io.mindspice.mindlib.data.geometry.IVector2;
import org.jctools.maps.NonBlockingHashMapLong;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class GameServer {
    public final WorldState worldState;
    private final NonBlockingHashMapLong<PlayerState> playerTable;
    private volatile long lastTickTime;
    @Autowired private SocketInQueue socketInQueue;
    private final ScheduledExecutorService tickExecutor = Executors.newSingleThreadScheduledExecutor();

    public GameServer(WorldState worldState, NonBlockingHashMapLong<PlayerState> playerTable) {
        this.worldState = worldState;
        this.playerTable = playerTable;
        tickExecutor.scheduleAtFixedRate(
                playerUpdateTick(),
                0,
                ServerConst.NANOS_IN_SEC / GameSettings.GET().tickRate(),
                TimeUnit.NANOSECONDS
        );
        tickExecutor.execute(initPlayer1());
    }

    public PlayerState getPlayer(int id) {
        return playerTable.get(id);
    }

    public Runnable playerUpdateTick() {
        return () -> {
            try {
                lastTickTime = System.currentTimeMillis();
                worldState.getAreaList()
                        .forEach(a -> a.getActivePlayers()
                                .forEach(p -> p.onTick(lastTickTime)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
    }

    // FIXME this is for testing

    public Runnable initPlayer1(){
        return (() -> {
            try {
                PlayerState playerState = new PlayerState(1);
                PlayerState playerState2 = new PlayerState(2);
                playerTable.put(1, playerState);
                playerTable.put(2, playerState);
                ChunkData[][] cdArr = new ChunkData[4][4];

                for (int i = 0; i < 4; ++i) {
                    for (int j = 0; j < 4; ++j) {
                        TileData[][] tdArr = new TileData[4096][4096];
                        for (int x = 0; x < 4096; ++x) {
                            for (int y = 0; y < 4096; ++y) {
                                TileData td = new TileData(IVector2.of(i * 32, j * 32));
                                tdArr[x][y] = td;
                            }
                        }
                        ChunkData cd = new ChunkData(
                                IVector2.of(i, j),
                                IVector2.of(i * 4096, j * 4096),
                                IVector2.of(4096, 4096),
                                tdArr,
                                Map.of());
                        cdArr[i][j] = cd;
                    }
                }
                AreaInstance ai = new AreaInstance(AreaId.TEST, cdArr);
                ai.addActivePlayer(playerState);
                ai.addActivePlayer(playerState2);
                playerState.init(ai, 0, 0);
                playerState.setId(1);
                playerState2.setId(2);
                playerState2.init(ai, 0, 0);
                worldState.addArea(AreaId.TEST, ai);
                System.out.println("inited player");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }


}