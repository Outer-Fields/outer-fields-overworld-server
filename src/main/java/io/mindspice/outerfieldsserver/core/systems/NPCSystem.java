package io.mindspice.outerfieldsserver.core.systems;

import io.mindspice.outerfieldsserver.core.singletons.EntityManager;
import io.mindspice.outerfieldsserver.enums.SystemType;
import io.mindspice.outerfieldsserver.systems.event.SystemListener;


public class NPCSystem extends SystemListener {

    public NPCSystem(int id) {
        super(id, SystemType.NPC, true);
    }
}


