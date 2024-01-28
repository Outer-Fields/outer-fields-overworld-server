package io.mindspice.outerfieldsserver.core.systems;

import io.mindspice.outerfieldsserver.core.singletons.EntityManager;
import io.mindspice.outerfieldsserver.enums.SystemType;
import io.mindspice.outerfieldsserver.systems.event.SystemListener;


public class PlayerSystem extends SystemListener {

    public PlayerSystem(int id) {
        super(id, SystemType.PLAYER, true);
    }
}


