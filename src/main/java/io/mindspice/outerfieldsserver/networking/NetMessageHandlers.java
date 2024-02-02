package io.mindspice.outerfieldsserver.networking;

import io.mindspice.outerfieldsserver.components.player.PlayerSession;
import io.mindspice.outerfieldsserver.core.networking.incoming.NetInPlayerPosition;
import io.mindspice.outerfieldsserver.core.singletons.EntityManager;
import io.mindspice.outerfieldsserver.networking.incoming.NetMessageIn;
import io.mindspice.outerfieldsserver.systems.event.Event;

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
        NetInPlayerPosition netIn = new NetInPlayerPosition(msg.playerId(), posX, posY, msg.timestamp());
        EntityManager.GET().emitEvent(Event.netInPlayerPosition(msg.entityId(), netIn));
    }
}
