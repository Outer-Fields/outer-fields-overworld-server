package io.mindspice.outerfieldsserver.core.singletons;

import io.mindspice.mindlib.util.DebugUtils;
import io.mindspice.outerfieldsserver.core.ActiveCombat;
import io.mindspice.outerfieldsserver.core.HttpServiceClient;
import io.mindspice.outerfieldsserver.core.systems.*;
import io.mindspice.outerfieldsserver.data.LootDropItem;
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
import io.mindspice.outerfieldsserver.systems.event.*;
import org.jctools.maps.NonBlockingHashMapLong;
import org.springframework.web.socket.WebSocketSession;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.StampedLock;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;


public class EntityManager {

    private static final EntityManager INSTANCE = new EntityManager();
    private final ConcurrentIndexCache<Entity> entityCache = new ConcurrentIndexCache<>(1000, false);
    private final Map<EntityType, IntList> entityMap = new EnumMap<>(EntityType.class);
    public final StampedLock entityLock = new StampedLock();

    private final List<SystemListener> systemListeners = new CopyOnWriteArrayList<>();
    private final ScheduledExecutorService tickExec = Executors.newSingleThreadScheduledExecutor();
    private final PriorityBlockingQueue<TimedEvent> timedEventQueue = new PriorityBlockingQueue<>(20, Comparator.comparing(TimedEvent::time));
    private long lastTickMilli = System.currentTimeMillis();

    // For Shell
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
        TimedEvent timedEvent = timedEventQueue.peek();
        while (timedEvent != null && timedEvent.time() <= lastTickMilli) {
            emitEvent(timedEventQueue.poll().event());
            timedEvent = timedEventQueue.peek();
        }
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
        long stamp = entityLock.tryOptimisticRead();
        List<Entity> entities;
        do {
            entities = entityCache.getAsList().stream().filter(Objects::nonNull).toList();
        } while (!entityLock.validate(stamp));
        return entities;
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

    public <T> T entityById(int entityId) {
        long stamp = -1;
        T entity = null;
        do {
            stamp = entityLock.tryOptimisticRead();
            Entity tmpEnt = entityCache.get(entityId);
            if (tmpEnt != null) {
                entity = tmpEnt.entityType().castOrNull(tmpEnt);
            }
        } while (!entityLock.validate(stamp));
        return entity;
    }

