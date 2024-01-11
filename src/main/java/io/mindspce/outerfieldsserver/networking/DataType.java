package io.mindspce.outerfieldsserver.networking;

public enum DataType {
    NEW_ENTITY((byte) 0),
    ENTITY_POSITION((byte) 1),
    NEW_ITEM((byte) 2),
    NEW_LOCATION((byte) 3);

    public final byte value;

    DataType(byte value) { this.value = value; }
}
