package io.mindspce.outerfieldsserver.components;

import io.mindspce.outerfieldsserver.components.subcomponents.AreaMonitor;
import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.enums.ComponentType;
import io.mindspce.outerfieldsserver.systems.event.*;


public abstract class Component<T extends Component<T>> implements TickListener, EventListener<T> {
    protected final Entity parentEntity;
    protected final ComponentType componentType;
    protected final ListenerCache<T> listenerCache = new ListenerCache<>();

    protected Component(Entity parentEntity, ComponentType componentType) {
        this.parentEntity = parentEntity;
        this.componentType = componentType;
    }

    public Entity parentEntity() {
        return parentEntity;
    }

    public ComponentType componentType() {
        return componentType;
    }
}