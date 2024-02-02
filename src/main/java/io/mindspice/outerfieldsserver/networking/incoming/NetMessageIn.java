package io.mindspice.outerfieldsserver.networking.incoming;

import java.nio.ByteBuffer;


public record NetMessageIn(
        long timestamp,
        int playerId,
        int entityId,
        ByteBuffer data
) { }
