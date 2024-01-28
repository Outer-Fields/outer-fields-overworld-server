package io.mindspice.outerfieldsserver.combat.schema.websocket.outgoing.game;

import io.mindspice.outerfieldsserver.combat.schema.websocket.outgoing.NetMsg;
import io.mindspice.outerfieldsserver.combat.schema.websocket.outgoing.OutMsgType;

import java.time.Instant;


public class NetKeepAlive extends NetMsg {
    public long time = Instant.now().getEpochSecond();
    public NetKeepAlive() {
        super(OutMsgType.KEEP_ALIVE, true);
    }
}
