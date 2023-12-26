package io.mindspce.outerfieldsserver.entities.player;

import io.mindspce.outerfieldsserver.components.Component;
import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.entities.OutFit;
import io.mindspce.outerfieldsserver.enums.EntityType;

import java.util.List;


public abstract class PlayerEntity extends Entity {
    private final int playerId;
    private volatile OutFit outfit = new OutFit();

    public PlayerEntity(int entityId, int playerId) {
        super(entityId, EntityType.PLAYER);
        this.playerId = playerId;
    }

    public PlayerEntity(int entityId, int playerId, List<Component<?>> components) {
        super(entityId, EntityType.PLAYER, components);
        this.playerId = playerId;
    }

    public int playerId() {
        return playerId;
    }

    public OutFit outfit() {
        return outfit;
    }

}
