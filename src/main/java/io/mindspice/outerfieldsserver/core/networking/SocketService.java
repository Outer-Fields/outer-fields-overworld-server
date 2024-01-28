package io.mindspice.outerfieldsserver.core.networking;

import io.mindspice.outerfieldsserver.entities.PlayerEntity;
import io.mindspice.outerfieldsserver.networking.NetMessageHandlers;
import io.mindspice.outerfieldsserver.networking.incoming.NetMessageIn;
import io.mindspice.outerfieldsserver.networking.NetMsgIn;
import org.jctools.maps.NonBlockingHashMapLong;
import org.jctools.queues.MpscArrayQueue;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.LockSupport;


public class SocketService implements NetMessageHandlers {
    private final MpscArrayQueue<NetMessageIn> networkInQueue = new MpscArrayQueue<>(10000);
    private final ExecutorService networkInExec = Executors.newSingleThreadExecutor();
    private volatile boolean running = true;
    NonBlockingHashMapLong<PlayerEntity> playerTable;

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
                networkInQueue.drain(this::handleMsgIn);
                Thread.onSpinWait();
                LockSupport.parkNanos(10000);

            }
        };
    }

    private void handleMsgIn(NetMessageIn msg) {
        switch (msg.type()) {
            case NetMsgIn.CLIENT_POSITION -> handleClientPosition(msg);

        }
    }
}


