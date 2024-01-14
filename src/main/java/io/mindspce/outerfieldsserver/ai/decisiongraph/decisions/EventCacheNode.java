package io.mindspce.outerfieldsserver.ai.decisiongraph.decisions;

import io.mindspce.outerfieldsserver.ai.decisiongraph.Node;
import io.mindspce.outerfieldsserver.systems.event.Event;
import io.mindspce.outerfieldsserver.systems.event.EventType;
import io.mindspice.mindlib.functional.consumers.BiPredicatedBiConsumer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;


public class EventCacheNode<T, U> extends Node<T> {
    List<U> eventDataCache = new ArrayList<>();
    final EventType gateEvent;
    final BiPredicatedBiConsumer<T, U> eventDataConsumer;
    final BiConsumer<EventCacheNode<T, U>, T> onTravelConsumer;

    public EventCacheNode(String name, EventType gateEvent,
            BiPredicatedBiConsumer<T, U> eventDataConsumer, BiConsumer<EventCacheNode<T, U>, T> onTravelConsumer
    ) {
        super(NodeType.DECISION, name);
        this.gateEvent = gateEvent;
        this.eventDataConsumer = eventDataConsumer;
        this.onTravelConsumer = onTravelConsumer;
    }

    public static <T, U> EventCacheNode<T, U> of(String name, EventType gateEvent,
            BiPredicatedBiConsumer<T, U> eventDataConsumer, BiConsumer<EventCacheNode<T, U>, T> onTravelConsumer) {
        return new EventCacheNode<>(name, gateEvent, eventDataConsumer, onTravelConsumer);
    }

    public void onEvent(Event<U> event) {
        eventDataCache.add(event.data());
    }

    @Override
    public boolean travel(T focusState) {
        for (var event : eventDataCache) {
            if (eventDataConsumer.predicate().test(focusState, event)) {
                for (Node<T> node : adjacentNodes) {
                    if (node.travel(focusState)) {
                        if (onTravelConsumer != null) {
                            onTravelConsumer.accept(this, focusState);
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
