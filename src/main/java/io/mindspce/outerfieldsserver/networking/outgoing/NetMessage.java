package io.mindspce.outerfieldsserver.networking.outgoing;

import io.mindspce.outerfieldsserver.enums.NetMsgType;


public class NetMessage<T> {
    public int mt;
    public T d;

    public NetMessage(NetMsgType msgType, T data) {
        this.mt = msgType.value;
        this.d = data;
    }
}
