package io.mindspce.outerfieldsserver.core.networking;

import io.mindspce.outerfieldsserver.area.AreaInstance;
import io.mindspce.outerfieldsserver.area.ChunkData;
import io.mindspce.outerfieldsserver.area.TileData;
import io.mindspce.outerfieldsserver.core.GameServer;
import io.mindspce.outerfieldsserver.core.WorldState;
import io.mindspce.outerfieldsserver.entities.player.PlayerState;
import io.mindspce.outerfieldsserver.enums.AreaId;
import io.mindspce.outerfieldsserver.networking.NetMessageInHandler;
import io.mindspce.outerfieldsserver.networking.incoming.NetMessageIn;
import io.mindspice.mindlib.data.geometry.IVector2;
import org.jctools.maps.NonBlockingHashMapLong;
import org.jctools.queues.MpscArrayQueue;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;


public class SocketInQueue implements NetMessageInHandler {
    private final MpscArrayQueue<NetMessageIn> queue = new MpscArrayQueue<>(10000);
    private final ScheduledExecutorService networkInExecutor = Executors.newSingleThreadScheduledExecutor();
    NonBlockingHashMapLong<PlayerState> playerTable;

    public SocketInQueue(NonBlockingHashMapLong<PlayerState> playerTable) {
        System.out.println("started socket queue");
        this.playerTable = playerTable;
        networkInExecutor.scheduleAtFixedRate(networkInProcessor(), 0, 2, TimeUnit.MILLISECONDS);
    }



    public void handOffMessage(NetMessageIn msg) {
        boolean success = queue.offer(msg);
        while (!success) {
            LockSupport.parkNanos(100);
            success = queue.offer(msg);
        }
    }


    public Runnable networkInProcessor() {
        return () ->  queue.drain(this::handleMessage);
    }


    private void handleMessage(NetMessageIn msg) {
        switch (msg.type()) {
            case CLIENT_POSITION -> {
                PlayerState player = playerTable.get(msg.pid());
                if (player != null) {
                    handleClientPosition(msg, player);
                }

            }
        }
    }
}