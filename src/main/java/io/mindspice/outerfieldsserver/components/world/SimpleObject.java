package io.mindspice.outerfieldsserver.components.world;

import io.mindspice.outerfieldsserver.components.Component;
import io.mindspice.outerfieldsserver.entities.Entity;
import io.mindspice.outerfieldsserver.enums.ComponentType;
import io.mindspice.outerfieldsserver.systems.event.EventType;

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
