package io.mindspce.outerfieldsserver.core.networking.incoming;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.Instant;


public record NetPlayerMovement(
        int playerId,
        int x,
        int y,
        long timestamp
) {
    public static NetPlayerMovement fromBytes(int playerId, ByteBuffer bytes) {
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        return new NetPlayerMovement(
                playerId,
                (int) bytes.getLong(),
                (int) bytes.getLong(),
                System.currentTimeMillis());
    }
}
