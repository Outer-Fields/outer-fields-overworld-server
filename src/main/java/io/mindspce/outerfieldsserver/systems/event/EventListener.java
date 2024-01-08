package io.mindspce.outerfieldsserver.systems.event;

import io.mindspce.outerfieldsserver.components.Component;
import io.mindspce.outerfieldsserver.core.Tick;
import io.mindspce.outerfieldsserver.enums.AreaId;
import io.mindspce.outerfieldsserver.enums.EntityType;

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
