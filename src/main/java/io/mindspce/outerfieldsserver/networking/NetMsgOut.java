package io.mindspce.outerfieldsserver.networking;

public enum  NetMsgOut {
    ENTITY_UPDATE((byte) 0),
    POS_AUTH_CORRECTION((byte) 1);


    public final byte value;

    NetMsgOut(byte value) { this.value = value; }
}
