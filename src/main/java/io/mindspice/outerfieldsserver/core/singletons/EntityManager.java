package io.mindspice.outerfieldsserver.core.singletons;

import io.mindspice.outerfieldsserver.core.HttpServiceClient;
import io.mindspice.outerfieldsserver.core.systems.*;
import io.mindspice.outerfieldsserver.entities.*;
import io.mindspice.outerfieldsserver.area.ChunkJson;
import io.mindspice.outerfieldsserver.core.WorldSettings;
import io.mindspice.outerfieldsserver.core.Tick;
import io.mindspice.outerfieldsserver.enums.*;
import io.mindspice.mindlib.data.cache.ConcurrentIndexCache;
import io.mindspice.mindlib.data.collections.lists.primative.IntList;
import io.mindspice.mindlib.data.geometry.IRect2;
import io.mindspice.mindlib.data.geometry.IVector2;
import io.mindspice.mindlib.data.tuples.Pair;
import io.mindspice.outerfieldsserver.systems.event.Event;
import io.mindspice.outerfieldsserver.systems.event.EventType;
import io.mindspice.outerfieldsserver.systems.event.SystemListener;
import org.jctools.maps.NonBlockingHashMapLong;
import org.springframework.web.socket.WebSocketSession;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Consumer;
import java.util.function.Predicate;


public class EntityManager {
    private static final EntityManager INSTANCE = new EntityManager();
    private final ConcurrentIndexCache<Entity> entityCache = new ConcurrentIndexCache<>(1000, false);
    private final Map<EntityType, IntList> entityMap = new EnumMap<>(EntityType.class);
    public final StampedLock entityLock = new StampedLock();
    private final List<SystemListener> systemListeners = new CopyOnWriteArrayList<>();
    private final ScheduledExecutorService tickExec = Executors.newSingleThreadScheduledExecutor();
    private long lastTickMilli = System.currentTimeMillis();
    private Consumer<String> eventMonitor;
    private boolean monitorEvents = false;
    private Set<Pair<EventType, Predicate<Event<?>>>> monitoredEvents = new HashSet<>();
    private ShellEntity shellEntity;

