package io.mindspce.outerfieldsserver.networking;

import io.mindspce.outerfieldsserver.entities.player.PlayerState;
import io.mindspce.outerfieldsserver.networking.incoming.NetMessageIn;

import java.nio.ByteBuffer;
import java.util.function.Consumer;


public interface NetMessageInHandler {


    default void handleClientPosition(NetMessageIn msg, PlayerState player) {
        ByteBuffer buffer = ByteBuffer.wrap(msg.data());
        int posX = buffer.getInt();
        int posY = buffer.getInt();
        long timestamp = msg.timestamp();
        player.onPositionUpdate(posX, posY, timestamp);
    }
}
