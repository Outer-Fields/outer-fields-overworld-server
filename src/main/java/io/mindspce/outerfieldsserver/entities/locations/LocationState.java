package io.mindspce.outerfieldsserver.entities.locations;

import io.mindspce.outerfieldsserver.enums.AreaId;
import io.mindspice.mindlib.data.geometry.IVector2;


public class LocationState extends LocationEntity {
    public LocationState(int entityId, int key, String locationName) {
        super(entityId, key, locationName);
    }

    @Override
    public IVector2 globalPosition() {
        return null;
    }

    @Override
    public IVector2 priorPosition() {
        return null;
    }

    @Override
    public AreaId currentArea() {
        return null;
    }

    @Override
    public IVector2 chunkIndex() {
        return null;
    }
}
