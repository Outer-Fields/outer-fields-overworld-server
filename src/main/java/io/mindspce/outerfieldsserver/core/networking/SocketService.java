package io.mindspce.outerfieldsserver.core.networking;

import io.mindspce.outerfieldsserver.components.PlayerSession;
import io.mindspce.outerfieldsserver.entities.player.PlayerEntity;
import io.mindspce.outerfieldsserver.networking.NetMessageHandlers;
import io.mindspce.outerfieldsserver.networking.incoming.NetMessageIn;
import org.jctools.maps.NonBlockingHashMapLong;
import org.jctools.queues.MpscArrayQueue;
import org.springframework.web.socket.BinaryMessage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BiConsumer;


public class SocketService implements NetMessageHandlers {
    private final MpscArrayQueue<NetMessageIn> networkInQueue = new MpscArrayQueue<>(10000);
    private final ExecutorService networkInExec = Executors.newSingleThreadExecutor();
    private final ExecutorService networkOutExec = Executors.newVirtualThreadPerTaskExecutor();
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

    public BiConsumer<PlayerSession, BinaryMessage> networkOutHandler() {
        return (session, packet) -> networkOutExec.execute(() -> session.send(packet));
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
            case CLIENT_POSITION -> {
                PlayerEntity player = playerTable.get(msg.pid());
                if (player != null) {
                    handleClientPosition(msg);
                }

            }
        }
    }


}