package io.mindspce.outerfieldsserver.core.systems;

import io.mindspce.outerfieldsserver.core.singletons.EntityManager;
import io.mindspce.outerfieldsserver.enums.SystemType;
import io.mindspce.outerfieldsserver.systems.event.SystemListener;


public class NPCSystem extends SystemListener {

    public NPCSystem() {
        super(SystemType.NPC, true);
        EntityManager.GET().registerSystem(this);
    }
}


