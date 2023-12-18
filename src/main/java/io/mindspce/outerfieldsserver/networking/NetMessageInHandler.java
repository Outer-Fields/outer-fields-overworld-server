package io.mindspce.outerfieldsserver.networking;

import io.mindspce.outerfieldsserver.entities.player.PlayerState;
import io.mindspce.outerfieldsserver.networking.incoming.NetMessageIn;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.function.Consumer;


public interface NetMessageInHandler {


    default void handleClientPosition(NetMessageIn msg, PlayerState player) {
        ByteBuffer buffer = msg.data();
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        int posX = (int)buffer.getLong();
        int posY = (int)buffer.getLong();
        long timestamp = msg.timestamp();
        player.onPositionUpdate(posX, posY, timestamp);
    }
}
