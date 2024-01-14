package io.mindspce.outerfieldsserver.systems.event;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import io.mindspce.outerfieldsserver.components.Component;
import io.mindspce.outerfieldsserver.core.Tick;
import io.mindspce.outerfieldsserver.core.singletons.EntityManager;
import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.enums.AreaId;
import io.mindspce.outerfieldsserver.enums.ComponentType;
import io.mindspce.outerfieldsserver.enums.EntityType;
import io.mindspce.outerfieldsserver.enums.SystemType;
import io.mindspce.outerfieldsserver.systems.EventData;
import io.mindspice.mindlib.data.collections.sets.AtomicBitSet;
import io.mindspice.mindlib.data.tuples.Pair;
import io.mindspice.mindlib.util.DebugUtils;
import org.jctools.queues.MpscUnboundedXaddArrayQueue;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicIntegerArray;


public abstract class SystemListener implements EventListener<SystemListener> {
    // Registries for various O(1) lookups
    private final SystemType systemType;
    private final Map<EventType, List<EventListener<?>>> listenerRegistry = new EnumMap<>(EventType.class);
    private final TIntObjectMap<List<Component<?>>> entityRegistry = new TIntObjectHashMap<>(100);
    private final Map<ComponentType, List<Component<?>>> componentTypeRegistry = new HashMap<>(100);
    private final TLongObjectMap<Component<?>> componentRegistry = new TLongObjectHashMap<>(100);

    // List of onTick components for easy iteration
    private final List<EventListener<?>> tickListeners = new ArrayList<>();

    // Thread safe lookup tables for message delivery
    private final AtomicIntegerArray listeningFor = new AtomicIntegerArray(EventType.values().length);
    private final AtomicBitSet listeningEntities = new AtomicBitSet(EntityManager.GET().entityCount());

    // Queue/Executor
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private volatile boolean running = false;
    private final MpscUnboundedXaddArrayQueue<Event<?>> eventQueue = new MpscUnboundedXaddArrayQueue<>(50);

    public SystemListener(SystemType systemType, boolean doStart) {
        this.systemType = systemType;
        if (doStart) { start(); }
        listeningFor.incrementAndGet(EventType.CALLBACK.ordinal());
        listeningFor.incrementAndGet(EventType.TICK.ordinal());
        listeningFor.incrementAndGet(EventType.COMPLETABLE_EVENT.ordinal());
    }

    @Override
    public String componentName() {
        return systemType.name();
    }

    @Override
    public boolean isListenerFor(EventType eventType) {
        return false;
    }

    @Override
    public int entityId() {
        return 0;
    }

    @Override
    public void onTick(Tick tickEvent) {

    }

    @Override
    public long componentId() {
        return 0;
    }

    public SystemType systemType() {
        return systemType;
    }

    public boolean stop() {
        running = false;
        return drainRemainingQueue();
    }

    public void start() {
        executorService.submit(process());
        running = true;
    }

    public boolean isListeningFor(EventType eventType) {
        return listeningFor.get(eventType.ordinal()) > 0;
    }

    public boolean hasListeningEntity(int entityId) {
        return listeningEntities.get(entityId);
    }

    @Override
    public void onEvent(Event<?> event) {
        eventQueue.add(event);
    }

    private Runnable process() {
        return (() -> {
            while (running) {
                Event<?> nextEvent;
                while ((nextEvent = eventQueue.relaxedPoll()) == null) {
                    Thread.onSpinWait();
                }
                switch (nextEvent.eventType()) {
                    case EventType.TICK -> handleTick((Tick) nextEvent.data());
                    case EventType.CALLBACK -> handleCallBack(nextEvent);
                    case EventType.COMPLETABLE_EVENT -> handleCompletableEvent(nextEvent);
                    default -> {
                        if (nextEvent.isDirect()) {
                            handleDirectEvent(nextEvent);
                        } else {
                            handleEvent(nextEvent);
                        }
                    }
                }
            }
        });
    }

