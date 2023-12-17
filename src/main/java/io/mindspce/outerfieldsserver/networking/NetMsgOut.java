package io.mindspce.outerfieldsserver.networking;

public enum NetMsgOut {
    EntityUpdate((byte) 0);

    public final byte value;

    NetMsgOut(byte value) { this.value = value; }
}
