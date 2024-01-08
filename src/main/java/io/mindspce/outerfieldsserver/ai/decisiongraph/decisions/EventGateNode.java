package io.mindspce.outerfieldsserver.ai.decisiongraph.decisions;

import io.mindspce.outerfieldsserver.ai.decisiongraph.Node;
import io.mindspce.outerfieldsserver.systems.event.Event;
import io.mindspce.outerfieldsserver.systems.event.EventType;

import java.util.function.Consumer;


public class EventGateNode<T, U> extends Node<T> {
    boolean gateOpen = false;
    final EventType gateEvent;
    final Consumer<U> eventDataConsumer;
    final Consumer<T> onTravelConsumer;

    public EventGateNode(NodeType nodeType, String name, EventType gateEvent, Consumer<U> eventDataConsumer,
            Consumer<T> onTravelConsumer) {
        super(nodeType, name);
        this.gateEvent = gateEvent;
        this.eventDataConsumer = eventDataConsumer;
        this.onTravelConsumer = onTravelConsumer;
    }

    public EventGateNode(NodeType nodeType, String name, EventType gateEvent, Consumer<U> eventDataConsumer) {
        super(nodeType, name);
        this.gateEvent = gateEvent;
        this.eventDataConsumer = eventDataConsumer;
        this.onTravelConsumer = null;
    }

    @Override
    public boolean travel(T focusState) {

        if (!gateOpen) { return false; }
        for (Node<T> node : adjacentNodes) {
            if (node.travel(focusState)) {
                if (onTravelConsumer != null) {
                    onTravelConsumer.accept(focusState);
                }
                return true;
            }
        }
        if (onTravelConsumer != null) {
            onTravelConsumer.accept(focusState);
        }
        return false;
    }

    public void onEvent(Event<U> event) {
        eventDataConsumer.accept(event.data());
    }

    public void setGateState(boolean isOpen) {
        gateOpen = isOpen;
    }

    public void resetGate() {
        gateOpen = false;
    }
}
