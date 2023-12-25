package io.mindspce.outerfieldsserver.components;

import io.mindspce.outerfieldsserver.components.subcomponents.AreaMonitor;
import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.systems.event.*;


public abstract class Component<T extends Component<T>> implements TickListener {
    protected final Entity parentEntity;
    protected final ListenerCache<T> listenerCache = new ListenerCache<>();

    protected Component(Entity parentEntity) { this.parentEntity = parentEntity; }
}