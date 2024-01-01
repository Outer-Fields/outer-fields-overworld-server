package io.mindspce.outerfieldsserver.components;

import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.enums.ComponentType;
import io.mindspce.outerfieldsserver.systems.event.Event;
import io.mindspce.outerfieldsserver.systems.event.EventType;

import java.util.List;


public class SimpleEmitter extends Component<SimpleEmitter> {
    public SimpleEmitter(Entity parentEntity, List<EventType> emittedEvents) {
        super(parentEntity, ComponentType.SIMPLE_EMITTER, emittedEvents);
    }

    @Override
    public void emitEvent(Event<?> event) {
        super.emitEvent(event);
    }
}
