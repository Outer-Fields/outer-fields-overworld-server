package io.mindspice.outerfieldsserver.enums;

import io.mindspice.outerfieldsserver.entities.AreaEntity;


public enum  AreaId {
    AREA_KEY(-3),
    NONE(-2),
    GLOBAL(-1),
    TEST(0);

    public final int value;
    public int entityId;
    public AreaEntity entity;

    AreaId(int value) { this.value = value; }

    public void setEntityId(int id) {
        entityId = id;
    }

    public void setEntity(AreaEntity entity) {
        this.entity = entity;
    }
}
