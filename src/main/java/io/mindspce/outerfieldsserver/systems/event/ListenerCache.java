package io.mindspce.outerfieldsserver.systems.event;

import io.mindspce.outerfieldsserver.components.Component;
import io.mindspice.mindlib.data.tuples.Pair;

import java.util.BitSet;
import java.util.EnumMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;


public class ListenerCache<T extends Component<T>> {
    private final BitSet listeningFor = new BitSet(EventType.values().length);
    private final EnumMap<EventType, List<BiConsumer<T, Event<?>>>> consumers = new EnumMap<>(EventType.class);

    public ListenerCache() { }

    public <E> void addListener(EventType eventType, BiConsumer<T, Event<E>> handler) {
        List<BiConsumer<T, Event<?>>> handlers = consumers.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>());
        @SuppressWarnings("unchecked")
        BiConsumer<T, Event<?>> castedHandler = (BiConsumer<T, Event<?>>) (Object) handler;
        handlers.add(castedHandler);
        listeningFor.set(eventType.ordinal());
    }

    public void handleEvent(T selfInstance, Event<?> event) {
        List<BiConsumer<T, Event<?>>> handlers = consumers.get(event.eventType());
        if (handlers != null) {
            for (BiConsumer<T, Event<?>> handler : handlers) {
                handler.accept(selfInstance, event);
            }
        }
    }

    public <E> void removeListener(EventType eventType, BiConsumer<T, Event<E>> handler) {
        consumers.getOrDefault(eventType, List.of()).remove(handler);
    }

    public boolean isListenerFor(EventType eventType) {
        return listeningFor.get(eventType.ordinal());
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

    public void setAllListing(boolean doListening) {
        if (!doListening) {
            listeningFor.clear();
        } else {
            consumers.keySet().forEach(c -> listeningFor.set(c.ordinal()));
        }
    }


}
