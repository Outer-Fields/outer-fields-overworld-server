package io.mindspce.outerfieldsserver.entities.player;

import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.entities.OutFit;
import io.mindspce.outerfieldsserver.enums.EntityType;


public abstract class PlayerEntity extends Entity {
    private final int playerId;
    private volatile OutFit outfit = new OutFit();

    public PlayerEntity(int playerId) {
        super(EntityType.PLAYER);
        this.playerId = playerId;
    }

    public int playerId() {
        return playerId;
    }

    public OutFit outfit() {
        return outfit;
    }

}
