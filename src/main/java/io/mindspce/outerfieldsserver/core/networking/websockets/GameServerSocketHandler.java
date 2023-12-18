package io.mindspce.outerfieldsserver.core.networking.websockets;

import io.mindspce.outerfieldsserver.area.AreaInstance;
import io.mindspce.outerfieldsserver.area.ChunkData;
import io.mindspce.outerfieldsserver.area.TileData;
import io.mindspce.outerfieldsserver.core.networking.SocketInQueue;
import io.mindspce.outerfieldsserver.entities.player.PlayerState;
import io.mindspce.outerfieldsserver.enums.AreaId;
import io.mindspce.outerfieldsserver.networking.NetMsgIn;
import io.mindspce.outerfieldsserver.networking.incoming.NetMessageIn;
import io.mindspice.mindlib.data.geometry.IVector2;
import org.jctools.maps.NonBlockingHashMapLong;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;


@Controller
public class GameServerSocketHandler extends AbstractWebSocketHandler {
    private final SocketInQueue socketQueue;
    private final NonBlockingHashMapLong<PlayerState> playerTable;
    public GameServerSocketHandler(SocketInQueue socketInQueue,
            NonBlockingHashMapLong<PlayerState> playerTableInstance) {
        socketQueue = socketInQueue;
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
            socketQueue.handOffMessage(netMessageIn);

        } catch (Exception e) {
            //todo logging
            session.close();
        }
    }

    volatile int count = 1;
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        session.getAttributes().put("pid", count);
        System.out.println("Connection id");
        playerTable.get(count).setPlayerSession(session);
        count++;
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
        System.out.println("Connection closed");
    }

    // Implement other necessary methods like handleTextMessage if needed
}

