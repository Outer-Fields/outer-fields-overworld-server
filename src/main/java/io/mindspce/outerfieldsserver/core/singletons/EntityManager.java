package io.mindspce.outerfieldsserver.core.singletons;

import io.mindspce.outerfieldsserver.components.Component;
import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.entities.locations.LocationState;
import io.mindspce.outerfieldsserver.entities.player.PlayerState;
import io.mindspce.outerfieldsserver.enums.ComponentType;
import io.mindspce.outerfieldsserver.enums.EntityType;
import io.mindspice.mindlib.data.cache.ConcurrentIndexCache;
import org.jctools.maps.NonBlockingHashMap;

import java.util.concurrent.ConcurrentHashMap;


public class EntityManager {
    public static final EntityManager INSTANCE = new EntityManager();
    private final ConcurrentIndexCache<Entity> entityCache = new ConcurrentIndexCache<>(1000, false);
    private final NonBlockingHashMap<EntityType, <T extends Entity> entityTypeLists = new NonBlockingHashMap<>(1000);
    private final NonBlockingHashMap<ComponentType, int[]>
    private EntityManager() { }

    public static EntityManager GET() {
        return INSTANCE;
    }

    public PlayerState newPlayerState(int playerId) {
        PlayerState playerState = new PlayerState(playerId);
        int entityId = entityCache.put(playerState);
        playerState.setId(entityId);
        return playerState;
    }

    public LocationState newLocationState(int locationKey, String locationName) {
        LocationState locationState = new LocationState(locationKey, locationName);
        int entityId = entityCache.put(locationState);
        locationState.setId(entityId);
        return locationState;
    }

    public int entityCount() {
        return entityCache.getSize();
    }


}
