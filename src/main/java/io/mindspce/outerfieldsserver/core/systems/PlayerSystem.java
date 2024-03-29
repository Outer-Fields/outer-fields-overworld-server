package io.mindspce.outerfieldsserver.core.systems;

import io.mindspce.outerfieldsserver.core.singletons.EntityManager;
import io.mindspce.outerfieldsserver.entities.PlayerEntity;
import io.mindspce.outerfieldsserver.enums.ComponentType;
import io.mindspce.outerfieldsserver.enums.SystemType;
import io.mindspce.outerfieldsserver.systems.event.SystemListener;

import java.util.Objects;


public class PlayerSystem extends SystemListener {

    public PlayerSystem() {
        super(SystemType.PLAYER, true);
        EntityManager.GET().registerSystem(this);
    }
}


