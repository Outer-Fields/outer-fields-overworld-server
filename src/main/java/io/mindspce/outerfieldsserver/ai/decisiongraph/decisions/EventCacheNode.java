package io.mindspce.outerfieldsserver.ai.decisiongraph.decisions;

import io.mindspce.outerfieldsserver.ai.decisiongraph.Node;
import io.mindspce.outerfieldsserver.systems.event.Event;
import io.mindspce.outerfieldsserver.systems.event.EventType;
import io.mindspice.mindlib.functional.consumers.BiPredicatedBiConsumer;

import java.util.ArrayList;
import java.util.List;


public class EventCacheNode<T, U> extends Node<T> {
    List<U> eventDataCache = new ArrayList<>();
    final EventType gateEvent;
    final BiPredicatedBiConsumer<T, U> eventDataConsumer;

    public EventCacheNode(Node.NodeType nodeType, String name, EventType gateEvent,
            BiPredicatedBiConsumer<T, U> eventDataConsumer) {
        super(nodeType, name);
        this.gateEvent = gateEvent;
        this.eventDataConsumer = eventDataConsumer;
    }

    public void onEvent(Event<U> event) {
        eventDataCache.add(event.data());
    }

    @Override
    public boolean travel(T focusState) {
        for (var event : eventDataCache) {
            if (eventDataConsumer.predicate().test(focusState, event)) {
                for (Node<T> node : adjacentNodes) {
                    if (node.travel(focusState)) { return true; }
                }
            }
        }
        return false;
    }
}
