package io.mindspce.outerfieldsserver.systems.event;

import io.mindspce.outerfieldsserver.components.Component;
import io.mindspce.outerfieldsserver.core.Tick;
import io.mindspce.outerfieldsserver.core.singletons.EntityManager;
import io.mindspice.mindlib.data.collections.sets.ByteSet;
import io.mindspice.mindlib.data.tuples.Pair;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;


public abstract class ListenerCache<T extends Component<T>> {

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

    private BiConsumer<EventType, Component<?>> onAddEventListener;
    private BiConsumer<EventType, Component<?>> onRemoveEventListener;

    public void linkSystemUpdates(BiConsumer<EventType, Component<?>> incConsumer, BiConsumer<EventType, Component<?>> decConsumer) {
        this.onAddEventListener = incConsumer;
        this.onRemoveEventListener = decConsumer;
    }

    public ListenerCache(List<EventType> emittedEvents) {
        this.emittedEvents.addAll(emittedEvents);
        listeningFor.increment(EventType.CALLBACK.ordinal());
    }

    private void linkToSystem(EventType eventType, Component<?> component) {
        if (onAddEventListener != null) {
            onAddEventListener.accept(eventType, component);
        }
    }

    private void unlinkFromSystem(EventType eventType, Component<?> component) {
        if (onRemoveEventListener != null) {
            onRemoveEventListener.accept(eventType, component);
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
            outputHooksFor = new BitSet(EventType.values().length);
        }
        @SuppressWarnings("unchecked")
        Consumer<Event<?>> castedHandler = (Consumer<Event<?>>) callback;
        outputEventHooks.computeIfAbsent(eventType, v -> new ArrayList<>(1))
                .add(Pair.of(intercept, castedHandler));
        outputHooksFor.set(eventType.ordinal());
    }

    public <E> void registerInputHook(EventType eventType, Consumer<E> callback, boolean intercept) {
        isListening = true;
        if (inputEventHooks == null) {
            inputEventHooks = new EnumMap<>(EventType.class);
            inputHooksFor = new BitSet(EventType.values().length);
        }

        var hookList = inputEventHooks.computeIfAbsent(eventType, k -> new ArrayList<>(1));
        @SuppressWarnings("unchecked")
        Consumer<Event<?>> castedHandler = (Consumer<Event<?>>) callback;

        hookList.add(Pair.of(intercept, castedHandler));

        inputHooksFor.set(eventType.ordinal());
        listeningFor.increment(eventType.ordinal());
        linkToSystem(eventType, (Component<?>) this);
    }

    public void clearOutputHooksFor(EventType eventType) {
        outputHooksFor.set(eventType.ordinal(), false);
        outputEventHooks.remove(eventType);
    }

    public void clearInputHooksFor(EventType eventType) {
        if (listeningFor.get(eventType.ordinal()) == 1) {
            unlinkFromSystem(eventType, (Component<?>) this);
        }
        inputHooksFor.set(eventType.ordinal(), false);
        inputEventHooks.remove(eventType);
        listeningFor.decrement(eventType.ordinal());
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
        linkToSystem(eventType, (Component<?>) this);
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
            e.printStackTrace();
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
                    if (hook.first()) { isIntercept = true; }
                    System.out.println("accepted event: " + event);

                }
            }
        }

        if (isIntercept) { return; }
        if (listeners == null) {
            // TODO log this as debug
            //System.out.println("null listener for event:" + event + " | " + this);
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
        if (inputHooksFor == null) { return List.of(); }
        return Arrays.stream(EventType.values()).filter(e -> inputHooksFor.get(e.ordinal())).toList();
    }

    public List<EventType> hasOutputHooksFor() {
        if (outputHooksFor == null) { return List.of(); }
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

    public void addEmittedEvents(List<EventType> eventTypes) {
        emittedEvents.addAll(eventTypes);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ListenerCache:\n");
        sb.append("  listeningFor: ").append(listeningFor);
        sb.append("\n");
        sb.append("  outputHooksFor: ").append(outputHooksFor);
        sb.append("\n");
        sb.append("  inputHooksFor: ").append(inputHooksFor);
        sb.append("\n");
        sb.append("  listeners: ").append(listeners);
        sb.append("\n");
        sb.append("  outputEventHooks: ").append(outputEventHooks);
        sb.append("\n");
        sb.append("  inputEventHooks: ").append(inputEventHooks);
        sb.append("\n");
        sb.append("  emittedEvents: ").append(emittedEvents);
        sb.append("\n");
        sb.append("  tickConsumer: ").append(tickConsumer);
        sb.append("\n");
        sb.append("  isListening: ").append(isListening);
        sb.append("\n");
        sb.append("  isOnTick: ").append(isOnTick);
        sb.append("\n");
        sb.append("  onAddEventListener: ").append(onAddEventListener);
        sb.append("\n");
        sb.append("  onRemoveEventListener: ").append(onRemoveEventListener);
        sb.append("\n");
        return sb.toString();
    }
}