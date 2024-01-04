package io.mindspce.outerfieldsserver.core.singletons;

import io.mindspce.outerfieldsserver.area.AreaEntity;
import io.mindspce.outerfieldsserver.area.ChunkEntity;
import io.mindspce.outerfieldsserver.area.ChunkJson;
import io.mindspce.outerfieldsserver.core.networking.SocketService;
import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.enums.AreaId;
import io.mindspce.outerfieldsserver.enums.EntityType;
import io.mindspce.outerfieldsserver.systems.event.*;
import io.mindspice.mindlib.data.cache.ConcurrentIndexCache;
import io.mindspice.mindlib.data.collections.lists.primative.IntList;
import io.mindspice.mindlib.data.geometry.IRect2;
import io.mindspice.mindlib.data.geometry.IVector2;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.StampedLock;


public class EntityManager {
    private static final EntityManager INSTANCE = new EntityManager();
    private final ConcurrentIndexCache<Entity> entityCache = new ConcurrentIndexCache<>(1000, false);
    private final Map<EntityType, IntList> entityMap = new EnumMap<>(EntityType.class);
    public final StampedLock lock = new StampedLock();
    private final List<SystemListener> eventListeners = new CopyOnWriteArrayList<>();

    private EntityManager() { }

    public static EntityManager GET() {
        return INSTANCE;
    }

    public List<Entity> allEntities() {
        return entityCache.getAsList();
    }

    public <T> List<T> getEntitiesOfType(EntityType entityType, List<T> returnList) {
        long stamp = -1;

        do {
            returnList.clear();
            stamp = lock.tryOptimisticRead();
            IntList ids = entityMap.get(entityType);
            for (int i = 0; i < ids.size(); i++) {
                T entity = entityType.castOrNull(entityCache.get(ids.get(i)));
                if (entity == null) {
                    //TODO log this
                } else {
                    returnList.add(entity);
                }
            }
        } while (!lock.validate(stamp));
        return returnList;
    }

    public <T> T getEntity(EntityType entityType, int entityId) {
        long stamp = -1;
        T entity;
        do {
            stamp = lock.tryOptimisticRead();
            entity = entityType.castOrNull(entityCache.get(entityId));
            if (entity == null) {
                //TODO log this
            }
        } while (!lock.validate(stamp));
        return entity;
    }

    public Entity entityById(int id) {
        return entityCache.get(id);
    }

    public AreaEntity areaById(AreaId areaId) {
        return null;
    }

    public SocketService socketService() {
        return null;
    }

    public int entityCount() {
        return entityCache.getSize();
    }

    public List<SystemListener> eventListeners() {
        return eventListeners;
    }

    public <T extends Entity> void registerSystem(SystemListener system) {
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

    public ChunkEntity newChunkEntity(AreaId areaId, IVector2 chunkIndex, ChunkJson chunkJson) {
        int id = entityCache.getAndReserveNextIndex();
        ChunkEntity chunkEntity = new ChunkEntity(id, areaId, chunkIndex, chunkJson);
        entityCache.putAtReservedIndex(id, chunkEntity);
        long stamp = lock.writeLock();
        try {
            entityMap.computeIfAbsent(EntityType.CHUNK_ENTITY, val -> new IntList()).add(id);
            return chunkEntity;
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    public AreaEntity newAreaEntity(AreaId areaId, ChunkEntity[][] chunkMap, IRect2 areaSize, IVector2 chunkSize) {
        int id = entityCache.getAndReserveNextIndex();
        AreaEntity areaEntity = new AreaEntity(id, areaId, chunkMap, areaSize, chunkSize);
        long stamp = lock.writeLock();
        try {
            entityMap.computeIfAbsent(EntityType.AREA_ENTITY, val -> new IntList()).add(id);
            return areaEntity;
        } finally {
            lock.unlockWrite(stamp);
        }
    }
}

