package io.mindspice.outerfieldsserver.core;

import io.mindspice.mindlib.util.JsonUtils;

import java.util.concurrent.atomic.AtomicInteger;


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
