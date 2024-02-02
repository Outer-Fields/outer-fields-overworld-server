package io.mindspice.outerfieldsserver.combat.schema.websocket.outgoing.lobby;

import io.mindspice.outerfieldsserver.data.PawnSet;
import io.mindspice.outerfieldsserver.combat.schema.websocket.outgoing.OutMsgType;

import java.util.Map;


public record NetPawnSetUpdate(
        OutMsgType msg_type,
        Map<Integer, PawnSet> pawn_sets
) {
    public NetPawnSetUpdate(Map<Integer, PawnSet> pawnSets) {
        this(OutMsgType.PAWN_SET_UPDATE, pawnSets);
    }
}
