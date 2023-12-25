package io.mindspce.outerfieldsserver.core.singletons;

import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.entities.locations.LocationState;
import io.mindspce.outerfieldsserver.entities.player.PlayerState;
import io.mindspce.outerfieldsserver.enums.EntityType;
import io.mindspce.outerfieldsserver.systems.event.Callback;
import io.mindspce.outerfieldsserver.systems.event.Event;
import io.mindspce.outerfieldsserver.systems.event.EventDomain;
import io.mindspce.outerfieldsserver.systems.event.EventListener;
import io.mindspice.mindlib.data.cache.ConcurrentIndexCache;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;


public class EntityManager {
    public static final EntityManager INSTANCE = new EntityManager();
    private final ConcurrentIndexCache<Entity> entityCache = new ConcurrentIndexCache<>(1000, false);
    private final Map<EntityType, List<? extends Entity>> entityTypeLists = new EnumMap<>(EntityType.class);
    //private final Map<ComponentType, List<? extends Component<?>>> componentTypeLists = new EnumMap<>(ComponentType.class);
    private final Map<EventDomain, List<Consumer<Event>>> listenerMap = new EnumMap<>(EventDomain.class);
    private final List<Consumer<Event>> globalListeners = new CopyOnWriteArrayList<>();

    private EntityManager() {
        for (var event : EventDomain.values()) {
            listenerMap.put(event, new CopyOnWriteArrayList<>());
        }
    }

    public static EntityManager GET() {
        return INSTANCE;
    }

    public Entity getEntityById(int id) {
        return entityCache.get(id);
    }

    public PlayerState newPlayerState(int playerId) {
        int entityId = entityCache.getAndReserveNextIndex();
        PlayerState playerState = new PlayerState(entityId, playerId);
        entityCache.putAtReservedIndex(entityId, playerState);
        return playerState;
    }

    public LocationState newLocationState(int locationKey, String locationName) {
        int entityId = entityCache.getAndReserveNextIndex();
        LocationState locationState = new LocationState(entityId, locationKey, locationName);
        entityCache.putAtReservedIndex(entityId, locationState);
        return locationState;
    }

    public int entityCount() {
        return entityCache.getSize();
    }

    public <T extends Entity> void registerEventListener(EventDomain domain, Consumer<Event> listener) {
        listenerMap.get(domain).add(listener);
        globalListeners.add(listener);
    }

    public void emitEvent(Event event) {
        if (event.domain() == EventDomain.GLOBAL) {
            globalListeners.forEach(c -> c.accept(event));
        }
        if (event.domain() == EventDomain.DIRECT) {

        }

        List<Consumer<Event>> listeners = listenerMap.get(event.domain());
        if (listeners == null) { return; }
        for (var listener : listeners) {
            listener.accept(event);
        }
    }

    public <T extends EventListener<T>> void emitCallback(Callback<T> callback) {
        EventListener<?> listener = newPlayerState(10);
        if (listener != null && callback.classType().isInstance(listener)) {
            T castedListener = callback.classType().cast(listener);
            castedListener.onCallBack(callback.callback());
        }
    }
}

//
//    public <T extends Object> void emitEntityEvent(EventType entityEvent, T eEvent) {
//      for (var l : eventSystemListeners.get(entityEvent)) {
//          l.accept(eEvent);
//      }
//    public void registerForEntityEvent(EntityEventType eventType, EventListener<EntityEvent> listener) {
//        synchronized (entityEventListeners) {
//            eventListenerSubscriptions.add(Triple.of(eventType, listener, true));
//        }
//    }
//
//    public void registerForEntityEvent(EntityEventType eventType, List<EventListener<EntityEvent>> listeners) {
//        var tripleList = listeners.stream().map(l -> Triple.of(eventType, l, true)).toList();
//        synchronized (eventListenerSubscriptions) {
//            eventListenerSubscriptions.addAll(tripleList);
//        }
//    }
//
//    public void unRegisterForEntityEvents(EntityEventType eventType, EventListener<EntityEvent> listener) {
//        synchronized (entityEventListeners) {
//            eventListenerSubscriptions.remove(Triple.of(eventType, listener, true));
//        }
//    }
//    public void unRegisterForEntityEvents(EntityEventType eventType, List<EventListener<EntityEvent>> listeners) {
//        var tripleList = listeners.stream().map(l -> Triple.of(eventType, l, true)).toList();
//        synchronized (eventListenerSubscriptions) {
//            eventListenerSubscriptions.addAll(tripleList);
//        }
//    }

