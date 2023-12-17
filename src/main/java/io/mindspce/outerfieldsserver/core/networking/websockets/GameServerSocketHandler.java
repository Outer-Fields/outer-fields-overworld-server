package io.mindspce.outerfieldsserver.core.networking.websockets;

import io.mindspce.outerfieldsserver.core.networking.SocketInQueue;
import io.mindspce.outerfieldsserver.networking.NetMsgIn;
import io.mindspce.outerfieldsserver.networking.incoming.NetMessageIn;
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
    private final SocketInQueue socketQueue;

    public GameServerSocketHandler(SocketInQueue socketInQueue) {
        socketQueue = socketInQueue;
    }

    @Override
    public void handleBinaryMessage(@NonNull WebSocketSession session, @NonNull BinaryMessage message) throws IOException {
        try {
            ByteBuffer byteBuffer = message.getPayload();
            byte messageTypeByte = byteBuffer.get();
            NetMsgIn messageType = NetMsgIn.convert(messageTypeByte);

            int pid = (int) session.getAttributes().get("pid");

            NetMessageIn netMessageIn = new NetMessageIn(System.currentTimeMillis(), pid, messageType, byteBuffer);

        } catch (Exception e) {
            //todo logging
            session.close();
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        session.getAttributes().put("pid", 1);
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
        // Connection closed
    }

    // Implement other necessary methods like handleTextMessage if needed
}

