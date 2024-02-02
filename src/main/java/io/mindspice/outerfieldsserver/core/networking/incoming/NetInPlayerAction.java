package io.mindspice.outerfieldsserver.core.networking.incoming;

import io.mindspice.outerfieldsserver.core.networking.proto.EntityProto;
import io.mindspice.outerfieldsserver.enums.NetAction;


public record NetInPlayerAction(
        int focusId,
        NetAction actionKey,
        int value,
        int amount
) {
    public static NetInPlayerAction fromProto(EntityProto.Action actionProto) {
        return new NetInPlayerAction(
                actionProto.getId(),
                NetAction.fromValue(actionProto.getKey()),
                actionProto.getValue(),
                actionProto.getAmount()
        );
    }

    public NetInPlayerAction {
        if (actionKey == null) {
            throw new IllegalStateException("Action cannot be null");
        }
    }

}