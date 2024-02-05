package io.mindspice.outerfieldsserver.core.systems;

import io.mindspice.outerfieldsserver.enums.SystemType;
import io.mindspice.outerfieldsserver.systems.event.SystemListener;
import io.mindspice.outerfieldsserver.util.Utility;


public class ExternalSystem extends SystemListener {
    public ExternalSystem(int id, SystemType systemType, boolean doStart, long waitNanos) {
        super(id, systemType, doStart, Utility.msToNano(5));
    }
}
