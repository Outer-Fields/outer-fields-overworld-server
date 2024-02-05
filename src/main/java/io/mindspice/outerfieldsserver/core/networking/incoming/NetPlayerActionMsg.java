package io.mindspice.outerfieldsserver.core.networking.incoming;

import io.mindspice.outerfieldsserver.core.networking.proto.EntityProto;
import io.mindspice.outerfieldsserver.enums.NetPlayerAction;


public record NetPlayerActionMsg(
        int focusId,
        NetPlayerAction actionKey,
        int value1,
        int value2,
        int amount
) {
    public static NetPlayerActionMsg fromProto(EntityProto.Action actionProto) {
        return new NetPlayerActionMsg(
                actionProto.getFocusId(),
                NetPlayerAction.fromValue(actionProto.getKey()),
                actionProto.getValue1() == 0 ? -1 : actionProto.getValue1(),
                actionProto.getValue2() == 0 ? -1 : actionProto.getValue2(),
                actionProto.getAmount()
        );
    }

    public NetPlayerActionMsg {
        if (actionKey == null) {
            throw new IllegalStateException("Action cannot be null");
        }
    }

}