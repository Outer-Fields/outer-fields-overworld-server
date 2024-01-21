package io.mindspce.outerfieldsserver.ai.task;

import io.mindspce.outerfieldsserver.core.Tick;
import io.mindspce.outerfieldsserver.systems.event.Event;
import io.mindspce.outerfieldsserver.systems.event.EventType;
import io.mindspice.mindlib.data.wrappers.MutableBoolean;
import io.mindspice.mindlib.functional.consumers.TriConsumer;
import io.mindspice.mindlib.functional.predicates.BiPredicateFlag;
import io.mindspice.mindlib.util.DebugUtils;

import java.util.function.BiPredicate;
import java.util.function.Consumer;


public class SubTask<T> {
    private EventType eventType;
    private T data;
    private Consumer<T> onStartConsumer;
    private BiPredicateFlag<T, Event<?>> completionEventPredicate;
    private BiPredicateFlag<T, Tick> completionTickPredicate;
    private boolean suspendable;
    private boolean suspended;
    private TriConsumer<EventType, Consumer<?>, Boolean> listenerLink;
    private Consumer<EventType> listenerUnlink;

    public SubTask(EventType eventType, T data, Consumer<T> onStartConsumer, BiPredicateFlag<T,
            Event<?>> completionEventPredicate, BiPredicateFlag<T, Tick> completionTickPredicate, boolean suspendable) {

        this.eventType = eventType;
        this.data = data;
        this.onStartConsumer = onStartConsumer;
        this.completionEventPredicate = completionEventPredicate;
        this.completionTickPredicate = completionTickPredicate;
        this.suspendable = suspendable;
        this.suspended = false;
    }

    public void onStart() {
        if (onStartConsumer != null) {
            onStartConsumer.accept(data);
        }
        linkEventListener();
    }

    public void linkListenerHooks(TriConsumer<EventType, Consumer<?>, Boolean> listenerLink, Consumer<EventType> listenerUnlink) {
        this.listenerLink = listenerLink;
        this.listenerUnlink = listenerUnlink;
    }

    public void suspend() {
        suspended = true;
    }

    public void unsuspend() {
        suspended = false;
    }

    public boolean suspendable() {
        return suspendable;
    }

    public boolean suspended() {
        return suspended;
    }

    public static <T> SubTask<T> ofOnEvent(
            EventType eventType,
            T data,
            Consumer<T> onStartConsumer,
            BiPredicate<T, Event<?>> completionEventPredicate,
            boolean suspendable
    ) {
        return new SubTask<>(eventType, data, onStartConsumer, BiPredicateFlag.of(completionEventPredicate),
                null, suspendable);
    }

    public static <T> SubTask<T> ofOnTick(
            T data,
            Consumer<T> onStartConsumer,
            BiPredicate<T, Tick> completionTickPredicate,
            boolean suspendable
    ) {
        return new SubTask<>(null, data, onStartConsumer, null,
                BiPredicateFlag.of(completionTickPredicate), suspendable);
    }

    public static <T> SubTask<T> ofEventAndTick(
            EventType eventType,
            T data,
            Consumer<T> onStartConsumer,
            BiPredicate<T, Event<?>> completionEventPredicate,
            BiPredicate<T, Tick> completionTickPredicate,
            boolean suspendable
    ) {
        return new SubTask<>(eventType, data, onStartConsumer, BiPredicateFlag.of(completionEventPredicate),
                BiPredicateFlag.of(completionTickPredicate), suspendable);
    }

    public void onEvent(Event<?> event) {

        // if (suspended.get()) { return; } still listen on suspend?
        if (!completionEventPredicate.confirmed()) {
            completionEventPredicate.test(data, event);
        }
    }

    public void onTick(Tick tick) {
        if (completionTickPredicate == null || suspended) { return; }
        if (!completionTickPredicate.confirmed()) {
            completionTickPredicate.test(data, tick);
        }
    }

    public void reset() {
        if (completionEventPredicate != null) { completionEventPredicate.reset(); }
        if (completionTickPredicate != null) { completionTickPredicate.reset(); }

    }

    public boolean completed() {
        if (completionEventPredicate != null && completionEventPredicate.confirmed()) {
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
        listenerLink.accept(eventType, (Event<?> event) -> onEvent(event), false);
    }

    private void unLinkEventListener() {
        if (eventType == null) { return; }
        listenerUnlink.accept(eventType);
    }


}
