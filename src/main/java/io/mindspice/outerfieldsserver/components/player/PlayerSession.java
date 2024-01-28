package io.mindspice.outerfieldsserver.components.player;

import io.mindspice.outerfieldsserver.components.Component;
import io.mindspice.outerfieldsserver.entities.Entity;
import io.mindspice.outerfieldsserver.enums.ComponentType;
import io.mindspice.outerfieldsserver.systems.event.Event;
import io.mindspice.outerfieldsserver.systems.event.EventType;
import io.mindspice.outerfieldsserver.util.Log;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.List;


public class PlayerSession extends Component<PlayerSession> {
    public WebSocketSession session;

    public PlayerSession(Entity parentEntity, WebSocketSession session) {
        super(parentEntity, ComponentType.PLAYER_SESSION, List.of());
        this.session = session;
        registerListener(EventType.NETWORK_PLAYER_RECONNECT, PlayerSession::onReconnection);
    }

    public void onReconnection(Event<WebSocketSession> event) {
        System.out.println("set_session");
        this.session = event.data();
    }

    public boolean isConnected() {
        return session.isOpen();
    }

    public void close() {
        try {
            session.close();
        } catch (IOException e) {
            Log.SERVER.debug(this.getClass(), "Error closing web socket session", e);
        }
    }

    public void send(byte[] data) {
        //  System.out.println("sending data: " + data.length);
        if (!session.isOpen()) {
            // TODO do something
            return;
        }
        Thread.ofVirtual().start(() -> {
            try {
                session.sendMessage(new BinaryMessage(data));
            } catch (IOException e) {
                // TODO log
                System.out.println("error sending message");
            }
        });
    }
}
