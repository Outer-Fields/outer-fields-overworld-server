package io.mindspce.outerfieldsserver.entities.locations;

import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.enums.EntityType;
import io.mindspice.mindlib.data.geometry.IVector2;


public abstract class LocationEntity extends Entity {
    protected final int key;
    protected final String locationName;

    public LocationEntity(int entityId, int locationKey, String locationName) {
        super(entityId, EntityType.LOCATION);
        this.key = locationKey;
        this.locationName = locationName;
    }

    public int key() {
        return key;
    }
}
