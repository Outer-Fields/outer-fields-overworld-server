package io.mindspice.outerfieldsserver.combat.schema.websocket.outgoing.lobby;

import io.mindspice.outerfieldsserver.combat.schema.websocket.outgoing.OutMsgType;


public record NetQueueResponse(
        OutMsgType msg_type,
        boolean is_staging,
        String match_id,
        boolean confirmed
) {
    public NetQueueResponse(String matchId) {
        this(OutMsgType.QUEUE_RESPONSE, true, matchId, false);
    }
    public NetQueueResponse(boolean confirmed) {
        this(OutMsgType.QUEUE_RESPONSE, false, "", confirmed);
    }
}
