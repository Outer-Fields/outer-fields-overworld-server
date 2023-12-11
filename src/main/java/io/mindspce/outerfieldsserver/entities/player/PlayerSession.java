package io.mindspce.outerfieldsserver.entities.player;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.mindspice.mindlib.util.JsonUtils;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;


public class PlayerSession {
    private final int playerId;
    private WebSocketSession connection;

    public PlayerSession(int playerId) {
        this.playerId = playerId;
    }

    public void setConnection(WebSocketSession webSocketSession) {
        this.connection = webSocketSession;
    }

    public void send(Object o) {
        if (!connection.isOpen()) { return; }
        try {
            byte[] msg = JsonUtils.writeBytes(o);
            connection.sendMessage(new BinaryMessage(msg));
        } catch (JsonProcessingException e) {
            //TODO log
        } catch (IOException e) {
            //TODO log
        }
    }
}
