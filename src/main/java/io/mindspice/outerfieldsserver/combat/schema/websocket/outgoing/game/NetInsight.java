package io.mindspice.outerfieldsserver.combat.schema.websocket.outgoing.game;

import io.mindspice.outerfieldsserver.combat.gameroom.effect.Insight;
import io.mindspice.outerfieldsserver.combat.schema.websocket.outgoing.NetMsg;
import io.mindspice.outerfieldsserver.combat.schema.websocket.outgoing.OutMsgType;

import java.util.List;


public class NetInsight extends NetMsg {
    public final List<Insight> insight;

    public NetInsight(List<Insight> insight) {
        super(OutMsgType.INSIGHT, true);
        this.insight = insight;
    }
}
