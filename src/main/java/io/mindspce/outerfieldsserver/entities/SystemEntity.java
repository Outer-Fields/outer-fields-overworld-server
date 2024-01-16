package io.mindspce.outerfieldsserver.entities;

import io.mindspce.outerfieldsserver.enums.AreaId;
import io.mindspce.outerfieldsserver.enums.EntityType;
import io.mindspce.outerfieldsserver.enums.SystemType;


public class SystemEntity extends Entity{
    private final SystemType systemType;
    public SystemEntity(int id, SystemType systemType) {
        super(id, EntityType.SYSTEM_ENTITY, AreaId.NONE);
        this.systemType = systemType;
    }

    public SystemType systemType() {
        return systemType;
    }
}
