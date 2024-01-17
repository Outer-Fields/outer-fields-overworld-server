package io.mindspce.outerfieldsserver.core.singletons;

import io.mindspce.outerfieldsserver.area.AreaEntity;
import io.mindspce.outerfieldsserver.area.ChunkEntity;
import io.mindspce.outerfieldsserver.area.ChunkJson;
import io.mindspce.outerfieldsserver.core.GameSettings;
import io.mindspce.outerfieldsserver.core.Tick;
import io.mindspce.outerfieldsserver.entities.*;
import io.mindspce.outerfieldsserver.enums.*;
import io.mindspce.outerfieldsserver.systems.EventData;
import io.mindspce.outerfieldsserver.systems.event.*;
import io.mindspice.mindlib.data.cache.ConcurrentIndexCache;
import io.mindspice.mindlib.data.collections.lists.primative.IntList;
import io.mindspice.mindlib.data.geometry.IRect2;
import io.mindspice.mindlib.data.geometry.IVector2;
import io.mindspice.mindlib.data.tuples.Pair;
import org.springframework.web.socket.WebSocketSession;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.StampedLock;


public class EntityManager {
    private static final EntityManager INSTANCE = new EntityManager();
    private final ConcurrentIndexCache<Entity> entityCache = new ConcurrentIndexCache<>(1000, false);
    private final Map<EntityType, IntList> entityMap = new EnumMap<>(EntityType.class);
    public final StampedLock entityLock = new StampedLock();
    private final List<SystemListener> systemListeners = new CopyOnWriteArrayList<>();
    private final ScheduledExecutorService tickExec = Executors.newSingleThreadScheduledExecutor();
    private long lastTickMilli = System.currentTimeMillis();

    private EntityManager() {
        for (EntityType type : EntityType.values()) {
            entityMap.put(type, new IntList(50));
        }
        long tickTime = 1_000_000_000 / GameSettings.GET().tickRate();
        tickExec.scheduleAtFixedRate(this::newTick, tickTime, tickTime, TimeUnit.NANOSECONDS);
    }

