package io.mindspice.outerfieldsserver.components.primatives;

import io.mindspice.outerfieldsserver.components.Component;
import io.mindspice.outerfieldsserver.entities.Entity;
import io.mindspice.outerfieldsserver.enums.ComponentType;
import io.mindspice.outerfieldsserver.systems.event.Event;
import io.mindspice.outerfieldsserver.systems.event.EventType;

import java.util.List;


public class SimpleEmitter extends Component<SimpleEmitter> {
    public SimpleEmitter(Entity parentEntity, List<EventType> emittedEvents) {
        super(parentEntity, ComponentType.SIMPLE_EMITTER, emittedEvents);
    }

    public void addEmittedEvent(EventType eventType) {
        emittedEvents().add(eventType);
    }

    @Override
    public void emitEvent(Event<?> event) {
        super.emitEvent(event);
    }
}
