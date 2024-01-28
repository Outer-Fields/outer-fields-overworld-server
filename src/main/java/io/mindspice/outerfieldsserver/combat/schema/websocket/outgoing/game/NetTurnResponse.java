package io.mindspice.outerfieldsserver.combat.schema.websocket.outgoing.game;

import io.mindspice.outerfieldsserver.combat.enums.*;
import io.mindspice.outerfieldsserver.combat.schema.websocket.outgoing.NetMsg;
import io.mindspice.outerfieldsserver.combat.schema.websocket.outgoing.OutMsgType;

import java.util.List;
import java.util.Map;


public class NetTurnResponse extends NetMsg {
    public boolean is_invalid = false;
    public boolean is_failed = false;
    public PawnIndex action_pawn;
    public List<PawnResponse> affected_pawns_player;
    public List<PawnResponse> affected_pawns_enemy;
    public Map<StatType, Integer> cost;
    public String animation;
    public PlayerAction card_slot;
    public InvalidMsg invalid_msg;


    public NetTurnResponse(boolean isPlayer) {
        super(OutMsgType.TURN_RESPONSE, isPlayer);
    }
}