    public boolean drainRemainingQueue() {
        running = false;
        Future<?> future = executorService.submit(() -> eventQueue.drain(this::processQueueItem));
        try {
            future.get();
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (ExecutionException e) {
            return false;
        }
    }

    private void processQueueItem(Event<?> event) {
        if (event.eventType() == EventType.TICK) {
            handleTick((Tick) event.data());
        } else {
            handleEvent(event);
        }
    }

    public void onRegisterComponent(Event<Pair<SystemType, Entity>> event) {
        Entity entity = event.data().second();
        entity.registerWithSystem(this);
    }

    public void registerComponent(Component<?> component) {
        component.setRegisteredWith(systemType);
        componentRegistry.put(component.componentId(), component);
        componentTypeRegistry.computeIfAbsent(component.componentType(), v -> new ArrayList<>()).add(component);

        for (var event : component.getAllListeningFor()) {
            listenerRegistry.computeIfAbsent(event, v -> new ArrayList<>(2)).add(component);
            listeningFor.incrementAndGet(event.ordinal());
        }

        if (component.isOnTick()) { tickListeners.add(component); }
        listeningEntities.set(component.entityId());
        var existingEntity = entityRegistry.get(component.entityId());
        if (existingEntity == null) {
            existingEntity = new ArrayList<>(2);
            entityRegistry.put(component.entityId(), existingEntity);
        }
        existingEntity.add(component);
        component.linkSystemUpdates(this::addListeningEvent, this::removeListeningEvent);
    }

    public void registerComponents(List<Component<?>> components) {
        components.forEach(this::registerComponent);
    }

    // TODO make these handle this mess of tables
    public void removeByComponentId(long componentId) {
        Iterator<Map.Entry<EventType, List<EventListener<?>>>> itr = listenerRegistry.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<EventType, List<EventListener<?>>> listenerList = itr.next();
            listenerList.getValue().removeIf(l -> l.componentId() == componentId);

            if (listenerList.getValue().isEmpty()) {
                listeningFor.incrementAndGet(listenerList.getKey().ordinal());
                itr.remove();
            }
        }

        Iterator<Map.Entry<ComponentType, List<Component<?>>>> itr2 = componentTypeRegistry.entrySet().iterator();
        while (itr2.hasNext()) {
            Map.Entry<ComponentType, List<Component<?>>> componentList = itr2.next();
            componentList.getValue().removeIf(l -> l.componentId() == componentId);

            if (componentList.getValue().isEmpty()) {
                itr2.remove();
            }
        }

        tickListeners.removeIf(l -> l.componentId() == componentId);
    }

    public void removeByEntityId(int entityId) {
        entityRegistry.remove(entityId);
        listeningEntities.set(entityId, false);

        Iterator<Map.Entry<EventType, List<EventListener<?>>>> itr = listenerRegistry.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<EventType, List<EventListener<?>>> listenerList = itr.next();
            listenerList.getValue().removeIf(l -> l.entityId() == entityId);
            if (listenerList.getValue().isEmpty()) {
                listeningFor.decrementAndGet(listenerList.getKey().ordinal());
                itr.remove();
            }
        }

