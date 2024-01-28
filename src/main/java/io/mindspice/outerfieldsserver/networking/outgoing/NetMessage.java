package io.mindspice.outerfieldsserver.networking.outgoing;

import io.mindspice.outerfieldsserver.networking.NetMsgOut;


public class NetMessage<T> {
    public int mt;
    public T d;

    public NetMessage(NetMsgOut msgType, T data) {
        this.mt = msgType.value;
        this.d = data;
    }
}
