package io.mindspce.outerfieldsserver.enums;

import io.mindspce.outerfieldsserver.area.AreaEntity;


public enum AreaId {
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
