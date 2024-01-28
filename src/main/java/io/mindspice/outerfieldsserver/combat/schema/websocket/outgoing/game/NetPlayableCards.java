package io.mindspice.outerfieldsserver.combat.schema.websocket.outgoing.game;

import io.mindspice.outerfieldsserver.combat.enums.PlayerAction;
import io.mindspice.outerfieldsserver.combat.schema.websocket.outgoing.NetMsg;
import io.mindspice.outerfieldsserver.combat.schema.websocket.outgoing.OutMsgType;

import java.util.Map;


public class NetPlayableCards extends NetMsg {
    public Map<PlayerAction, Integer> pawn_1;
    public Map<PlayerAction, Integer> pawn_2;
    public Map<PlayerAction, Integer> pawn_3;

    public NetPlayableCards(Map<PlayerAction, Integer> pawn1, Map<PlayerAction, Integer> pawn2,
            Map<PlayerAction, Integer> pawn3) {
        super(OutMsgType.PLAYABLE_CARDS, true);
        this.pawn_1 = pawn1;
        this.pawn_2 = pawn2;
        this.pawn_3 = pawn3;
    }

    public NetPlayableCards() {
        super(OutMsgType.PLAYABLE_CARDS,true);
    }
}
