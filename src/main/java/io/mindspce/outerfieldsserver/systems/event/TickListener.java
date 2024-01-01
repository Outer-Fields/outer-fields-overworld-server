package io.mindspce.outerfieldsserver.systems.event;

import io.mindspce.outerfieldsserver.core.Tick;


public interface TickListener {
    void onTick(Tick tick);
}
