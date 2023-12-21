package io.mindspce.outerfieldsserver.entities.player;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.mindspce.outerfieldsserver.networking.outgoing.NetMessage;
import io.mindspice.mindlib.util.JsonUtils;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.function.Consumer;


public class PlayerSession {
    private static final ObjectWriter msgWriter = JsonUtils.getMapper().writerFor(NetMessage.class);

    private WebSocketSession session;
    private Consumer<BinaryMessage> messageOutConsumer;
    private final EntityUpdateContainer entityUpdateContainer;

    public PlayerSession(WebSocketSession session) {
        entityUpdateContainer = new EntityUpdateContainer();
        this.session = session;
    }

    public void setMessageOutConsumer(Consumer<BinaryMessage> msgOutConsumer) {
        this.messageOutConsumer = msgOutConsumer;
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

    public void submitMsg(byte[] msgBytes) {
       messageOutConsumer.accept(new BinaryMessage(msgBytes));
    }

    public void send(BinaryMessage msg) {
        if (!session.isOpen()) {
            // TODO do something
            return;
        }
        try {
            session.sendMessage(msg);
        } catch (IOException e) {
            //TODO log
        }
    }


}
