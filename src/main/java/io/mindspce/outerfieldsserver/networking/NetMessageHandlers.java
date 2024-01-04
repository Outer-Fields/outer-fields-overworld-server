package io.mindspce.outerfieldsserver.networking;

import io.mindspce.outerfieldsserver.components.PlayerSession;
import io.mindspce.outerfieldsserver.core.networking.incoming.NetInPlayerPosition;
import io.mindspce.outerfieldsserver.core.singletons.EntityManager;
import io.mindspce.outerfieldsserver.networking.incoming.NetMessageIn;
import io.mindspce.outerfieldsserver.systems.event.Event;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public interface NetMessageHandlers {

    @FunctionalInterface
    interface MessageOutHandler {
        void handleMsgOut(byte[] msg, PlayerSession playerSession);
    }

    default void handleClientPosition(NetMessageIn msg) {
        ByteBuffer buffer = msg.data();
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        int posX = (int) buffer.getLong();
        int posY = (int) buffer.getLong();
        NetInPlayerPosition netIn = new NetInPlayerPosition(msg.pid(), posX, posY, msg.timestamp());
        EntityManager.GET().emitEvent(Event.netInPlayerPosition(msg.entityId(), netIn));
    }
}
