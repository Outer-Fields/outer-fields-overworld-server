package io.mindspce.outerfieldsserver.systems.event;

import io.mindspce.outerfieldsserver.core.Tick;
import io.mindspce.outerfieldsserver.enums.AreaId;
import io.mindspce.outerfieldsserver.enums.EntityType;
import io.mindspce.outerfieldsserver.enums.QueryType;
import io.mindspce.outerfieldsserver.systems.EventData;

import java.util.List;
import java.util.function.Consumer;


public interface EventListener<T extends EventListener<T>> {
    void onEvent(Event<?> event);

    void onTick(Tick tickEvent);

    void onQuery(Event<EventData.Query<?, ?, ?>> queryEvent);

    boolean isListenerFor(EventType eventType);

    boolean isQueryableFor(QueryType queryType);

    List<EventType> emittedEvents();

    List<EventType> hasInputHooksFor();

    List<EventType> hasOutputHooksFor();

    boolean isListening();

    boolean isOnTick();

    public int entityId();

    String name();

    long componentId();

    AreaId areaId();

    EntityType entityType();


}
