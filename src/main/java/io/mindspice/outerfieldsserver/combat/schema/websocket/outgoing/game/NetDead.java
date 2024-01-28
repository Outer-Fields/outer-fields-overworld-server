package io.mindspice.outerfieldsserver.combat.schema.websocket.outgoing.game;

import io.mindspice.outerfieldsserver.combat.enums.PawnIndex;
import io.mindspice.outerfieldsserver.combat.schema.websocket.outgoing.NetMsg;
import io.mindspice.outerfieldsserver.combat.schema.websocket.outgoing.OutMsgType;


public class NetDead extends NetMsg {
    public  PawnIndex pawn_index;

    public NetDead(PawnIndex pawn_index, boolean isOwnPawn) {
        super(OutMsgType.DEAD, isOwnPawn);
        this.pawn_index = pawn_index;
    }
}