    private void newTick() {
        var currTime = System.currentTimeMillis();
        double delta = (double) (currTime - lastTickMilli) / 1000.0;
        var tick = new Tick(currTime, delta, -1);
        emitEvent(new Event<>(EventType.TICK, AreaId.GLOBAL, -1, -1, ComponentType.ANY,
                EntityType.ANY, -1, -1, ComponentType.ANY, tick));
        lastTickMilli = currTime;
    }

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
            stamp = entityLock.tryOptimisticRead();
            IntList ids = entityMap.get(entityType);
            for (int i = 0; i < ids.size(); i++) {
                T entity = entityType.castOrNull(entityCache.get(ids.get(i)));
                if (entity == null) {
                    System.out.println("null cast in entity manager");
                    //TODO log this
                } else {
                    returnList.add(entity);
                }
            }
        } while (!entityLock.validate(stamp));
        return returnList;
    }

    public <T> T getEntity(int entityId) {
        long stamp = -1;
        T entity;
        do {
            stamp = entityLock.tryOptimisticRead();
            Entity tmpEnt = entityCache.get(entityId);
            entity = tmpEnt.entityType().castOrNull(tmpEnt);
            if (entity == null) {
                //TODO log this
            }
        } while (!entityLock.validate(stamp));
        return entity;
    }

    public Entity entityById(int id) {
        return entityCache.get(id);
    }

    public AreaEntity areaById(AreaId areaId) {
        return areaId.entity;
    }

    public SystemListener systemListenerByType(SystemType type) {
        return systemListeners.stream().filter(s -> s.systemType() == type).findFirst().orElse(null);

    }

    public int entityCount() {
        return entityCache.getSize();
    }

    public List<SystemListener> eventListeners() {
        return systemListeners;
    }

    public <T extends Entity> void registerSystem(SystemListener system) {
        systemListeners.add(system);
    }

    public void emitEvent(Event<?> event) {
        if (event.eventType() != EventType.TICK) {
            System.out.println(event);
        }


        for (int i = 0; i < systemListeners.size(); ++i) {
            var listener = systemListeners.get(i);
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

    public void emitEventToSystem(SystemType system, Event<?> event) {
        var systemListener = systemListeners.stream()
                .filter(s -> s.systemType() == system)
                .findFirst();

        if (systemListener.isEmpty()) {
            // TODO log this
            System.out.println("failed to find system to emit event");
            return;
        }

        var listener = systemListener.get();
        if (event.isDirect() && listener.hasListeningEntity(event.recipientEntityId())) {
            listener.onEvent(event);
        } else {
            if (listener.isListeningFor(event.eventType())) {
                listener.onEvent(event);
            }
        }

    }

    public ChunkEntity newChunkEntity(AreaId areaId, IVector2 chunkIndex, ChunkJson chunkJson) {
        int id = entityCache.getAndReserveNextIndex();
        ChunkEntity chunkEntity = new ChunkEntity(id, areaId, chunkIndex, chunkJson);
        entityCache.putAtReservedIndex(id, chunkEntity);

        long stamp = entityLock.writeLock();
        try {
            entityMap.get(EntityType.CHUNK_ENTITY).add(id);
            return chunkEntity;
        } finally {
            entityLock.unlockWrite(stamp);
        }

        // Doesnt need to register is handled internally on world system
    }

    public AreaEntity newAreaEntity(AreaId areaId, IRect2 areaSize, IVector2 chunkSize,
            List<Pair<LocationEntity, IVector2>> staticLocations) {
        int id = entityCache.getAndReserveNextIndex();
        AreaEntity areaEntity = new AreaEntity(id, areaId, areaSize, chunkSize, staticLocations);
        areaId.setEntityId(id);
        areaId.setEntity(areaEntity);

        long stamp = entityLock.writeLock();
        try {
            entityMap.get(EntityType.AREA_ENTITY).add(id);
            return areaEntity;
        } finally {
            entityLock.unlockWrite(stamp);
        }
        // Doesnt need to register is handled internally on world system

    }

    public PlayerEntity newPlayerEntity(int playerId, String playerName, List<EntityState> initState,
            ClothingItem[] outfit, AreaId currArea, IVector2 currPos, WebSocketSession session) {

        int id = entityCache.getAndReserveNextIndex();
        PlayerEntity playerEntity = new PlayerEntity(id, playerId, playerName, initState, outfit, currArea, currPos, session);

        long stamp = entityLock.writeLock();
        try {
            entityMap.get(EntityType.PLAYER_ENTITY).add(id);
        } finally {
            entityLock.unlockWrite(stamp);
        }


        emitEvent(Event.newEntity(new EventData.NewEntity(currArea, currPos, playerEntity)));
        emitEventToSystem(SystemType.PLAYER, Event.systemRegisterEntity(playerEntity));
        return playerEntity;
    }

    public NonPlayerEntity newNonPlayerEntity(long key, String name, List<EntityState> initStates, ClothingItem[] outfit,
            AreaId currArea, IVector2 currPos, IVector2 viewRectSize) {

        int id = entityCache.getAndReserveNextIndex();
        NonPlayerEntity npcEntity = new NonPlayerEntity(id, key, name, initStates, outfit, currArea, currPos, viewRectSize);

        long stamp = entityLock.writeLock();
        try {
            entityMap.get(EntityType.NON_PLAYER_ENTITY).add(id);
        } finally {
            entityLock.unlockWrite(stamp);
        }

        emitEvent(Event.newEntity(new EventData.NewEntity(currArea, currPos, npcEntity)));
      //  emitEventToSystem(SystemType.NPC, Event.systemRegisterEntity(npcEntity));
        return npcEntity;
    }

    public Entity newSystemEntity(SystemType systemType) {
        int id = entityCache.getAndReserveNextIndex();
        SystemEntity systemEntity = new SystemEntity(id, systemType);
        long stamp = entityLock.writeLock();
        try {
            entityMap.get(EntityType.SYSTEM_ENTITY).add(id);
        } finally {
            entityLock.unlockWrite(stamp);
        }

        var system = systemListeners.stream().filter(s -> s.systemType() == systemType).findFirst();
        if (system.isEmpty()) {
            throw new RuntimeException("Attempted to register entity with null system");
        }

        // doesn't need to register this is handled internally
        return systemEntity;
    }

}

