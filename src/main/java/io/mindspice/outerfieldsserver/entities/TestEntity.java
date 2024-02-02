package io.mindspice.outerfieldsserver.entities;

import io.mindspice.outerfieldsserver.entities.Entity;
import io.mindspice.outerfieldsserver.enums.AreaId;
import io.mindspice.outerfieldsserver.enums.EntityType;


public  class TestEntity extends Entity {

    public TestEntity(int id, EntityType entityType, AreaId areaId) {
        super(id, entityType, areaId);
    }
}
