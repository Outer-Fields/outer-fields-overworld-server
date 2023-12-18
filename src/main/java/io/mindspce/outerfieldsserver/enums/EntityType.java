package io.mindspce.outerfieldsserver.enums;

public enum EntityType {
    PLAYER((byte) 0),
    NON_PLAYER((byte) 1),
    ITEM((byte) 2),
    LOCATION((byte) 3);

    public final byte value;

    EntityType(byte value) { this.value = value; }
}
