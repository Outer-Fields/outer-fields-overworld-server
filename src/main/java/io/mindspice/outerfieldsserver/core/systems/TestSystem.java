package io.mindspice.outerfieldsserver.core.systems;

import io.mindspice.outerfieldsserver.enums.SystemType;
import io.mindspice.outerfieldsserver.systems.event.SystemListener;


public class TestSystem extends SystemListener {
    public TestSystem(int id, SystemType systemType) {
        super(id,systemType, true);

    }
}

