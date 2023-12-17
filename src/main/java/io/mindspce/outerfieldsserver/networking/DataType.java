package io.mindspce.outerfieldsserver.networking;

public enum DataType {
    ENTITY_UPDATE((byte) 0),
    NEW_CHARACTER((byte) 1),
    NEW_ITEM((byte) 2),
    NEW_LOCATION((byte) 3);

    public final byte value;

    DataType(byte value) { this.value = value; }
}
