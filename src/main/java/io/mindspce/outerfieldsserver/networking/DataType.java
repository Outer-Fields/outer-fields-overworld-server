package io.mindspce.outerfieldsserver.networking;

public enum DataType {
    ENTITY_POSITION((byte) 0),
    NEW_ENTITY((byte) 1),
    NEW_ITEM((byte) 2),
    NEW_LOCATION((byte) 3);

    public final byte value;

    DataType(byte value) { this.value = value; }
}