    public AreaEntity areaById(AreaId areaId) {
        return areaId.areaEntity;
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

    public void destroyEntity(int entityId) {
        Entity entity = entityById(entityId);
        if (entity == null) { return; }
        entityCache.remove(entity.entityId());

        long stamp = entityLock.writeLock();
        try {
            entityMap.get(entity.entityType()).removeFirstValueOf(entityId);
        } finally {
            entityLock.unlockWrite(stamp);
        }
    }

    public void emitEvent(Event<?> event) {
        if (event.eventType() != EventType.TICK) {
            System.out.println(event);
        }
        if (event.eventType() == EventType.ENTITY_DESTROY) {
            destroyEntity(event.recipientEntityId());
        }

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

    public void submitTimedEvent(TimedEvent timedEvent) {
        timedEventQueue.add(timedEvent);
    }

    private void addToEntityMap(EntityType entityType, int EntityId) {
        long stamp = entityLock.writeLock();
        try {
            entityMap.get(entityType).add(EntityId);
        } finally {
            entityLock.unlockWrite(stamp);
        }
    }

    public ChunkEntity newChunkEntity(AreaId areaId, IVector2 chunkIndex, ChunkJson chunkJson) {
        int id = entityCache.getAndReserveNextIndex();
        ChunkEntity chunkEntity = new ChunkEntity(id, areaId, chunkIndex, chunkJson);
        entityCache.putAtReservedIndex(id, chunkEntity);

        addToEntityMap(EntityType.CHUNK, id);
        // Doesn't need to register is handled internally on world system
        return chunkEntity;
    }

    public ShellEntity getShellEntity() {
        if (shellEntity == null) {
            int id = entityCache.getAndReserveNextIndex();
            shellEntity = new ShellEntity(id);
            entityCache.putAtReservedIndex(id, shellEntity);

            addToEntityMap(EntityType.ANY, id);
            Event.emitAndRegisterPositionalEntity(SystemType.QUEST, AreaId.NONE, IVector2.negOne(), shellEntity);

        }
        return shellEntity;
    }

    public AreaEntity newAreaEntity(AreaId areaId, IRect2 areaSize, IVector2 chunkSize,
            List<Pair<LocationEntity, IVector2>> staticLocations) {
        int id = entityCache.getAndReserveNextIndex();
        AreaEntity areaEntity = new AreaEntity(id, areaId, areaSize, chunkSize, staticLocations, List.of());
        areaId.setEntityId(id);
        areaId.setAreaEntity(areaEntity);
        entityCache.putAtReservedIndex(id, areaEntity);

        addToEntityMap(EntityType.AREA, id);
        // Doesnt need to register is handled internally on world system
        return areaEntity;
    }

    public PlayerEntity newPlayerEntity(int playerId, String playerName, List<EntityState> initState,
            ClothingItem[] outfit, AreaId currArea, IVector2 currPos, WebSocketSession session, boolean broadcast) {

        int id = entityCache.getAndReserveNextIndex();
        PlayerEntity playerEntity = new PlayerEntity(id, playerId, playerName, initState, outfit, currArea, currPos, session);
        entityCache.putAtReservedIndex(id, playerEntity);

        addToEntityMap(EntityType.PLAYER, id);

        if (broadcast) {
            Event.emitAndRegisterPositionalEntity(SystemType.PLAYER, currArea, currPos, playerEntity);
        }
        return playerEntity;
    }

    public NonPlayerEntity newNonPlayerEntity(long key, String name, List<EntityState> initStates, ClothingItem[] outfit,
            AreaId currArea, IVector2 currPos, IVector2 viewRectSize, boolean broadcast) {

        int id = entityCache.getAndReserveNextIndex();
        NonPlayerEntity npcEntity = new NonPlayerEntity(id, key, name, initStates, outfit, currArea, currPos, viewRectSize);
        entityCache.putAtReservedIndex(id, npcEntity);

        addToEntityMap(EntityType.NON_PLAYER, id);

        if (broadcast) {
            Event.emitAndRegisterPositionalEntity(SystemType.NPC, currArea, currPos, npcEntity);
        }
        return npcEntity;
    }

    // TODO non positional entity emit
    public PlayerQuestEntity newPlayerQuestEntity(PlayerQuests quest, int participatingPlayerId, boolean broadcast) {
        int id = entityCache.getAndReserveNextIndex();
        PlayerQuestEntity questEntity = new PlayerQuestEntity(id, quest, participatingPlayerId);
        entityCache.putAtReservedIndex(id, questEntity);

        addToEntityMap(EntityType.QUEST_PLAYER, id);
        if (broadcast) {
            Event.emitAndRegisterPositionalEntity(SystemType.NPC, AreaId.NONE, IVector2.of(-1, -1), questEntity);
        }
        return questEntity;
    }

    public ContainerEntity newContainerEntity(ContainerType containerType, AreaId areaId, IVector2 position,
            Map<TokenType, Integer> tokenMap, Map<Long, ItemEntity<?>> itemMap, boolean broadcast) {
        int id = entityCache.getAndReserveNextIndex();
        ContainerEntity containerEntity = new ContainerEntity(id, containerType, areaId, position, tokenMap, itemMap);
        entityCache.putAtReservedIndex(id, containerEntity);

        addToEntityMap(EntityType.CONTAINER, id);

        if (broadcast) {
            Event.emitAndRegisterPositionalEntity(SystemType.WORLD, AreaId.NONE, position, containerEntity);
        }
        return containerEntity;
    }

    public LootEntity newLootEntity(AreaId areaId, List<LootDropItem> lootItems,
            BiFunction<LootEntity, PlayerEntity, List<ItemEntity<?>>> lootCalcFunc) {

        int id = entityCache.getAndReserveNextIndex();
        LootEntity lootEntity = new LootEntity(id, areaId, lootItems, lootCalcFunc);
        entityCache.putAtReservedIndex(id, lootEntity);

        addToEntityMap(EntityType.LOOT, id);
        return lootEntity;
    }

    public TestEntity newTestEntity(SystemType systemTypeToEmit) {
        int id = entityCache.getAndReserveNextIndex();
        TestEntity testEnt = new TestEntity(id, EntityType.TEST, AreaId.TEST);
        entityCache.putAtReservedIndex(id, testEnt);

        addToEntityMap(EntityType.TEST, id);

        Event.emitAndRegisterPositionalEntity(systemTypeToEmit, AreaId.TEST, IVector2.of(-1, -1), testEnt);

        return testEnt;
    }

    public TestSystem newTestSystem(SystemType systemType) {
        int id = entityCache.getAndReserveNextIndex();
        TestSystem systemEntity = new TestSystem(id, systemType);
        entityCache.putAtReservedIndex(id, systemEntity);

        addToEntityMap(EntityType.SYSTEM, id);
        registerSystem(systemEntity);
        return systemEntity;
    }

    public CombatSystem newCombatSystem(ScheduledExecutorService combatExec, HttpServiceClient serviceClient,
            NonBlockingHashMapLong<PlayerEntity> playerTable, Map<UUID, ActiveCombat> combatTable) {
        int id = entityCache.getAndReserveNextIndex();
        CombatSystem systemEntity = new CombatSystem(id, combatExec, serviceClient, playerTable, combatTable);
        entityCache.putAtReservedIndex(id, systemEntity);

        addToEntityMap(EntityType.SYSTEM, id);
        registerSystem(systemEntity);
        return systemEntity;
    }

    public NPCSystem newNPCSystem() {
        int id = entityCache.getAndReserveNextIndex();
        NPCSystem systemEntity = new NPCSystem(id);
        entityCache.putAtReservedIndex(id, systemEntity);

        addToEntityMap(EntityType.SYSTEM, id);
        registerSystem(systemEntity);
        return systemEntity;
    }

    public PlayerSystem newPlayerSystem() {
        int id = entityCache.getAndReserveNextIndex();
        PlayerSystem systemEntity = new PlayerSystem(id);
        entityCache.putAtReservedIndex(id, systemEntity);

        addToEntityMap(EntityType.SYSTEM, id);
        registerSystem(systemEntity);
        return systemEntity;
    }

    public QuestSystem newQuestSystem() {
        int id = entityCache.getAndReserveNextIndex();
        QuestSystem systemEntity = new QuestSystem(id);
        entityCache.putAtReservedIndex(id, systemEntity);

        addToEntityMap(EntityType.SYSTEM, id);
        registerSystem(systemEntity);
        return systemEntity;
    }

    public WorldSystem newWorldSystem(Map<AreaId, AreaEntity> areaMap) {
        int id = entityCache.getAndReserveNextIndex();
        WorldSystem systemEntity = new WorldSystem(id, areaMap);
        entityCache.putAtReservedIndex(id, systemEntity);

        addToEntityMap(EntityType.SYSTEM, id);
        registerSystem(systemEntity);
        return systemEntity;
    }
}