    private EntityManager() {
        for (EntityType type : EntityType.values()) {
            entityMap.put(type, new IntList(50));
        }
        long tickTime = 1_000_000_000 / WorldSettings.GET().tickRate();
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

    public void linkEventMonitor(Consumer<String> monitor) {
        this.eventMonitor = monitor;
    }

    public void writeToShell(String string) {
        eventMonitor.accept(string);
    }

    public void toggleEventMonitoring(boolean bool) {
        monitorEvents = !monitorEvents;
        System.out.println(monitorEvents);
        System.out.println("toggled");
    }

    public boolean addMonitoredEvent(EventType eventType, Predicate<Event<?>> predicate) {
        return monitoredEvents.add(Pair.of(eventType, predicate));
    }

    public boolean removeMonitoredEvent(EventType eventType) {
        return monitoredEvents.removeIf(e -> e.first().equals(eventType));
    }

    public boolean clearMonitoredEvents() {
        monitoredEvents.clear();
        return true;
    }

    public List<EventType> getMonitoredEvents() {
        return monitoredEvents.stream().map(Pair::first).toList();
    }

    public static EntityManager GET() {
        return INSTANCE;
    }

    public List<Entity> allEntities() {
        return entityCache.getAsList().stream().filter(Objects::nonNull).toList();
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

    public List<SystemListener> systemListeners() {
        return Collections.unmodifiableList(systemListeners);
    }

    public int entityCount() {
        return entityCache.getSize();
    }

    public List<SystemListener> eventListeners() {
        return systemListeners;
    }

    private <T extends Entity> void registerSystem(SystemListener system) {
        systemListeners.add(system);
    }

    public void emitEvent(Event<?> event) {
        if (event.eventType() != EventType.TICK) { System.out.println(event); }
        ;
        if (monitorEvents && eventMonitor != null) {
            monitoredEvents.forEach(e -> {
                if (e.first().equals(event.eventType()) && e.second().test(event)) {
                    try {
                        eventMonitor.accept(event.toString());
                    } catch (Exception ex) {
                        System.out.println("Exception on monitor");
                    }
                }
            });
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
        if (monitorEvents && eventMonitor != null) {
            monitoredEvents.forEach(e -> {
                if (e.first().equals(event.eventType()) && e.second().test(event)) {
                    try {
                        eventMonitor.accept(event.toString());
                    } catch (Exception ex) {
                        System.out.println("Exception on monitor");
                    }
                }
            });
        }

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
            entityMap.get(EntityType.CHUNK).add(id);
            return chunkEntity;
        } finally {
            entityLock.unlockWrite(stamp);
        }

        // Doesnt need to register is handled internally on world system
    }

    public ShellEntity getShellEntity() {
        if (shellEntity == null) {
            int id = entityCache.getAndReserveNextIndex();
            shellEntity = new ShellEntity(id);
            entityCache.putAtReservedIndex(id, shellEntity);
            long stamp = entityLock.writeLock();
            try {
                entityMap.get(EntityType.ANY).add(id);
            } finally {
                entityLock.unlockWrite(stamp);
            }
        }
        return shellEntity;
    }

    public AreaEntity newAreaEntity(AreaId areaId, IRect2 areaSize, IVector2 chunkSize,
            List<Pair<LocationEntity, IVector2>> staticLocations) {
        int id = entityCache.getAndReserveNextIndex();
        AreaEntity areaEntity = new AreaEntity(id, areaId, areaSize, chunkSize, staticLocations, List.of());
        areaId.setEntityId(id);
        areaId.setEntity(areaEntity);
        entityCache.putAtReservedIndex(id, areaEntity);

        long stamp = entityLock.writeLock();
        try {
            entityMap.get(EntityType.AREA).add(id);
            return areaEntity;
        } finally {
            entityLock.unlockWrite(stamp);
        }
        // Doesnt need to register is handled internally on world system

    }

    public PlayerEntity newPlayerEntity(int playerId, String playerName, List<EntityState> initState,
            ClothingItem[] outfit, AreaId currArea, IVector2 currPos, WebSocketSession session, boolean broadcast) {

        int id = entityCache.getAndReserveNextIndex();
        PlayerEntity playerEntity = new PlayerEntity(id, playerId, playerName, initState, outfit, currArea, currPos, session);
        entityCache.putAtReservedIndex(id, playerEntity);

        long stamp = entityLock.writeLock();
        try {
            entityMap.get(EntityType.PLAYER).add(id);
        } finally {
            entityLock.unlockWrite(stamp);
        }

        if (broadcast) { Event.emitAndRegisterEntity(SystemType.PLAYER, currArea, currPos, playerEntity); }
        return playerEntity;
    }

    public NonPlayerEntity newNonPlayerEntity(long key, String name, List<EntityState> initStates, ClothingItem[] outfit,
            AreaId currArea, IVector2 currPos, IVector2 viewRectSize, boolean broadcast) {

        int id = entityCache.getAndReserveNextIndex();
        NonPlayerEntity npcEntity = new NonPlayerEntity(id, key, name, initStates, outfit, currArea, currPos, viewRectSize);
        entityCache.putAtReservedIndex(id, npcEntity);

        long stamp = entityLock.writeLock();
        try {
            entityMap.get(EntityType.NON_PLAYER).add(id);
        } finally {
            entityLock.unlockWrite(stamp);
        }

        if (broadcast) { Event.emitAndRegisterEntity(SystemType.NPC, currArea, currPos, npcEntity); }
        return npcEntity;
    }

    public PlayerQuestEntity newPlayerQuestEntity(PlayerQuests quest, int participatingPlayerId, boolean broadcast) {
        int id = entityCache.getAndReserveNextIndex();
        PlayerQuestEntity questEntity = new PlayerQuestEntity(id, quest, participatingPlayerId);
        entityCache.putAtReservedIndex(id, questEntity);

        long stamp = entityLock.writeLock();
        try {
            entityMap.get(EntityType.QUEST_PLAYER).add(id);
        } finally {
            entityLock.unlockWrite(stamp);
        }

        if (broadcast) {
            Event.emitAndRegisterEntity(SystemType.NPC, AreaId.NONE, IVector2.of(-1, -1), questEntity);
            emitEvent(Event.newPlayerQuest(questEntity));
        }
        return questEntity;
    }

    private Entity newSystemEntity(SystemType systemType) {
        int id = entityCache.getAndReserveNextIndex();
        SystemEntity systemEntity = new SystemEntity(id, systemType);
        long stamp = entityLock.writeLock();
        try {
            entityMap.get(EntityType.SYSTEM).add(id);
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

    public CombatSystem newCombatSystem(ScheduledExecutorService combatExec, HttpServiceClient serviceClient,
            NonBlockingHashMapLong<PlayerEntity> playerTable) {
        int id = entityCache.getAndReserveNextIndex();
        CombatSystem systemEntity = new CombatSystem(id, combatExec, serviceClient, playerTable);
        entityCache.putAtReservedIndex(id, systemEntity);

        long stamp = entityLock.writeLock();
        try {
            entityMap.get(EntityType.SYSTEM).add(id);
        } finally {
            entityLock.unlockWrite(stamp);
        }

        registerSystem(systemEntity);
        return systemEntity;
    }

    public NPCSystem newNPCSystem() {
        int id = entityCache.getAndReserveNextIndex();
        NPCSystem systemEntity = new NPCSystem(id);
        entityCache.putAtReservedIndex(id, systemEntity);

        long stamp = entityLock.writeLock();
        try {
            entityMap.get(EntityType.SYSTEM).add(id);
        } finally {
            entityLock.unlockWrite(stamp);
        }

        registerSystem(systemEntity);
        return systemEntity;
    }

    public PlayerSystem newPlayerSystem() {
        int id = entityCache.getAndReserveNextIndex();
        PlayerSystem systemEntity = new PlayerSystem(id);
        entityCache.putAtReservedIndex(id, systemEntity);

        long stamp = entityLock.writeLock();
        try {
            entityMap.get(EntityType.SYSTEM).add(id);
        } finally {
            entityLock.unlockWrite(stamp);
        }

        registerSystem(systemEntity);
        return systemEntity;
    }

    public QuestSystem newQuestSystem() {
        int id = entityCache.getAndReserveNextIndex();
        QuestSystem systemEntity = new QuestSystem(id);
        entityCache.putAtReservedIndex(id, systemEntity);

        long stamp = entityLock.writeLock();
        try {
            entityMap.get(EntityType.SYSTEM).add(id);
        } finally {
            entityLock.unlockWrite(stamp);
        }

        registerSystem(systemEntity);
        return systemEntity;
    }

    public WorldSystem newWorldSystem(Map<AreaId, AreaEntity> areaMap) {
        int id = entityCache.getAndReserveNextIndex();
        WorldSystem systemEntity = new WorldSystem(id, areaMap);
        entityCache.putAtReservedIndex(id, systemEntity);

        long stamp = entityLock.writeLock();
        try {
            entityMap.get(EntityType.SYSTEM).add(id);
        } finally {
            entityLock.unlockWrite(stamp);
        }

        registerSystem(systemEntity);
        return systemEntity;
    }
}



