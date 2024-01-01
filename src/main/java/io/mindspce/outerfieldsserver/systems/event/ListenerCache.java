package io.mindspce.outerfieldsserver.systems.event;

import io.mindspce.outerfieldsserver.core.Tick;
import io.mindspce.outerfieldsserver.core.singletons.EntityManager;
import io.mindspce.outerfieldsserver.enums.QueryType;
import io.mindspce.outerfieldsserver.systems.EventData;
import io.mindspice.mindlib.data.tuples.Pair;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;


public abstract class ListenerCache<T> {

    private BitSet listeningFor = null;
    private BitSet outputHooksFor = null;
    private BitSet inputHooksFor = null;
    private BitSet queryableFor = null;
    private EnumMap<EventType, BiConsumer<T, Event<?>>> listeners = null;
    private EnumMap<EventType, List<Pair<Boolean, Consumer<Event<?>>>>> outputEventHooks = null;
    private EnumMap<EventType, List<Pair<Boolean, Consumer<Event<?>>>>> inputEventHooks = null;
    private EnumMap<QueryType, BiConsumer<T, Event<?>>> queryListeners = null;
    private final List<EventType> emittedEvents;
    private BiConsumer<T, Tick> tickConsumer = null;
    private boolean isListening = false;
    private boolean isOnTick = false;

    public ListenerCache(List<EventType> emittedEvents) {
        this.emittedEvents = emittedEvents;
    }

    public boolean isListenerFor(EventType eventType) {
        if (listeningFor == null) {
            return false;
        }
        return listeningFor.get(eventType.ordinal());
    }

    public void setOnTickConsumer(BiConsumer<T, Tick> tickConsumer) {
        this.tickConsumer = tickConsumer;
        isOnTick = true;
    }

    public <E> void registerOutputHook(EventType eventType, Consumer<E> callback, boolean intercept) {
        if (outputEventHooks == null) {
            outputEventHooks = new EnumMap<>(EventType.class);
            outputEventHooks.put(eventType, new ArrayList<>(1));
            outputHooksFor = new BitSet(EventType.values().length);
        }
        @SuppressWarnings("unchecked")
        // Consumer<Event<?>> castedHandler = (Consumer<Event<?>>) callback;
        Consumer<Event<?>> castedHandler = (Consumer<Event<?>>) callback;
        var hookList = outputEventHooks.get(eventType);
        if (hookList == null) {
            hookList = new ArrayList<>(1);
        }
        outputHooksFor.set(eventType.ordinal());
        hookList.add(Pair.of(intercept, castedHandler));
    }

    public <E> void registerQueryListener(QueryType queryType, BiConsumer<T, Event<E>> handler) {
        if (queryListeners == null) {
            queryListeners = new EnumMap<>(QueryType.class);
            if (queryableFor != null) {
                queryableFor = new BitSet(QueryType.values().length);
            }
        }
        @SuppressWarnings("unchecked")
        BiConsumer<T, Event<?>> castedHandler = (BiConsumer<T, Event<?>>) (Object) handler;

        queryListeners.put(queryType, castedHandler);
        listeningFor.set(queryType.ordinal());
    }

    public <E> void registerInputHook(EventType eventType, Consumer<E> callback, boolean intercept) {
        isListening = true;
        if (inputEventHooks == null) {
            inputEventHooks = new EnumMap<>(EventType.class);
            inputEventHooks.put(eventType, new ArrayList<>(1));
            inputHooksFor = new BitSet(EventType.values().length);
        }
        @SuppressWarnings("unchecked")
        //Consumer<Event<?>> castedHandler = (Consumer<Event<?>>) callback;
        Consumer<Event<?>> castedHandler = (Consumer<Event<?>>) callback;

        var hookList = inputEventHooks.get(eventType);
        if (hookList == null) {
            hookList = new ArrayList<>(1);
        }
        inputHooksFor.set(eventType.ordinal());
        listeningFor.set(eventType.ordinal());
        hookList.add(Pair.of(intercept, castedHandler));
    }

