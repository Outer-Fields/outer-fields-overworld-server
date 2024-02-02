package io.mindspice.outerfieldsserver.core.networking.incoming;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public record NetInPlayerPosition(
        int playerId,
        int x,
        int y,
        long timestamp
) { }
