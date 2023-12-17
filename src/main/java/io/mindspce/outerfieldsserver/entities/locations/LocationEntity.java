package io.mindspce.outerfieldsserver.entities.locations;

import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.enums.EntityType;
import io.mindspice.mindlib.data.geometry.IVector2;


public abstract class LocationEntity extends Entity {
    protected final int key;

    public LocationEntity(int key) {
        super(EntityType.LOCATION);
        this.key = key;
    }

    public int key() {
        return key;
    }
}
