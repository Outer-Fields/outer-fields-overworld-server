package io.mindspce.outerfieldsserver.networking.outgoing;

import io.mindspce.outerfieldsserver.networking.NetMsgOut;


public class NetMessage<T> {
    public int mt;
    public T d;

    public NetMessage(NetMsgOut msgType, T data) {
        this.mt = msgType.value;
        this.d = data;
    }
}
