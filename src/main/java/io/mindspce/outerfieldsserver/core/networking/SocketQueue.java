package io.mindspce.outerfieldsserver.core.networking;

import io.mindspce.outerfieldsserver.entities.player.PlayerSession;
import io.mindspce.outerfieldsserver.entities.player.PlayerState;
import io.mindspce.outerfieldsserver.networking.NetMessageHandlers;
import io.mindspce.outerfieldsserver.networking.incoming.NetMessageIn;
import org.jctools.maps.NonBlockingHashMapLong;
import org.jctools.queues.MpscArrayQueue;
import org.springframework.web.socket.BinaryMessage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;


public class SocketQueue implements NetMessageHandlers {
    private final MpscArrayQueue<NetMessageIn> networkInQueue = new MpscArrayQueue<>(10000);
    private final MpscArrayQueue<NetMessageIn> networkOutQueue = new MpscArrayQueue<>(10000);
    private final ExecutorService networkInExec = Executors.newSingleThreadExecutor();
    private final ExecutorService networkOutExec = Executors.newVirtualThreadPerTaskExecutor();
    private volatile boolean running = true;
    NonBlockingHashMapLong<PlayerState> playerTable;

    public SocketQueue(NonBlockingHashMapLong<PlayerState> playerTable) {
        this.playerTable = playerTable;
        networkInExec.submit(networkInProcessor());
    }

    public void handOffMessageIn(NetMessageIn msg) {
        boolean success = networkOutQueue.offer(msg);
        while (!success) {
            LockSupport.parkNanos(100);
            success = networkOutQueue.offer(msg);
        }
    }

    public Consumer<BinaryMessage> networkOutHandler(PlayerSession playerSession) {
        return msg -> networkOutExec.submit(() -> playerSession.send(msg));
    }

    public Runnable networkInProcessor() {
        return () -> {
            System.out.println("ran");
            while (running) {
                networkOutQueue.drain(this::handleMsgIn);
                Thread.onSpinWait();
                LockSupport.parkNanos(10000);


            }
        };
    }

    private void handleMsgIn(NetMessageIn msg) {
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