package io.mindspce.outerfieldsserver.systems.event;

public interface TickListener {
    void onTick(long tickTime, double delta);
}
