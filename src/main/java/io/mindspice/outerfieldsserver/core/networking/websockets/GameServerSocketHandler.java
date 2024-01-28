package io.mindspice.outerfieldsserver.core.networking.websockets;

import io.mindspice.outerfieldsserver.core.networking.SocketService;
import io.mindspice.outerfieldsserver.core.singletons.EntityManager;
import io.mindspice.outerfieldsserver.entities.PlayerEntity;
import io.mindspice.outerfieldsserver.enums.*;
import io.mindspice.outerfieldsserver.enums.AreaId;
import io.mindspice.outerfieldsserver.enums.ClothingItem;
import io.mindspice.outerfieldsserver.enums.EntityState;
import io.mindspice.outerfieldsserver.networking.NetMsgIn;
import io.mindspice.outerfieldsserver.networking.incoming.NetMessageIn;
import io.mindspice.mindlib.data.geometry.IVector2;
import org.jctools.maps.NonBlockingHashMapLong;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;


@Controller
public class GameServerSocketHandler extends AbstractWebSocketHandler {
    private final SocketService socketService;
    private final NonBlockingHashMapLong<PlayerEntity> playerTable;

    public GameServerSocketHandler(SocketService socketService,
            NonBlockingHashMapLong<PlayerEntity> playerTable) {
        this.socketService = socketService;
        this.playerTable = playerTable;
    }

    @Override
    public void handleBinaryMessage(@NonNull WebSocketSession session, @NonNull BinaryMessage message) throws IOException {
        try {
            ByteBuffer byteBuffer = message.getPayload();
            byte messageTypeByte = byteBuffer.get();
            NetMsgIn messageType = NetMsgIn.convert(messageTypeByte);

            int pid = (int) session.getAttributes().get("pid");
            int eid = (int) session.getAttributes().get("eid");

            NetMessageIn netMessageIn = new NetMessageIn(System.currentTimeMillis(), pid, eid, messageType, byteBuffer);
            socketService.handOffMessageIn(netMessageIn);
        } catch (Exception e) {
            e.printStackTrace();

            //todo logging
            session.close();
        }
    }

    volatile int count = 1;

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        System.out.println("connection");

        var player = EntityManager.GET().newPlayerEntity(
                1, "test_1", List.of(EntityState.TEST),
                new ClothingItem[6], AreaId.TEST, IVector2.of(0, 0), session, true
        );

        session.getAttributes().put("pid", count);
        session.getAttributes().put("eid", player.entityId());

        playerTable.put(player.playerId(), player);

        count++;

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
        System.out.println("Connection closed:" + status);
    }

    // Implement other necessary methods like handleTextMessage if needed
}

