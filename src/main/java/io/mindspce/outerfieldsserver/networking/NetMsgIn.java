package io.mindspce.outerfieldsserver.networking;

import io.mindspce.outerfieldsserver.networking.incoming.NetMessageIn;

import java.util.HashMap;


public enum NetMsgIn {
    CLIENT_POSITION((byte) 0),
    ;

    private final byte value;
    private static final NetMsgIn[] convArr;

    static {
        convArr = new NetMsgIn[256];
        for (NetMsgIn msg : NetMsgIn.values()) {
            convArr[msg.value & 0xFF] = msg;
        }
    }

    NetMsgIn(byte value) {
        this.value = value;
    }

    public static NetMsgIn convert(byte value) {
        return convArr[value & 0xFF];
    }
}


