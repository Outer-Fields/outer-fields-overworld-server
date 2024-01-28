package io.mindspice.outerfieldsserver.combat.schema.websocket.outgoing.game;

import io.mindspice.outerfieldsserver.combat.gameroom.MatchResult;
import io.mindspice.outerfieldsserver.combat.schema.websocket.outgoing.NetMsg;
import io.mindspice.outerfieldsserver.combat.schema.websocket.outgoing.OutMsgType;


public class NetGameOver extends NetMsg {
    public final MatchResult.EndFlag endFlag;
    public final String reward;


    public NetGameOver(boolean isPlayer, MatchResult.EndFlag endFlag, String reward) {
        super(OutMsgType.GAME_OVER, isPlayer);
        this.endFlag = endFlag;
        this.reward = reward;
    }
}
