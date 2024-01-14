package io.mindspce.outerfieldsserver.ai.decisiongraph.decisions;

import io.mindspce.outerfieldsserver.ai.decisiongraph.Node;
import io.mindspce.outerfieldsserver.systems.event.Event;
import io.mindspce.outerfieldsserver.systems.event.EventType;

import java.util.function.BiConsumer;
import java.util.function.Consumer;


public class EventGateNode<T, U> extends Node<T> {
    boolean gateOpen = false;
    final EventType gateEvent;
    final BiConsumer<EventGateNode<T, U>, U> eventDataConsumer;
    final BiConsumer<EventGateNode<T, U>, T> onTravelConsumer;

    public EventGateNode(String name, EventType gateEvent, BiConsumer<EventGateNode<T, U>, U> eventConsumer,
            BiConsumer<EventGateNode<T, U>, T> onTravelConsumer) {
        super(NodeType.DECISION, name);
        this.gateEvent = gateEvent;
        this.eventDataConsumer = eventConsumer;
        this.onTravelConsumer = onTravelConsumer;
    }

    public EventGateNode(String name, EventType gateEvent, BiConsumer<EventGateNode<T, U>, U> eventDataConsumer) {
        super(NodeType.DECISION, name);
        this.gateEvent = gateEvent;
        this.eventDataConsumer = eventDataConsumer;
        this.onTravelConsumer = null;
    }

    public static <T, U> EventGateNode<T, U> of(String name, EventType gateEvent, BiConsumer<EventGateNode<T, U>, U> eventConsumer,
            BiConsumer<EventGateNode<T, U>, T> onTravelConsumer) {
        return new EventGateNode<>(name, gateEvent, eventConsumer, onTravelConsumer);
    }

    public static <T, U> EventGateNode<T, U> of(String name, EventType gateEvent, BiConsumer<EventGateNode<T, U>, U> eventConsumer) {
        return new EventGateNode<>(name, gateEvent, eventConsumer);
    }

    @Override
    public boolean travel(T focusState) {

        if (!gateOpen) { return false; }
        for (Node<T> node : adjacentNodes) {
            if (node.travel(focusState)) {
                if (onTravelConsumer != null) {
                    onTravelConsumer.accept(this, focusState);
                }
                return true;
            }
        }
        return false;
    }

    public void onEvent(Event<U> event) {
        eventDataConsumer.accept(this, event.data());
    }

    public void setGateState(boolean isOpen) {
        gateOpen = isOpen;
    }

    public void resetGate() {
        gateOpen = false;
    }
}
