package io.mindspice.outerfieldsserver.enums;

import io.mindspice.outerfieldsserver.entities.AreaEntity;


public enum  AreaId {
    AREA_KEY(-3),
    NONE(-2),
    GLOBAL(-1),
    TEST(0),
    INVENTORY(1);

    public final int value;
    public int entityId;
    public AreaEntity areaEntity;

    AreaId(int value) { this.value = value; }

    public void setEntityId(int id) {
        entityId = id;
    }

    public void setAreaEntity(AreaEntity areaEntity) {
        this.areaEntity = areaEntity;
    }
}
