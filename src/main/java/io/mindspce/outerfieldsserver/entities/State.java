package io.mindspce.outerfieldsserver.entities;

public interface State {

    void onTick(long tickTime, double deltaTime);
}
