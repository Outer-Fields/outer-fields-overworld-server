package io.mindspice.outerfieldsserver.systems.event;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.procedure.TObjectProcedure;
import io.mindspice.mindlib.data.collections.lists.primative.LongList;
import io.mindspice.mindlib.data.collections.sets.AtomicBitSet;
import io.mindspice.outerfieldsserver.components.Component;
import io.mindspice.outerfieldsserver.components.primatives.SimpleEmitter;
import io.mindspice.outerfieldsserver.components.primatives.SimpleListener;
import io.mindspice.outerfieldsserver.core.Tick;
import io.mindspice.outerfieldsserver.core.singletons.EntityManager;
import io.mindspice.outerfieldsserver.entities.Entity;
import io.mindspice.outerfieldsserver.entities.SystemEntity;
import io.mindspice.outerfieldsserver.enums.AreaId;
import io.mindspice.outerfieldsserver.enums.ComponentType;
import io.mindspice.outerfieldsserver.enums.EntityType;
import io.mindspice.outerfieldsserver.enums.SystemType;
import io.mindspice.mindlib.data.collections.sets.AtomicByteSet;
import io.mindspice.mindlib.util.DebugUtils;
import org.jctools.queues.MpscUnboundedXaddArrayQueue;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Predicate;


public abstract class SystemListener extends SystemEntity implements EventListener<SystemListener> {
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
    private final long waitNanos;
    private BitSet systemEvents = new BitSet(EventType.values().length);
    protected final SimpleListener selfListener;
    protected final SimpleEmitter selfEmitter;

    public void onDestroyEntity(Entity entity) {
        long t = System.nanoTime();
        int entityId = entity.entityId();
        listenerRegistry.values().forEach(v -> v.removeIf(c -> c.entityId() == entityId));
        entityRegistry.remove(entityId);
        componentTypeRegistry.values().forEach(v -> v.removeIf(c -> c.entityId() == entityId));;
        componentRegistry.valueCollection().removeIf(c -> c.entityId() == entityId);
        listeningEntities.set(entityId, false);

        entity.getAttachedComponents().forEach(Component::unregisterAllListeners);
        long end = System.nanoTime() - t;
        System.out.println("Destruction Took: " + end);
    }

    public SystemListener(int id, SystemType systemType, boolean doStart) {
        super(id, systemType);
        this.systemType = systemType;
        if (doStart) { start(); }
        waitNanos = -1;
        selfListener = new SimpleListener(this);
        selfEmitter = new SimpleEmitter(this, new ArrayList<>());
        registerComponent(selfListener);
        initDefaultListenerFlags();
    }

    public SystemListener(int id, SystemType systemType, boolean doStart, long waitNanos) {
        super(id, systemType);
        this.systemType = systemType;
        if (doStart) { start(); }
        this.waitNanos = waitNanos;
        selfListener = new SimpleListener(this);
        selfEmitter = new SimpleEmitter(this, new ArrayList<>());
        registerComponent(selfListener);
        initDefaultListenerFlags();
    }

    private void initDefaultListenerFlags() {
        listeningFor.incrementAndGet(EventType.CALLBACK.ordinal());
        listeningFor.incrementAndGet(EventType.TICK.ordinal());
        listeningFor.incrementAndGet(EventType.COMPLETABLE_EVENT.ordinal());
        listeningFor.incrementAndGet(EventType.SYSTEM_ENTITIES_QUERY.ordinal());
        listeningFor.incrementAndGet(EventType.SYSTEM_REGISTER_ENTITY.ordinal());
        listeningFor.incrementAndGet(EventType.ENTITY_DESTROY.ordinal());
        systemEvents.set(EventType.SYSTEM_ENTITIES_QUERY.ordinal());
        systemEvents.set(EventType.SYSTEM_REGISTER_ENTITY.ordinal());
        systemEvents.set(EventType.ENTITY_DESTROY.ordinal());
    }

    @Override
    public String componentName() {
        return systemType.name();
    }

