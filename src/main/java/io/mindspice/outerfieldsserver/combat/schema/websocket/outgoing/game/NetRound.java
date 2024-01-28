package io.mindspice.outerfieldsserver.combat.schema.websocket.outgoing.game;

import io.mindspice.outerfieldsserver.combat.schema.websocket.outgoing.NetMsg;
import io.mindspice.outerfieldsserver.combat.schema.websocket.outgoing.OutMsgType;


public class NetRound extends NetMsg {
    public final int round;
    public NetRound(int round) {
        super(OutMsgType.ROUND_UPDATE, true);
        this.round = round;
    }
}
