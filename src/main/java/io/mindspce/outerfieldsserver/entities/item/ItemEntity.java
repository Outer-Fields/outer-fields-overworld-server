package io.mindspce.outerfieldsserver.entities.item;

import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.enums.EntityType;


public abstract class ItemEntity extends Entity {
    private final int key;

    public ItemEntity(int id, int key) {
        super(id, EntityType.AREA);
        this.key = key;
    }

    public int key() {
        return key;
    }
}