    public <E> void registerListener(EventType eventType, BiConsumer<T, Event<E>> handler) {
        isListening = true;
        if (listeners == null) {
            listeners = new EnumMap<>(EventType.class);
            listeningFor = new BitSet(EventType.values().length);
        }
        @SuppressWarnings("unchecked")
        BiConsumer<T, Event<?>> castedHandler = (BiConsumer<T, Event<?>>) (Object) handler;
        listeners.put(eventType, castedHandler);
        listeningFor.set(eventType.ordinal());
    }

    public void removeListener(EventType eventType) {
        listeners.remove(eventType);
    }

    public boolean enableListener(EventType eventType) {
        if (!listeners.containsKey(eventType)) {
            return false;
        }
        listeningFor.set(eventType.ordinal());
        return true;
    }

    public void disableListenerFor(EventType eventType) {
        listeningFor.set(eventType.ordinal(), false);
    }

    public boolean isQueryableFor(QueryType queryType) {
        return queryableFor.get(queryType.ordinal());
    }

    protected void handleEvent(T selfInstance, Event<?> event) {
        boolean isIntercept = false;
        if (inputEventHooks != null && inputHooksFor.get(event.eventType().ordinal())) {
            var hooks = inputEventHooks.get(event.eventType());
            if (hooks != null) {
                for (var hook : hooks) {
                    hook.second().accept(event);
                    if (hook.first()) {
                        isIntercept = true;
                    }
                }
            }

        }
        if (isIntercept) {
            return;
        }
        //BiConsumer<T, Event<?>> consumer = listeners.get(event.eventType());
        var listener = listeners.get(event.eventType());
        if (listener != null) {
            listener.accept(selfInstance, event);
        }
    }

    protected void handleTick(T selfInstance, Tick tick) {
        if (tickConsumer == null) {
            return;
        }
        tickConsumer.accept(selfInstance, tick);
    }

    protected void handleQuery(T selfInstance, Event<EventData.Query<?, ?, ?>> queryEvent) {
        var listener = queryListeners.get(queryEvent.data().queryType());
        if (listener == null) {
            //TODO log this
            return;
        }
        listener.accept(selfInstance, queryEvent);
    }

    public boolean isOnTick() {
        return isOnTick;
    }

    public void disableListening() {
        isListening = false;
    }

    public List<EventType> hasInputHooksFor() {
        return Arrays.stream(EventType.values()).filter(e -> inputHooksFor.get(e.ordinal())).toList();
    }

    public List<EventType> hasOutputHooksFor() {
        return Arrays.stream(EventType.values()).filter(e -> outputHooksFor.get(e.ordinal())).toList();
    }

    public void enableListening() {
        isListening = true;
    }

    public boolean isListening() {
        return isListening;
    }

    protected void emitEvent(Event<?> event) {
        assert (emittedEvents.contains(event.eventType()));
        boolean isIntercept = false;
        if (null != outputEventHooks && outputHooksFor.get(event.eventType().ordinal())) {
            var hooks = outputEventHooks.get(event.eventType());
            if (hooks != null) {
                for (var hook : hooks) {
                    hook.second().accept(event);
                    if (hook.first()) {
                        isIntercept = true;
                    }
                }
            }
        }
        if (isIntercept) {
            return;
        }
        EntityManager.GET().emitEvent(event);
    }

    protected void emitEvent(List<Event<?>> events) {
        events.forEach(this::emitEvent);
    }

    public List<EventType> getAllListeningFor() {
        if (listeningFor == null) { return List.of(); }
        return Arrays.stream(EventType.values()).filter(e -> listeningFor.get(e.ordinal())).toList();
    }

    public List<QueryType> getAllQueryableFor() {
        if (queryableFor == null) { return List.of(); }
        return Arrays.stream(QueryType.values()).filter(q -> queryableFor.get(q.ordinal())).toList();
    }

    public List<EventType> emittedEvents() {
        return emittedEvents;
    }

    public List<EventType> getInputHooksFor() {
        return Arrays.stream(EventType.values()).filter(e -> inputHooksFor.get(e.ordinal())).toList();
    }

    public List<EventType> getOutputHooksFor() {
        if (inputHooksFor == null) { return List.of(); }
        return Arrays.stream(EventType.values()).filter(e -> outputHooksFor.get(e.ordinal())).toList();
    }


}