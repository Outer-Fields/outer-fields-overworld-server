package io.mindspice.outerfieldsserver.systems.event;

import io.mindspice.outerfieldsserver.core.Tick;
import io.mindspice.outerfieldsserver.enums.AreaId;
import io.mindspice.outerfieldsserver.enums.EntityType;

import java.util.List;


public interface EventListener<T extends EventListener<T>> {
    void onEvent(Event<?> event);

    void onTick(Tick tickEvent);

    boolean isListenerFor(EventType eventType);

    List<EventType> emittedEvents();

    List<EventType> hasInputHooksFor();

    List<EventType> hasOutputHooksFor();

    boolean isListening();

    boolean isOnTick();

    public int entityId();

    String componentName();

    String entityName();

    long componentId();

    AreaId areaId();

    EntityType entityType();

}
