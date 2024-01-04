package io.mindspce.outerfieldsserver.entities;

import io.mindspce.outerfieldsserver.components.AreaMonitor;
import io.mindspce.outerfieldsserver.enums.AreaId;
import io.mindspce.outerfieldsserver.enums.EntityType;
import io.mindspice.mindlib.data.geometry.IRect2;
import io.mindspice.mindlib.data.geometry.IVector2;


public class LocationEntity extends PositionalEntity {
    private final int locationKey;

    public LocationEntity(int id, EntityType entityType, AreaId areaId, IVector2 position, int locationKey) {
        super(id, EntityType.LOCATION_ENTITY, areaId, position);
        this.locationKey = locationKey;
    }

    public LocationEntity withAreaMonitor(IRect2 monitorArea) {
        AreaMonitor areaMonitor = new AreaMonitor(this, monitorArea);
        addComponent(areaMonitor);
        return this;
    }

    public int locationKey() { return locationKey; }

}
