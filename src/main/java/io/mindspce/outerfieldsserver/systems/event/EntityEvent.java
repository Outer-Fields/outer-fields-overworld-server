package io.mindspce.outerfieldsserver.systems.event;

import io.mindspce.outerfieldsserver.enums.AreaId;
import io.mindspce.outerfieldsserver.enums.EntityEventType;


public class EntityEvent extends Event<EntityEvent> {
    AreaId areaId;
    int entityId;
    EntityEventType entityEventType;

    public AreaId areaId() {
        return areaId;
    }

    public EntityEventType entityEventType() {
        return entityEventType;
    }

    public int entityId() {
        return entityId;
    }
}
