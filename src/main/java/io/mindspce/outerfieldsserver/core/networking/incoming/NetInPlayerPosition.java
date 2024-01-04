package io.mindspce.outerfieldsserver.core.networking.incoming;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public record NetInPlayerPosition(
        int playerId,
        int x,
        int y,
        long timestamp
) {
    public static NetInPlayerPosition fromBytes(int playerId, ByteBuffer bytes) {
        bytes.order(ByteOrder.LITTLE_ENDIAN);
        return new NetInPlayerPosition(
                playerId,
                (int) bytes.getLong(),
                (int) bytes.getLong(),
                System.currentTimeMillis());
    }
}
