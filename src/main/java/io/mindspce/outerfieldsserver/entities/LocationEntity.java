package io.mindspce.outerfieldsserver.entities;

import io.mindspce.outerfieldsserver.components.monitors.AreaMonitor;
import io.mindspce.outerfieldsserver.enums.AreaId;
import io.mindspce.outerfieldsserver.enums.EntityType;
import io.mindspice.mindlib.data.geometry.IRect2;
import io.mindspice.mindlib.data.geometry.IVector2;


public class LocationEntity extends PositionalEntity {
    private final long key;

    public LocationEntity(int id, AreaId areaId, IVector2 position, int key) {
        super(id, EntityType.LOCATION_ENTITY, areaId, position);
        this.key = key;
    }

    public LocationEntity withAreaMonitor(IRect2 monitorArea) {
        AreaMonitor areaMonitor = new AreaMonitor(this, monitorArea);
        addComponent(areaMonitor);
        return this;
    }

    public long locationKey() { return key; }

}
