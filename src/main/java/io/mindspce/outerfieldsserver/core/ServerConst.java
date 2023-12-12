package io.mindspce.outerfieldsserver.core;

import com.fasterxml.jackson.databind.ObjectWriter;
import io.mindspce.outerfieldsserver.networking.outgoing.NetMessage;
import io.mindspice.mindlib.util.JsonUtils;


public class ServerConst {
    static {
        JsonUtils.setFailOnUnknownProperties(true);
    }

    public static final long NANOS_IN_SEC = 1_000_000_000;
    public static final int NANOS_IN_MSEC = 1_000_000;
}
