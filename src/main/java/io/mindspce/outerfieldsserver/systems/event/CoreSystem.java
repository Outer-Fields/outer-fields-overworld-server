package io.mindspce.outerfieldsserver.systems.event;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import io.mindspce.outerfieldsserver.components.Component;
import io.mindspce.outerfieldsserver.core.Tick;
import io.mindspce.outerfieldsserver.core.singletons.EntityManager;
import io.mindspce.outerfieldsserver.enums.ComponentType;
import io.mindspce.outerfieldsserver.enums.QueryType;
import io.mindspce.outerfieldsserver.enums.SystemType;
import io.mindspce.outerfieldsserver.systems.EventData;
import io.mindspice.mindlib.data.collections.sets.AtomicBitSet;
import org.jctools.queues.MpscUnboundedXaddArrayQueue;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public abstract class CoreSystem implements EventListener<CoreSystem> {
    // Registries for various O(1) lookups
    private final SystemType systemType;
    private final Map<EventType, List<EventListener<?>>> listenerRegistry = new EnumMap<>(EventType.class);
    private final Map<QueryType, List<EventListener<?>>> queryRegistry = new EnumMap<>(QueryType.class);
    private final TIntObjectMap<List<EventListener<?>>> entityRegistry = new TIntObjectHashMap<>(100);
    private final Map<ComponentType, List<Component<?>>> componentTypeRegistry = new HashMap<>(100);
    private final TLongObjectMap<Component<?>> componentRegistry = new TLongObjectHashMap<>(100);

    // List of onTick components for easy iteration
    private final List<EventListener<?>> tickListeners = new ArrayList<>();

    // Thread safe lookup tables for message delivery
    private final AtomicBitSet listeningFor = new AtomicBitSet(EventType.values().length);
    private final AtomicBitSet queryableFor = new AtomicBitSet(QueryType.values().length);
    private final AtomicBitSet listeningEntities = new AtomicBitSet(EntityManager.GET().entityCount());

    // Queue/Executor
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private volatile boolean running = false;
    private final MpscUnboundedXaddArrayQueue<Event<?>> eventQueue = new MpscUnboundedXaddArrayQueue<>(50);

    public CoreSystem(SystemType systemType, boolean doStart) {
        this.systemType = systemType;
        if (doStart) { start(); }
    }

    @Override
    public String name() {
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

    public SystemType getSystemType() {
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
        return listeningFor.get(eventType.ordinal());
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

                    case EventType.QUERY -> {
                        @SuppressWarnings("unchecked")
                        var castedEvent = (Event<EventData.Query<?, ?, ?>>) nextEvent;
                        handleQuery(castedEvent);
                    }
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
        Future<?> future = executorService.submit(() -> {
            eventQueue.drain(this::processQueueItem);
        });
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

    public void registerComponent(Component<?> component) {
        List<EventListener<?>> existingListeners = entityRegistry.get(component.entityId());
        if (existingListeners == null) {
            existingListeners = new ArrayList<>(4);
        }

        existingListeners.add(component);
        componentRegistry.put(component.componentId(), component);
        componentTypeRegistry.computeIfAbsent(component.componentType(), v -> new ArrayList<>()).add(component);

        for (var event : component.getAllListeningFor()) {
            listenerRegistry.computeIfAbsent(event, v -> new ArrayList<>()).add(component);
            listeningFor.set(event.ordinal());
        }

        for (var query : component.getAllQueryableFor()) {
            queryRegistry.computeIfAbsent(query, v -> new ArrayList<>()).add(component);
            queryableFor.set(query.ordinal());
        }

        if (component.isOnTick()) {
            tickListeners.add(component);
        }
        listeningEntities.set(component.entityId());


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
                listeningFor.set(listenerList.getKey().ordinal());
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
                listeningFor.set(listenerList.getKey().ordinal(), false);
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
        CallBack<?> data = (CallBack<?>) event.data();
        if (data.entityId() == -1) {
            componentTypeRegistry.getOrDefault(data.componentType(), List.of()).forEach(c -> c.onEvent(event));
        } else {
            List<Component<?>> components = componentTypeRegistry.get(data.componentType());
            for (int i = 0; i < components.size(); i++) {
                if (components.get(i).entityId() == data.entityId()) {
                    components.get(i).onEvent(event);
                    return;
                }

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
            listeners.get(i).onEvent(event);
        }
    }

    private void handleQuery(Event<EventData.Query<?, ?, ?>> event) {
        List<EventListener<?>> listeners = queryRegistry.get(event.data().queryType());
        if (listeners == null) {
            //TODO logs this
        }
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).onQuery(event);
        }
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
            List<EventListener<?>> listeners = entityRegistry.get(event.recipientEntityId());
            if (listeners == null) {
                // TODO log this? might not be error as we dont track entity ids before issuing?
                return;
            }
            for (int i = 0; i < listeners.size(); ++i) {
                listeners.get(i).onEvent(event);
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
}