        Iterator<Map.Entry<ComponentType, List<Component<?>>>> itr2 = componentTypeRegistry.entrySet().iterator();
        while (itr2.hasNext()) {
            Map.Entry<ComponentType, List<Component<?>>> componentList = itr2.next();
            componentList.getValue().removeIf(l -> l.entityId() == entityId);
            if (componentList.getValue().isEmpty()) {
                itr2.remove();
            }
        }
        tickListeners.removeIf(l -> l.entityId() == entityId);
    }

    private void handleCallBack(Event<?> event) {
        if (!event.isDirect()) {
            componentTypeRegistry.getOrDefault(event.recipientCompType(), List.of())
                    .forEach(c -> { if (c.entityId() != event.issuerEntityId()) { c.onEvent(event); } });
        } else {
            if (event.isDirectComponent()) {
                Component<?> component = componentRegistry.get(event.recipientComponentId());
                component.onEvent(event);
            } else {
                entityRegistry.get(event.recipientEntityId()).forEach(c -> {
                    if (c.componentType() == event.recipientCompType()) {
                        c.onEvent(event);
                    }
                });
            }
        }
    }

    private void handleTick(Tick tick) {
        for (int i = 0; i < tickListeners.size(); ++i) {
            tickListeners.get(i).onTick(tick);
        }
    }

    private void handleEvent(Event<?> event) {
        List<EventListener<?>> listeners = listenerRegistry.get(event.eventType());
        if (listeners == null) {
            //TODO log this, error state
            return;
        }
        for (int i = 0; i < listeners.size(); ++i) {
            var listener = listeners.get(i);
            if (listener.entityId() == event.issuerEntityId()) { continue; }
            listener.onEvent(event);
        }
    }

    private void handleCompletableEvent(Event<?> event) {
        EventData.CompletableEvent<?, ?> data = (EventData.CompletableEvent<?, ?>) event.data();
        if (data.mainEvent().eventType() == EventType.CALLBACK) {
            handleCallBack(data.mainEvent());
        } else {
            handleEvent(data.mainEvent());
        }
        EntityManager.GET().emitEvent(data.completionEvent());
    }

    private void handleDirectEvent(Event<?> event) {
        if (event.isDirectComponent()) {
            Component<?> component = componentRegistry.get(event.recipientComponentId());
            if (component == null) {
                // TODO log this? might not be error as we dont track entity ids before issuing?
                return;
            }
            component.onEvent(event);
        } else {
            List<Component<?>> listeners = entityRegistry.get(event.recipientEntityId());
            if (listeners == null) {
                // TODO log this? might not be error as we dont track entity ids before issuing?
                return;
            }
            for (int i = 0; i < listeners.size(); ++i) {
                if (listeners.get(i).isListenerFor(event.eventType())) {
                    listeners.get(i).onEvent(event);
                }
            }
        }
    }

    public List<Component<?>> getComponentsByType(ComponentType type) {
        return componentTypeRegistry.get(type);
    }

    public Component<?> getComponent(long componentId) {
        return componentTypeRegistry.values().stream()
                .flatMap(List::stream)
                .filter(ic -> ic.componentId() == componentId)
                .findFirst()
                .orElse(null);
    }

    public Component<?> getComponent(ComponentType type, long componentId) {
        return componentTypeRegistry.getOrDefault(type, List.of()).stream()
                .filter(c -> c.componentId() == componentId)
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<EventType> emittedEvents() {
        return List.of(); // TODO fix this?
    }

    @Override
    public List<EventType> hasInputHooksFor() {
        return List.of();
    }

    @Override
    public List<EventType> hasOutputHooksFor() {
        return List.of();
    }

    @Override
    public boolean isListening() {
        return true; // TODO change this if systems can be toggled
    }

    @Override
    public boolean isOnTick() {
        return true; // TODO change this if ontick can be toggled
    }

    @Override
    public AreaId areaId() {
        return AreaId.GLOBAL;
    }

    @Override
    public EntityType entityType() {
        return EntityType.ANY;
    }

    @Override
    public String entityName() {
        return systemType().name();
    }

    private void addListeningEvent(EventType type) {
        listeningFor.incrementAndGet(type.ordinal());
    }

    private void removeListeningEvent(EventType eventType) {
        var val = listeningFor.decrementAndGet(eventType.ordinal());
        if (val < 0) {
            System.out.println("Decremented listeners below zero, this shouldnt happen.");
            // TODO log this
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SystemListener:\n");
        sb.append("  systemType: ").append(systemType);
        sb.append("\n");
        sb.append("  listenerRegistry: ").append(listenerRegistry);
        sb.append("\n");
        sb.append("  entityRegistry: ").append(entityRegistry);
        sb.append("\n");
        sb.append("  componentTypeRegistry: ").append(componentTypeRegistry);
        sb.append("\n");
        sb.append("  componentRegistry: ").append(componentRegistry);
        sb.append("\n");
        sb.append("  tickListeners: ").append(tickListeners);
        sb.append("\n");
        sb.append("  listeningFor: ").append(listeningFor);
        sb.append("\n");
        sb.append("  listeningEntities: ").append(listeningEntities);
        sb.append("\n");
        sb.append("  executorService: ").append(executorService);
        sb.append("\n");
        sb.append("  running: ").append(running);
        sb.append("\n");
        sb.append("  eventQueue: ").append(eventQueue);
        sb.append("\n");
        return sb.toString();
    }
}
