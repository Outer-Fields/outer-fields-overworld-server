package io.mindspce.outerfieldsserver.core;

import io.mindspce.outerfieldsserver.area.AreaInstance;
import io.mindspce.outerfieldsserver.area.ChunkData;
import io.mindspce.outerfieldsserver.area.TileData;
import io.mindspce.outerfieldsserver.core.networking.SocketQueue;
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
    private final NonBlockingHashMapLong<PlayerState> playerTable;
    private volatile long lastTickTime;
    @Autowired private SocketQueue socketQueue;
    private final ScheduledExecutorService tickExecutor = Executors.newSingleThreadScheduledExecutor();

    public GameServer(NonBlockingHashMapLong<PlayerState> playerTable) {
        this.playerTable = playerTable;
        tickExecutor.scheduleAtFixedRate(
                playerUpdateTick(),
                0,
                //2,
                ServerConst.NANOS_IN_SEC / GameSettings.GET().tickRate(),
                TimeUnit.NANOSECONDS
                // TimeUnit.SECONDS
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
                WorldState.GET().getAreaList()
                        .forEach(a -> a.getActivePlayers()
                                .forEach(p -> p.onTick(lastTickTime)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
    }

    // FIXME this is for testing

    public Runnable initPlayer1() {
        return (() -> {
            try {
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
                WorldState.GET().init(Map.of(AreaId.TEST, ai));
                System.out.println("inited woolrd");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }


}