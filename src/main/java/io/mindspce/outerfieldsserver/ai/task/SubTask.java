package io.mindspce.outerfieldsserver.ai.task;

import io.mindspce.outerfieldsserver.core.Tick;
import io.mindspce.outerfieldsserver.systems.event.Event;
import io.mindspce.outerfieldsserver.systems.event.EventType;
import io.mindspice.mindlib.data.wrappers.MutableBoolean;
import io.mindspice.mindlib.functional.consumers.TriConsumer;
import io.mindspice.mindlib.functional.predicates.BiPredicateFlag;

import java.util.function.BiPredicate;
import java.util.function.Consumer;


public record SubTask<T>(
        EventType eventType,
        T data,
        Consumer<T> onStartConsumer,
        BiPredicateFlag<T, Event<?>> completionEventPredicate,
        BiPredicateFlag<T, Tick> completionTickPredicate,
        boolean suspendable,
        MutableBoolean suspended,
        TriConsumer<EventType, Consumer<?>, Boolean> listenerLink,
        Consumer<EventType> listenerUnlink
) {

    public SubTask {
        if ((eventType == null) != (completionEventPredicate == null)) {
            throw new IllegalStateException("Must have either event type and consumer, or neither");
        }
        if (completionEventPredicate == null && completionTickPredicate == null) {

        }
    }

    public void onStart() {
        if (onStartConsumer() != null) {
            onStartConsumer.accept(data());
            linkEventListener();
        }
    }

    public void suspend() {
        suspended.setTrue();
    }

    public void unsuspend() {
        suspended.setFalse();
    }

    public static <T> SubTask<T> ofOnEvent(
            EventType eventType,
            T data,
            Consumer<T> onStartConsumer,
            BiPredicate<T, Event<?>> completionEventPredicate,
            boolean suspendable,
            TriConsumer<EventType, Consumer<?>, Boolean> listenerLink,
            Consumer<EventType> listenerUnlink
    ) {
        return new SubTask<>(eventType, data, onStartConsumer, BiPredicateFlag.of(completionEventPredicate),
                null, suspendable, MutableBoolean.of(false), listenerLink, listenerUnlink);
    }

    public static <T> SubTask<T> ofOnTick(
            T data,
            Consumer<T> onStartConsumer,
            BiPredicate<T, Tick> completionTickPredicate,
            boolean suspendable,
            TriConsumer<EventType, Consumer<?>, Boolean> listenerLink,
            Consumer<EventType> listenerUnlink
    ) {
        return new SubTask<>(null, data, onStartConsumer, null, BiPredicateFlag.of(completionTickPredicate),
                suspendable, MutableBoolean.of(false), listenerLink, listenerUnlink);
    }

    public static <T> SubTask<T> ofEventAndTick(
            EventType eventType,
            T data,
            Consumer<T> onStartConsumer,
            BiPredicate<T, Event<?>> completionEventPredicate,
            BiPredicate<T, Tick> completionTickPredicate,
            boolean suspendable,
            TriConsumer<EventType, Consumer<?>, Boolean> listenerLink,
            Consumer<EventType> listenerUnlink
    ) {
        return new SubTask<>(eventType, data, onStartConsumer, BiPredicateFlag.of(completionEventPredicate),
                BiPredicateFlag.of(completionTickPredicate), suspendable, MutableBoolean.of(false), listenerLink, listenerUnlink);
    }

    public void onEvent(Event<?> event) {
        if (suspended.get()) { return; }
        if (!completionEventPredicate.confirmed()) {
            completionEventPredicate.test(data, event);
        }
        if (completed()) {

        }
    }

    public void onTick(Tick tick) {
        if (completionTickPredicate == null || suspended.get()) { return; }
        if (!completionTickPredicate.confirmed()) {
            completionTickPredicate.test(data, tick);
        }
    }

    public boolean completed() {

        if (completionEventPredicate.confirmed()) {
            unLinkEventListener();
        }
        return ((completionEventPredicate == null || completionEventPredicate.confirmed())
                && (completionTickPredicate == null || completionTickPredicate.confirmed()));
    }

    public boolean hasEventListener() {
        return completionEventPredicate != null;
    }

    private void linkEventListener() {
        if (completionEventPredicate == null) { return; }
        listenerLink.accept(eventType, (Event<?> event) -> completionEventPredicate.test(data, event), false);
    }

    private void unLinkEventListener() {
        if (eventType == null) { return; }
        listenerUnlink.accept(eventType);
    }


}
