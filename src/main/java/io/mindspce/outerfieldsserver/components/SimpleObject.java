package io.mindspce.outerfieldsserver.components;

import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.enums.ComponentType;
import io.mindspce.outerfieldsserver.systems.event.EventType;

import java.util.List;


public class SimpleObject<T> extends Component<SimpleObject<T>> {
    private final T object;

    public SimpleObject(Entity parentEntity, T object, List<EventType> emittedEvents) {
        super(parentEntity, ComponentType.SIMPLE_OBJECT, emittedEvents);
        this.object = object;
    }

    public T object() {
        return object;
    }
}
