package io.mindspce.outerfieldsserver.core;

import com.fasterxml.jackson.databind.ObjectWriter;
import io.mindspce.outerfieldsserver.networking.outgoing.NetMessage;
import io.mindspice.mindlib.util.JsonUtils;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;


public class ServerConst {
    static {
        JsonUtils.setFailOnUnknownProperties(true);
    }

    private static final AtomicInteger currEntityIdIndex = new AtomicInteger(1);
    public static final long NANOS_IN_SEC = 1_000_000_000;
    public static final int NANOS_IN_MSEC = 1_000_000;

    public static int getNewEntityId() {
        return currEntityIdIndex.getAndIncrement();
    }
}
