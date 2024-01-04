package io.mindspce.outerfieldsserver.core.networking.websockets;

import io.mindspce.outerfieldsserver.networking.NetMsgIn;
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
//    private final SocketService socketService;
//    private final NonBlockingHashMapLong<PlayerState> playerTable;

    public GameServerSocketHandler(/*SocketService socketService,
//            NonBlockingHashMapLong<PlayerState> playerTableInstance*/) {
//        this.socketService = socketService;
//        playerTable = playerTableInstance;
    }

    @Override
    public void handleBinaryMessage(@NonNull WebSocketSession session, @NonNull BinaryMessage message) throws IOException {
        try {
            ByteBuffer byteBuffer = message.getPayload();
            byte messageTypeByte = byteBuffer.get();
            NetMsgIn messageType = NetMsgIn.convert(messageTypeByte);

            int pid = (int) session.getAttributes().get("pid");

           // NetMessageIn netMessageIn = new NetMessageIn(System.currentTimeMillis(), pid, messageType, byteBuffer);

        //    socketService.handOffMessageIn(netMessageIn);

        } catch (Exception e) {
            //todo logging
            session.close();
        }
    }

    volatile int count = 1;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {

//        var ps = EntityManager.GET().newPlayerState(count);
//        playerTable.put(ps.entityId(), ps);
//        session.getAttributes().put("pid", ps.entityId());
//        WorldState.GET().getAreaTable().get(AreaId.TEST).addActivePlayer(ps);
//        PlayerSession pSession = new PlayerSession(session);
//        pSession.setMessageOutConsumer(socketService.networkOutHandler(pSession));
//        ps.init(new PlayerSession(session), AreaId.TEST, 0, 0);
//        playerTable.get(ps.entityId()).setPlayerSession(pSession);
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
        System.out.println("Connection closed");
    }

    // Implement other necessary methods like handleTextMessage if needed
}

