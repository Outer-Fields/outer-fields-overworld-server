package io.mindspce.outerfieldsserver.entities.nonplayer;

import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.entities.OutFit;
import io.mindspce.outerfieldsserver.enums.EntityType;


public abstract class NonPlayerEntity extends Entity {
    private String name = "";
    private OutFit outfit = new OutFit();

    public NonPlayerEntity(int id) {
        super(id, EntityType.NON_PLAYER);
    }

    public String name() {
        return name;
    }

    public OutFit outfit() {
        return outfit;
    }

}
