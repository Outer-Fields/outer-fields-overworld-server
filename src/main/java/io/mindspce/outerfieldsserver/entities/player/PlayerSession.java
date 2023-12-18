package io.mindspce.outerfieldsserver.entities.player;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.mindspce.outerfieldsserver.networking.outgoing.NetMessage;
import io.mindspice.mindlib.util.JsonUtils;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;


public class PlayerSession {
    private static final ObjectWriter msgWriter = JsonUtils.getMapper().writerFor(NetMessage.class);

    private WebSocketSession session;
    private final EntityUpdateContainer entityUpdateContainer;

    public PlayerSession(WebSocketSession session) {
        entityUpdateContainer = new EntityUpdateContainer();
        this.session = session;
    }

    public EntityUpdateContainer entityUpdateContainer() {
        return entityUpdateContainer;
    }

    public void setConnection(WebSocketSession webSocketSession) {
        this.session = webSocketSession;
    }

    public boolean isConnected(){
        return session.isOpen();
    }

    public void send(byte[] msgBytes) {
        if (!session.isOpen()) {
            // TODO do something
            System.out.println("essions not open");
            return;
        }
        try {
            System.out.println("sending");
            session.sendMessage(new BinaryMessage(msgBytes));
        } catch (IOException e) {
            //TODO log
        }
    }


}
