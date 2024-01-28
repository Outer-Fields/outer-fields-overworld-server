package io.mindspice.outerfieldsserver.networking.incoming;

import io.mindspice.outerfieldsserver.networking.NetMsgIn;

import java.nio.ByteBuffer;


public record NetMessageIn(
        long timestamp,
        int pid,
        int entityId,
        NetMsgIn type,
        ByteBuffer data
) { }
