package io.mindspce.outerfieldsserver.core.singletons;

import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.entities.player.PlayerState;
import io.mindspice.mindlib.data.cache.ConcurrentIndexCache;


public class EntityManager {
    public static final EntityManager INSTANCE = new EntityManager();
    private final ConcurrentIndexCache<Entity> entityCache = new ConcurrentIndexCache<>(1000, false);

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

    public int entityCount() {
        return entityCache.getSize();
    }

}
