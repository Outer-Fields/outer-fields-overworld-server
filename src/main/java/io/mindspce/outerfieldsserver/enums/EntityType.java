package io.mindspce.outerfieldsserver.enums;

public enum EntityType {
    PLAYER(0),
    NON_PLAYER(2),
    ITEM(3),
    LOCATION(4);

    public final int value;

    EntityType(int value) { this.value = value; }
}
