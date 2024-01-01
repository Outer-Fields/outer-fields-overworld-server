package io.mindspce.outerfieldsserver.core.singletons;

import io.mindspce.outerfieldsserver.area.AreaEntity;
import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.entities.locations.LocationState;
import io.mindspce.outerfieldsserver.entities.player.PlayerState;
import io.mindspce.outerfieldsserver.enums.AreaId;
import io.mindspce.outerfieldsserver.systems.event.*;
import io.mindspce.outerfieldsserver.systems.event.EventListener;
import io.mindspice.mindlib.data.cache.ConcurrentIndexCache;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;


public class EntityManager {
    public static final EntityManager INSTANCE = new EntityManager();
    private final ConcurrentIndexCache<Entity> entityCache = new ConcurrentIndexCache<>(1000, false);
    //    private final Map<EntityType, List<? extends Entity> entityTypeLists = new EnumMap<>(EntityType.class);
    //private final Map<ComponentType, List<? extends Component<?>>> componentTypeLists = new EnumMap<>(ComponentType.class);
    private final List<CoreSystem> eventListeners = new CopyOnWriteArrayList<>();

    private EntityManager() { }

    public static EntityManager GET() {
        return INSTANCE;
    }

    public Entity getEntityById(int id) {
        return entityCache.get(id);
    }

    public AreaEntity getAreaById(AreaId areaId){

    }


    private PlayerState getPlayerState(int playerId, String name) {
        int entityId = entityCache.getAndReserveNextIndex();
        PlayerState playerState = new PlayerState(entityId, playerId);
        entityCache.putAtReservedIndex(entityId, playerState);
        return playerState;
    }

    private LocationState newLocationState(int locationKey, String locationName) {
        int entityId = entityCache.getAndReserveNextIndex();
        LocationState locationState = new LocationState(entityId, locationKey, locationName);
        entityCache.putAtReservedIndex(entityId, locationState);
        return locationState;
    }

    public int entityCount() {
        return entityCache.getSize();
    }

    public <T extends Entity> void registerEventListener(CoreSystem system) {
        eventListeners.add(system);
    }

    public void emitEvent(Event<?> event) {
        for (int i = 0; i < eventListeners.size(); ++i) {
            var listener = eventListeners.get(i);
            if (event.isDirect() && listener.hasListeningEntity(event.recipientEntityId())) {
                listener.onEvent(event);
                return;
            } else {
                if (listener.isListeningFor(event.eventType())) {
                    listener.onEvent(event);
                }
            }
        }
    }

    public <T extends EventListener<T>> void emitCallback(CallBack<T> callback) {

    }

}

