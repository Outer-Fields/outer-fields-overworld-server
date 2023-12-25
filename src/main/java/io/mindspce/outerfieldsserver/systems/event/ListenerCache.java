package io.mindspce.outerfieldsserver.systems.event;

import io.mindspice.mindlib.data.tuples.Pair;

import java.util.BitSet;
import java.util.EnumMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;public class ListenerCache<T> {

    BitSet listeningFor = new BitSet(EventType.values().length);
    EnumMap<EventType, BiConsumer<T, Event<?>>> consumers = new EnumMap<>(EventType.class);

    public ListenerCache() { }

    public ListenerCache(List<Pair<EventType, BiConsumer<T, Event<?>>>> listeners) {
        listeners.forEach(e -> {
            consumers.put(e.first(), e.second());
            listeningFor.set(e.first().ordinal());
        });
    }

    public boolean isListenerFor(EventType eventType) {
        return listeningFor.get(eventType.ordinal());
    }

    public <E> void addListener(EventType eventType, BiConsumer<T, Event<E>> handler) {
        @SuppressWarnings("unchecked")
        BiConsumer<T, Event<?>> castedHandler = (BiConsumer<T, Event<?>>) (Object) handler;
        consumers.put(eventType, castedHandler);
    }

    public void removeListener(EventType eventType) {
        consumers.remove(eventType);
    }

    public boolean enableListener(EventType eventType) {
        if (!consumers.containsKey(eventType)) {
            return false;
        }
        listeningFor.set(eventType.ordinal());
        return true;
    }

    public void disableListenerFor(EventType eventType) {
        listeningFor.set(eventType.ordinal(), false);
    }

    public void handleEvent(T selfInstance, Event<?> event) {
        BiConsumer<T, Event<?>> consumer = consumers.get(event.eventType());
        if (consumer != null) {
            consumer.accept(selfInstance, event);
        }
    }

}