    @Override
    public boolean isListenerFor(EventType eventType) {
        return listeningFor.get(eventType.ordinal()) > 0;
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
                try {

                    Event<?> nextEvent;
                    while ((nextEvent = eventQueue.relaxedPoll()) == null) {
                        if (waitNanos != -1) {
                            LockSupport.parkNanos(waitNanos);
                        } else {
                            Thread.onSpinWait();
                        }
                    }

                    if (systemEvents.get(nextEvent.eventType().ordinal())) {
                        handleSystemEvent(nextEvent);
                        continue;
                    }

                    switch (nextEvent.eventType()) {
                        case TICK -> handleTick((Tick) nextEvent.data());
                        case CALLBACK -> handleCallBack(nextEvent);
                        case COMPLETABLE_EVENT -> handleCompletableEvent(nextEvent);
                        default -> {
                            if (nextEvent.isDirect()) {
                                handleDirectEvent(nextEvent);
                            } else {
                                handleEvent(nextEvent);
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Exception Processing for system listener: " + systemType);
                    e.printStackTrace();
                }
            }

        });
    }

    private void handleSystemEvent(Event<?> event) {
        switch (event.eventType()) {
            case SYSTEM_ENTITIES_QUERY -> {
                @SuppressWarnings("unchecked")
                Predicate<Entity> pred = (Predicate<Entity>) event.data();
                List<Entity> rtnEntities = new ArrayList<>();
                for (int i = 0; i < EntityManager.GET().entityCount(); ++i) {
                    if (listeningEntities.get(i)) {
                        Entity entity = EntityManager.GET().entityById(i);
                        if (pred.test(entity)) { rtnEntities.add(entity); }
                    }
                }
                EntityManager.GET().emitEvent(new Event<>(EventType.SYSTEM_ENTITIES_RESP, AreaId.GLOBAL, -1, -1, ComponentType.ANY,
                        EntityType.ANY, event.issuerEntityId(), event.issuerComponentId(), ComponentType.ANY, rtnEntities));
            }
            case SYSTEM_REGISTER_ENTITY -> {
                Entity entity = (Entity) event.data();
                listeningEntities.set(entity.entityId());
                onRegisterComponent(entity);
            }
            case ENTITY_DESTROY -> onDestroyEntity((Entity) event.data());
        }

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

    public void onRegisterComponent(Entity entity) {
        registerComponents(entity.getAttachedComponents());
        entity.registerWithSystem(this);
    }

    public void registerComponent(Component<?> component) {
        DebugUtils.print("Registered:", component.componentName(), "With:", systemType());
        component.setRegisteredWith(systemType);
        componentRegistry.put(component.componentId(), component);
        componentTypeRegistry.computeIfAbsent(component.componentType(), v -> new ArrayList<>()).add(component);

        for (var event : component.getAllListeningFor()) {
            addListeningEvent(event, component);
        }

        var existingEntity = entityRegistry.get(component.entityId());
        if (existingEntity == null) {
            existingEntity = new ArrayList<>(2);
            entityRegistry.put(component.entityId(), existingEntity);
        }
        existingEntity.add(component);

        if (component.isOnTick()) { tickListeners.add(component); }

        component.linkSystemUpdates(this::addListeningEvent, this::removeListeningEvent, this::registerTickListener);
    }

    public void registerTickListener(EventListener<?> eventListener){
        this.tickListeners.add(eventListener);
    }

    private void registerComponents(List<Component<?>> components) {
        if (components == null) {
            // TODO log this
            System.out.println("Attempted to register null component list");
            return;
        }
        components.forEach(this::registerComponent);
    }

//    public void removeByComponent(Component<?> component) {
//        Iterator<Map.Entry<EventType, List<EventListener<?>>>> itr = listenerRegistry.entrySet().iterator();
//        while (itr.hasNext()) {
//            Map.Entry<EventType, List<EventListener<?>>> listenerList = itr.next();
//            listenerList.getValue().removeIf(l -> l.componentId() == component.componentId());
//
//            if (listenerList.getValue().isEmpty()) {
//                listeningFor.decrementAndGet(listenerList.getKey().ordinal());
//                itr.remove();
//            }
//        }
//
//        Iterator<Map.Entry<ComponentType, List<Component<?>>>> itr2 = componentTypeRegistry.entrySet().iterator();
//        while (itr2.hasNext()) {
//            Map.Entry<ComponentType, List<Component<?>>> componentList = itr2.next();
//            componentList.getValue().removeIf(l -> l.componentId() == component.componentId());
//
//            if (componentList.getValue().isEmpty()) {
//                itr2.remove();
//            }
//        }
//
//        listeningEntities.decrement(component.parentEntity().entityId());
//        tickListeners.removeIf(l -> l.componentId() == component.componentId());
//    }

//    public void removeByEntityId(int entityId) {
//        entityRegistry.remove(entityId);
//        listeningEntities.set(entityId, 0);
//
//        Iterator<Map.Entry<EventType, List<EventListener<?>>>> itr = listenerRegistry.entrySet().iterator();
//        while (itr.hasNext()) {
//            Map.Entry<EventType, List<EventListener<?>>> listenerList = itr.next();
//            listenerList.getValue().removeIf(l -> l.entityId() == entityId);
//            if (listenerList.getValue().isEmpty()) {
//                listeningFor.decrementAndGet(listenerList.getKey().ordinal());
//                itr.remove();
//            }
//        }
//
//        Iterator<Map.Entry<ComponentType, List<Component<?>>>> itr2 = componentTypeRegistry.entrySet().iterator();
//        while (itr2.hasNext()) {
//            Map.Entry<ComponentType, List<Component<?>>> componentList = itr2.next();
//            componentList.getValue().removeIf(l -> l.entityId() == entityId);
//            if (componentList.getValue().isEmpty()) {
//                itr2.remove();
//            }
//        }
//        tickListeners.removeIf(l -> l.entityId() == entityId);
//    }

    private void handleCallBack(Event<?> event) {
        if (!event.isDirect()) {
            componentTypeRegistry.getOrDefault(event.recipientCompType(), List.of())
                    .forEach(c -> { if (c.entityId() != event.issuerEntityId()) { c.onEvent(event); } });
        } else {
            if (event.isDirectComponent()) {
                Component<?> component = componentRegistry.get(event.recipientComponentId());
                component.onEvent(event);
            } else {
                List<Component<?>> comps = entityRegistry.get(event.recipientEntityId());
                if (comps == null) { return; }
                comps.forEach(c -> {
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
            System.out.println("null listeners");
            //TODO log this, error state
            return;
        }
        for (int i = 0; i < listeners.size(); ++i) {
            var listener = listeners.get(i);
            if (listener.entityId() == event.issuerEntityId()) {
                System.out.println("Skipping event: " + event.eventType() + " |  Reason: Self event");
                continue;
            }
            if (!listener.isListening() && event.eventType() != EventType.CALLBACK) { continue; }
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
                var listener = listeners.get(i);
                if (listener.isListenerFor(event.eventType())) {
                    if (!listener.isListening() && event.eventType() != EventType.CALLBACK) { continue; }
                    listener.onEvent(event);
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

    private void addListeningEvent(EventType eventType, Component<?> component) {
        listeningFor.incrementAndGet(eventType.ordinal());
        List<EventListener<?>> existing = listenerRegistry.computeIfAbsent((eventType), v -> new ArrayList<>());

        if (existing.stream().noneMatch(c -> c.equals(component))) {
            existing.add(component);
        }
    }

    private void removeListeningEvent(EventType eventType, Component<?> component) {
        var val = listeningFor.decrementAndGet(eventType.ordinal());
        if (val < 0) {
            throw new RuntimeException("Decremented listeners below zero, this shouldnt happen.");
            // TODO log this and remove exception
        }
        listenerRegistry.get(eventType).removeIf(c -> c.equals(component));
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
