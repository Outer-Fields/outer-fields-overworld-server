package io.mindspce.outerfieldsserver.core.networking.websockets;

import io.mindspce.outerfieldsserver.core.WorldState;
import io.mindspce.outerfieldsserver.core.networking.SocketQueue;
import io.mindspce.outerfieldsserver.core.singletons.EntityManager;
import io.mindspce.outerfieldsserver.core.statemanagers.EnemyStateManager;
import io.mindspce.outerfieldsserver.entities.player.PlayerSession;
import io.mindspce.outerfieldsserver.entities.player.PlayerState;
import io.mindspce.outerfieldsserver.enums.AreaId;
import io.mindspce.outerfieldsserver.networking.NetMsgIn;
import io.mindspce.outerfieldsserver.networking.incoming.NetMessageIn;
import org.jctools.maps.NonBlockingHashMapLong;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.IOException;
import java.nio.ByteBuffer;


@Controller
public class GameServerSocketHandler extends AbstractWebSocketHandler {
    private final SocketQueue socketQueue;
    private final NonBlockingHashMapLong<PlayerState> playerTable;

    public GameServerSocketHandler(SocketQueue socketQueue,
            NonBlockingHashMapLong<PlayerState> playerTableInstance) {
        this.socketQueue = socketQueue;
        playerTable = playerTableInstance;
    }

    @Override
    public void handleBinaryMessage(@NonNull WebSocketSession session, @NonNull BinaryMessage message) throws IOException {
        try {
            ByteBuffer byteBuffer = message.getPayload();
            byte messageTypeByte = byteBuffer.get();
            NetMsgIn messageType = NetMsgIn.convert(messageTypeByte);

            int pid = (int) session.getAttributes().get("pid");

            NetMessageIn netMessageIn = new NetMessageIn(System.currentTimeMillis(), pid, messageType, byteBuffer);
            socketQueue.handOffMessageIn(netMessageIn);

        } catch (Exception e) {
            //todo logging
            session.close();
        }
    }

    volatile int count = 1;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {

        var ps = EntityManager.GET().newPlayerState(count);
        playerTable.put(ps.id(), ps);
        session.getAttributes().put("pid", ps.id());
        WorldState.GET().getAreaTable().get(AreaId.TEST).addActivePlayer(ps);
        PlayerSession pSession = new PlayerSession(session);
        pSession.setMessageOutConsumer(socketQueue.networkOutHandler(pSession));
        ps.init(new PlayerSession(session), AreaId.TEST, 0, 0);
        playerTable.get(ps.id()).setPlayerSession(pSession);
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
        System.out.println("Connection closed");
    }

    // Implement other necessary methods like handleTextMessage if needed
}

