package io.mindspce.outerfieldsserver.entities.player;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.mindspce.outerfieldsserver.networking.outgoing.NetMessage;
import io.mindspice.mindlib.util.JsonUtils;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;


public class PlayerSession {
    private static final ObjectWriter msgWriter = JsonUtils.getMapper().writerFor(NetMessage.class);

    private final int id;
    private WebSocketSession connection;
    private PlayerCharacter character;

    public PlayerSession(int playerId) {
        this.id = playerId;
    }

    public void setConnection(WebSocketSession webSocketSession) {
        this.connection = webSocketSession;
    }

    public void send(NetMessage<?> message) {
        if (!connection.isOpen()) { return; }
        try {
            byte[] msg = msgWriter.writeValueAsBytes(message);
            connection.sendMessage(new BinaryMessage(msg));
        } catch (JsonProcessingException e) {
            //TODO log
        } catch (IOException e) {
            //TODO log
        }
    }

    public PlayerCharacter getCharacter() {
        return character;
    }
}
