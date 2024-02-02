package io.mindspice.outerfieldsserver.entities;

import io.mindspice.mindlib.data.geometry.IRect2;
import io.mindspice.outerfieldsserver.components.world.ActiveEntities;
import io.mindspice.outerfieldsserver.enums.AreaId;
import io.mindspice.outerfieldsserver.enums.EntityType;
import io.mindspice.outerfieldsserver.factory.ComponentFactory;


public class MapEntity extends Entity {
    protected IRect2 areaSize;
    protected final ActiveEntities activeEntities;


    public MapEntity(int id, EntityType entityType, AreaId areaId, IRect2 areaSize) {
        super(id, entityType, areaId);
        this.areaSize = areaSize;
        this.activeEntities = ComponentFactory.addActiveEntities(this, 10); // TODO add size to constructor

    }
}
