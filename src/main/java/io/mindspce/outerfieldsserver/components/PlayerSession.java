package io.mindspce.outerfieldsserver.components;

import com.fasterxml.jackson.databind.ObjectWriter;
import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.enums.ComponentType;
import io.mindspce.outerfieldsserver.networking.outgoing.NetMessage;
import io.mindspce.outerfieldsserver.systems.event.Event;
import io.mindspce.outerfieldsserver.systems.event.EventType;
import io.mindspice.mindlib.util.JsonUtils;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;


public class PlayerSession extends Component<PlayerSession> {
    private static final ObjectWriter msgWriter = JsonUtils.getMapper().writerFor(NetMessage.class);

    public WebSocketSession session;
    public BiConsumer<PlayerSession, BinaryMessage> messageOutConsumer;

    public PlayerSession(Entity parentEntity, WebSocketSession session,
            BiConsumer<PlayerSession, BinaryMessage> msgOutConsumer) {
        super(parentEntity, ComponentType.PLAYER_SESSION, List.of());
        this.session = session;
        this.messageOutConsumer = msgOutConsumer;
        registerListener(EventType.PLAYER_RECONNECT, PlayerSession::onReconnection);
    }

    public void onReconnection(Event<WebSocketSession> event) {
        this.session = event.data();
    }

    public boolean isConnected() {
        return session.isOpen();
    }

    public void onEntityUpdateOut(byte[] packet) {
        messageOutConsumer.accept(this, new BinaryMessage(packet));
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
