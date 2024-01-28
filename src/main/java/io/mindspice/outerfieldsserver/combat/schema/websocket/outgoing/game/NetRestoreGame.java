package io.mindspice.outerfieldsserver.combat.schema.websocket.outgoing.game;

import io.mindspice.outerfieldsserver.combat.schema.websocket.outgoing.NetMsg;
import io.mindspice.outerfieldsserver.combat.schema.websocket.outgoing.OutMsgType;


public class NetRestoreGame extends NetMsg {
    public final String room_id;
    public NetRestoreGame(String roomId) {
        super(OutMsgType.RESTORE_GAME, true);
        room_id = roomId;
    }
}
