package io.mindspce.outerfieldsserver.entities.item;

import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.enums.EntityType;
import io.mindspice.mindlib.data.geometry.IVector2;


public abstract class ItemEntity extends Entity {
    private final int key;

    public ItemEntity(int id, int key) {
        super(id, EntityType.ITEM);
        this.key = key;
    }

    public int key() {
        return key;
    }
}
