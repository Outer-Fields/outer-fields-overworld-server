package io.mindspce.outerfieldsserver.core.networking;

import io.mindspce.outerfieldsserver.core.GameServer;
import io.mindspce.outerfieldsserver.entities.player.PlayerState;
import io.mindspce.outerfieldsserver.networking.NetMessageInHandler;
import io.mindspce.outerfieldsserver.networking.incoming.NetMessageIn;
import org.jctools.queues.MpscArrayQueue;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;


public class SocketInQueue implements NetMessageInHandler {
    private final MpscArrayQueue<NetMessageIn> queue = new MpscArrayQueue<>(10000);
    private final ScheduledExecutorService networkInExecutor = Executors.newSingleThreadScheduledExecutor();
    private final GameServer gameServer;

    public SocketInQueue(GameServer gameServer) {
        this.gameServer = gameServer;
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
                PlayerState player = gameServer.getPlayer(msg.pid());
                if (player != null) {
                    handleClientPosition(msg, player);
                }

            }
        }
    }
}