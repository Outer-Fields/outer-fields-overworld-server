package io.mindspce.outerfieldsserver.systems.event;

import io.mindspce.outerfieldsserver.components.Component;
import io.mindspce.outerfieldsserver.core.Tick;
import io.mindspce.outerfieldsserver.core.singletons.EntityManager;
import io.mindspice.mindlib.data.collections.sets.ByteSet;
import io.mindspice.mindlib.data.tuples.Pair;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;


public abstract class ListenerCache<T> {

    private ByteSet listeningFor = new ByteSet(EventType.values().length);
    private BitSet outputHooksFor = null;
    private BitSet inputHooksFor = null;
    private EnumMap<EventType, BiConsumer<T, Event<?>>> listeners = null;
    private EnumMap<EventType, List<Pair<Boolean, Consumer<Event<?>>>>> outputEventHooks = null;
    private EnumMap<EventType, List<Pair<Boolean, Consumer<Event<?>>>>> inputEventHooks = null;
    private final List<EventType> emittedEvents = new ArrayList<>(List.of(EventType.CALLBACK));
    private BiConsumer<T, Tick> tickConsumer = null;
    private boolean isListening = false;
    private boolean isOnTick = false;

    private Consumer<EventType> onAddEventListener;
    private Consumer<EventType> onRemoveEventListener;

    public void linkSystemUpdates(Consumer<EventType> incConsumer, Consumer<EventType> decConsumer){
        this.onAddEventListener = incConsumer;
        this.onRemoveEventListener = decConsumer;
    }

    public ListenerCache(List<EventType> emittedEvents) {
        this.emittedEvents.addAll(emittedEvents);
        listeningFor.increment(EventType.CALLBACK.ordinal());
    }

    private void incListeningEvent(EventType eventType) {
        if (onAddEventListener != null) {
            onAddEventListener.accept(eventType);
        }
    }

    private void decListenEvent(EventType eventType) {
        if (onRemoveEventListener != null) {
            onRemoveEventListener.accept(eventType);
        }
    }

    public boolean isListenerFor(EventType eventType) {
        return listeningFor.isNonZero(eventType.ordinal());
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
        Consumer<Event<?>> castedHandler = (Consumer<Event<?>>) callback;

        var hookList = outputEventHooks.get(eventType);
        if (hookList == null) {
            hookList = new ArrayList<>(1);
        }
        outputHooksFor.set(eventType.ordinal());
        hookList.add(Pair.of(intercept, castedHandler));
    }

    public <E> void registerInputHook(EventType eventType, Consumer<E> callback, boolean intercept) {
        isListening = true;
        if (inputEventHooks == null) {
            inputEventHooks = new EnumMap<>(EventType.class);
            inputEventHooks.put(eventType, new ArrayList<>(1));
            inputHooksFor = new BitSet(EventType.values().length);
        }
        @SuppressWarnings("unchecked")
        Consumer<Event<?>> castedHandler = (Consumer<Event<?>>) callback;
        var hookList = inputEventHooks.get(eventType);
        hookList.add(Pair.of(intercept, castedHandler));

        inputHooksFor.set(eventType.ordinal());
        listeningFor.increment(eventType.ordinal());
        incListeningEvent(eventType);
    }

    public void clearOutputHooksFor(EventType eventType) {
        outputHooksFor.set(eventType.ordinal(), false);
        outputEventHooks.remove(eventType);
    }

    public void clearInputHooksFor(EventType eventType) {
        inputEventHooks.getOrDefault(eventType, List.of()).forEach(e -> decListenEvent(eventType));
        inputHooksFor.set(eventType.ordinal(), false);
        decListenEvent(eventType);
        inputEventHooks.remove(eventType);
    }

    public <E> void registerListener(EventType eventType, BiConsumer<T, Event<E>> handler) {
        isListening = true;
        if (listeners == null) {
            listeners = new EnumMap<>(EventType.class);
        }
        @SuppressWarnings("unchecked")
        BiConsumer<T, Event<?>> castedHandler = (BiConsumer<T, Event<?>>) (Object) handler;
        listeners.put(eventType, castedHandler);
        listeningFor.increment(eventType.ordinal());
        incListeningEvent(eventType);
    }

    public <E> void unRegisterListener(EventType eventType, BiConsumer<T, Event<E>> handler) {
        if (listeners == null) {
            return;
        }
        listeners.remove(eventType);
        listeningFor.decrement(eventType.ordinal());
    }

//    public void removeListenersOfType(EventType eventType) {
//        listeners.remove(eventType);
//    }
//
//
//    public boolean enableListener(EventType eventType) {
//        if (!listeners.containsKey(eventType)) {
//            return false;
//        }
//        listeningFor.set(eventType.ordinal());
//        return true;
//    }
//
//    public void disableListenerFor(EventType eventType) {
//        listeningFor.set(eventType.ordinal(), false);
//    }

    public void onTick(Tick tickEvent) {
        try {
            @SuppressWarnings("unchecked")
            T self = (T) this;
            handleTick(self, tickEvent);
        } catch (Exception e) {
            //TODO log this
        }
    }

    public void onEvent(Event<?> event) {
        try {
            if (event.eventType() == EventType.CALLBACK) {
                @SuppressWarnings("unchecked")
                Event<Consumer<T>> consumer = (Event<Consumer<T>>) event;
                handleCallBack(consumer);
            } else {
                ;
                @SuppressWarnings("unchecked")
                T self = (T) this;
                handleEvent(self, event);
            }
        } catch (Exception e) {
            //TODO log this
            e.printStackTrace();
        }
    }

    protected void handleEvent(T selfInstance, Event<?> event) {

        boolean isIntercept = false;
        if (inputEventHooks != null && inputHooksFor.get(event.eventType().ordinal())) {
            var hooks = inputEventHooks.get(event.eventType());
            if (hooks != null) {
                for (var hook : hooks) {
                    hook.second().accept(event);
                    ;
                    if (hook.first()) { isIntercept = true; }

                }
            }
        }
        if (isIntercept) { return; }
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

    public void handleCallBack(Event<Consumer<T>> event) {
        try {
            @SuppressWarnings("unchecked")
            T self = (T) this;
            event.data().accept(self);
        } catch (Exception e) {
            e.printStackTrace();
            //TODO log this
        }
    }

    public boolean isOnTick() {
        return isOnTick;
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

    public void disableListening() {
        isListening = false;
    }

    public boolean isListening() {
        return isListening;
    }

    public void emitEvent(Event<?> event) {
        boolean isIntercept = false;
        if (null != outputEventHooks && outputHooksFor.get(event.eventType().ordinal())) {
            var hooks = outputEventHooks.get(event.eventType());
            if (hooks != null) {
                for (var hook : hooks) {
                    hook.second().accept(event);
                    if (hook.first()) { isIntercept = true; }
                }
            }
        }
        if (isIntercept) { return; }
        EntityManager.GET().emitEvent(event);
    }

    protected void emitEvent(List<Event<?>> events) {
        events.forEach(this::emitEvent);
    }

    public List<EventType> getAllListeningFor() {
        if (listeningFor == null) { return List.of(); }
        return Arrays.stream(EventType.values()).filter(e -> listeningFor.isNonZero(e.ordinal())).toList();
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

    public void addEmittedEvents(List<EventType> eventTypes) {
        emittedEvents.addAll(eventTypes);
    }


}