package io.mindspce.outerfieldsserver.ai.task;

import io.mindspce.outerfieldsserver.core.Tick;
import io.mindspce.outerfieldsserver.systems.event.Event;
import io.mindspce.outerfieldsserver.systems.event.EventType;
import io.mindspice.mindlib.data.wrappers.MutableBoolean;
import io.mindspice.mindlib.functional.consumers.TriConsumer;
import io.mindspice.mindlib.functional.predicates.BiPredicateFlag;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;


public record SubTask<T>(
        T data,
        EventType eventType,
        Consumer<T> onStartConsumer,
        BiConsumer<T, Event<?>> onEventConsumer,
        BiPredicateFlag<T, Event<?>> eventPredicate,
        BiConsumer<T, Tick> onTickConsumer,
        BiPredicateFlag<T, Tick> tickPredicate,
        boolean suspendable,
        MutableBoolean suspended
) {

    public SubTask {
        if ((eventType == null) != (onEventConsumer == null)) {
            throw new IllegalStateException("Must have either event type and consumer, or neither");
        }
    }

    public void onStart() {
        if (onStartConsumer() != null) {
            onStartConsumer.accept(data());
        }
    }

    public void suspend(){
        suspended.setTrue();
    }

    public void unsuspend(){
        suspended.setFalse();
    }

    public static <T> SubTask<T> ofOnEvent(
            T data,
            EventType eventType,
            Consumer<T> onStartConsumer,
            BiConsumer<T, Event<?>> eventConsumer,
            BiPredicate<T, Event<?>> eventPredicate,
            boolean suspendable) {
        return new SubTask<>(data, eventType, onStartConsumer, eventConsumer, BiPredicateFlag.of(eventPredicate),
                null, null, suspendable, MutableBoolean.of(false));
    }

    public static <T> SubTask<T> ofOnTick(
            T data,
            Consumer<T> onStartConsumer,
            BiConsumer<T, Tick> tickConsumer,
            BiPredicate<T, Tick> tickPredicate,
            boolean suspendable) {
        return new SubTask<>(data, null, onStartConsumer, null, null,
                tickConsumer, BiPredicateFlag.of(tickPredicate), suspendable, MutableBoolean.of(false));
    }

    public static <T> SubTask<T> ofEventAndTick(
            T data,
            EventType eventType,
            Consumer<T> onStartConsumer,
            BiConsumer<T, Event<?>> eventConsumer,
            BiPredicate<T, Event<?>> eventPredicate,
            BiConsumer<T, Tick> tickConsumer,
            BiPredicate<T, Tick> tickPredicate,
            boolean suspendable) {
        return new SubTask<>(data, eventType, onStartConsumer, eventConsumer, BiPredicateFlag.of(eventPredicate),
                tickConsumer, BiPredicateFlag.of(tickPredicate), suspendable, MutableBoolean.of(false));
    }

    public void onEvent(Event<?> event) {
        if (suspended.get()) { return; }
        if (!eventPredicate.confirmed() && eventPredicate.test(data, event)) {
            onEventConsumer.accept(data, event);
        }
    }

    public void onTick(Tick tick) {
        if (onTickConsumer == null) { return; }
        if (suspended.get()) { return; }
        if (!tickPredicate.confirmed() && tickPredicate.test(data, tick)) {
            onTickConsumer.accept(data, tick);
        }
    }

    public boolean completed() {
        return ((eventPredicate == null || eventPredicate.confirmed())
                && (tickPredicate == null || tickPredicate.confirmed()));
    }

    public boolean hasEventListener() {
        return onTickConsumer != null;
    }

    public void linkEventListener(TriConsumer<EventType, Consumer<?>, Boolean> linkHandle) {
        if (onTickConsumer == null) { return; }
        linkHandle.accept(eventType, (Event<?> event) -> onEventConsumer.accept(data, event), false);
    }

    public void unLinkEventListener(Consumer<EventType> linkHandle) {
        if (eventType == null) { return; }
        linkHandle.accept(eventType);
    }


}
