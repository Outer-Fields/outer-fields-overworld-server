package io.mindspce.outerfieldsserver.enums;

public enum EntityType {
    PLAYER((byte)0),
    NON_PLAYER((byte)2),
    ITEM((byte)3),
    LOCATION((byte)4);

    public final byte value;

    EntityType(byte value) { this.value = value; }
}
