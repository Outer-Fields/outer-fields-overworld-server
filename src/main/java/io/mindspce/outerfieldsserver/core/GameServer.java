package io.mindspce.outerfieldsserver.core;

import io.mindspce.outerfieldsserver.core.networking.SocketInQueue;
import io.mindspce.outerfieldsserver.entities.player.PlayerSession;
import io.mindspce.outerfieldsserver.entities.player.PlayerState;
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
        tickExecutor.scheduleAtFixedRate(playerUpdateTick(), 0, 50000000, TimeUnit.NANOSECONDS);
    }

    public PlayerState getPlayer(int id) {
        return playerTable.get(id);
    }

    public Runnable playerUpdateTick() {
        lastTickTime = System.currentTimeMillis();
        return () -> worldState.getAreaList()
                .forEach(a -> a.getActivePlayers()
                        .forEach(p -> p.onTick(lastTickTime)));
    }

}