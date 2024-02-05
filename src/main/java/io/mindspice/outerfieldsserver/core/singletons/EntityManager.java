package io.mindspice.outerfieldsserver.core.singletons;

import io.mindspice.mindlib.data.tuples.Triple;
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
import jakarta.annotation.Nullable;
import org.jctools.maps.NonBlockingHashMapLong;
import org.springframework.web.socket.WebSocketSession;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.StampedLock;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;


public class EntityManager {

    private static final EntityManager INSTANCE = new EntityManager();
    private final ConcurrentIndexCache<Entity> entityCache = new ConcurrentIndexCache<>(1000, false);
    private final Map<EntityType, List<Entity>> entityMap = new ConcurrentHashMap<>(EntityType.values().length);

    private final List<SystemListener> systemListeners = new CopyOnWriteArrayList<>();
    private final ScheduledExecutorService tickExec = Executors.newSingleThreadScheduledExecutor();
    private final PriorityBlockingQueue<TimedEvent> timedEventQueue = new PriorityBlockingQueue<>(20, Comparator.comparing(TimedEvent::time));
    private long lastTickMilli = System.currentTimeMillis();

    // ItemTable
    private final Map<String, Triple<ItemType, String, Supplier<?>>> itemSupplierMap = new ConcurrentHashMap<>();

    // For Shell
    private Consumer<String> eventMonitor;
    private boolean monitorEvents = false;
    private Set<Pair<EventType, Predicate<Event<?>>>> monitoredEvents = new HashSet<>();
    private ShellEntity shellEntity;

    private EntityManager() {
        for (EntityType type : EntityType.values()) {
            entityMap.put(type, new ArrayList<>(50));
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

    public List<Entity> multipleEntitiesById(IntList ids) {
        return entityCache.getMultiple(ids);
    }

    public List<Entity> multipleEntitiesById(Collection<Integer> ids) {
        IntList list = new IntList(ids.size());
        ids.forEach(list::add);
        return entityCache.getMultiple(list);
    }

    public List<Entity> allEntities() {
        return entityCache.getAsList().stream().filter(Objects::nonNull).toList();
    }

    public <T> List<T> getEntitiesOfType(EntityType entityType, List<T> rtnList) {
        entityMap.get(entityType).forEach(e -> {
            T ent = entityType.castOrNull(e);
            if (ent == null) {
                // TODO log this
            } else {
                rtnList.add(ent);
            }
        });
        return rtnList;
    }

    public Entity entityById(int entityId) {
        return entityCache.get(entityId);
    }

    public int entityIdToPlayerId(int id) {
        PlayerEntity player = EntityType.PLAYER.castOrNull(entityCache.get(id));
        if (player == null) { return -1; }
        return player.playerId();
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

    private void destroyEntity(int entityId) {
        Entity entity = entityById(entityId);
        if (entity == null) { return; }
        entityMap.get(entity.entityType()).removeIf(e -> e.entityId() == entityId);
        entityCache.remove(entityId);
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

    private void addToEntityMap(EntityType entityType, Entity entity) {
        if (!entityType.validate(entity)) {
            throw new IllegalStateException("Error adding new entity to map: Type mismatch");
        }
        entityMap.get(entityType).add(entity);
    }

    public ChunkEntity newChunkEntity(AreaId areaId, IVector2 chunkIndex, ChunkJson chunkJson) {
        int id = entityCache.getAndReserveNextIndex();
        ChunkEntity chunkEntity = new ChunkEntity(id, areaId, chunkIndex, chunkJson);
        entityCache.putAtReservedIndex(id, chunkEntity);

        addToEntityMap(EntityType.CHUNK, chunkEntity);
        // Doesn't need to register is handled internally on world system
        return chunkEntity;
    }

    public ShellEntity getShellEntity() {
        if (shellEntity == null) {
            int id = entityCache.getAndReserveNextIndex();
            shellEntity = new ShellEntity(id);
            entityCache.putAtReservedIndex(id, shellEntity);

            addToEntityMap(EntityType.ANY, shellEntity);
            Event.emitAndRegisterPositionalEntity(SystemType.QUEST, AreaId.NONE, IVector2.negOne(), shellEntity);

        }
        return shellEntity;
    }

    public AreaEntity newAreaEntity(AreaId areaId, IRect2 areaSize, IVector2 chunkSize,
            List<Pair<LocationEntity, IVector2>> staticLocations, int initialSetSize) {
        int id = entityCache.getAndReserveNextIndex();
        AreaEntity areaEntity = new AreaEntity(id, areaId, areaSize, chunkSize, staticLocations, initialSetSize);
        areaId.setEntityId(id);
        areaId.setAreaEntity(areaEntity);
        entityCache.putAtReservedIndex(id, areaEntity);

        addToEntityMap(EntityType.AREA, areaEntity);
        // Doesnt need to register is handled internally on world system
        return areaEntity;
    }

    public PlayerEntity newPlayerEntity(int playerId, String playerName, List<EntityState> initState,
            ClothingItem[] outfit, AreaId currArea, IVector2 currPos, WebSocketSession session, boolean broadcast) {

        int id = entityCache.getAndReserveNextIndex();
        PlayerEntity playerEntity = new PlayerEntity(id, playerId, playerName, initState, outfit, currArea, currPos, session);
        entityCache.putAtReservedIndex(id, playerEntity);

        addToEntityMap(EntityType.PLAYER, playerEntity);

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

        addToEntityMap(EntityType.NON_PLAYER, npcEntity);

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

        addToEntityMap(EntityType.QUEST_PLAYER, questEntity);
        if (broadcast) {
            Event.emitAndRegisterPositionalEntity(SystemType.NPC, AreaId.NONE, IVector2.of(-1, -1), questEntity);
        }
        return questEntity;
    }

    public ContainerEntity newContainerEntity(ContainerType containerType, AreaId areaId, IVector2 position,
            Map<String, ItemEntity<?>> itemMap, boolean broadcast) {
        int id = entityCache.getAndReserveNextIndex();
        ContainerEntity containerEntity = new ContainerEntity(id, containerType, areaId, position, itemMap);
        entityCache.putAtReservedIndex(id, containerEntity);

        addToEntityMap(EntityType.CONTAINER, containerEntity);

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

        addToEntityMap(EntityType.LOOT, lootEntity);
        return lootEntity;
    }

    public TestEntity newTestEntity(SystemType systemTypeToEmit) {
        int id = entityCache.getAndReserveNextIndex();
        TestEntity testEnt = new TestEntity(id, EntityType.TEST, AreaId.TEST);
        entityCache.putAtReservedIndex(id, testEnt);

        addToEntityMap(EntityType.TEST, testEnt);

        Event.emitAndRegisterPositionalEntity(systemTypeToEmit, AreaId.TEST, IVector2.of(-1, -1), testEnt);

        return testEnt;
    }

    @Nullable
    public ItemEntity<?> newItemEntity(String itemKey, int amount) {
        int id = entityCache.getAndReserveNextIndex();

        Triple<ItemType, String, Supplier<?>> itemSupplier = itemSupplierMap.get(itemKey);
        if (itemSupplier == null) {
            // TODO log this, this is important;
            return null; //eww
        }

        ItemEntity<?> itemEntity;

        try {
            itemEntity = new ItemEntity<>(
                    id, itemKey, itemSupplier.first(), itemSupplier.second(),
                    itemSupplier.first().castOrNull(itemSupplier.third().get()),
                    amount
            );
        } catch (Exception e) {
            return null;
        }
        entityCache.putAtReservedIndex(id, itemEntity);

        addToEntityMap(EntityType.ITEM, itemEntity);
        // items are untracked by systems
        return itemEntity;
    }

    public TestSystem newTestSystem(SystemType systemType) {
        int id = entityCache.getAndReserveNextIndex();
        TestSystem systemEntity = new TestSystem(id, systemType);
        entityCache.putAtReservedIndex(id, systemEntity);

        addToEntityMap(EntityType.SYSTEM, systemEntity);
        registerSystem(systemEntity);
        return systemEntity;
    }

    public CombatSystem newCombatSystem(ScheduledExecutorService combatExec, HttpServiceClient serviceClient,
            NonBlockingHashMapLong<PlayerEntity> playerTable, Map<UUID, ActiveCombat> combatTable) {
        int id = entityCache.getAndReserveNextIndex();
        CombatSystem systemEntity = new CombatSystem(id, combatExec, serviceClient, playerTable, combatTable);
        entityCache.putAtReservedIndex(id, systemEntity);

        addToEntityMap(EntityType.SYSTEM, systemEntity);
        registerSystem(systemEntity);
        return systemEntity;
    }

    public NPCSystem newNPCSystem() {
        int id = entityCache.getAndReserveNextIndex();
        NPCSystem systemEntity = new NPCSystem(id);
        entityCache.putAtReservedIndex(id, systemEntity);

        addToEntityMap(EntityType.SYSTEM, systemEntity);
        registerSystem(systemEntity);
        return systemEntity;
    }

    public PlayerSystem newPlayerSystem() {
        int id = entityCache.getAndReserveNextIndex();
        PlayerSystem systemEntity = new PlayerSystem(id);
        entityCache.putAtReservedIndex(id, systemEntity);

        addToEntityMap(EntityType.SYSTEM, systemEntity);
        registerSystem(systemEntity);
        return systemEntity;
    }

    public QuestSystem newQuestSystem() {
        int id = entityCache.getAndReserveNextIndex();
        QuestSystem systemEntity = new QuestSystem(id);
        entityCache.putAtReservedIndex(id, systemEntity);

        addToEntityMap(EntityType.SYSTEM, systemEntity);
        registerSystem(systemEntity);
        return systemEntity;
    }

    public WorldSystem newWorldSystem(Map<AreaId, AreaEntity> areaMap) {
        int id = entityCache.getAndReserveNextIndex();
        WorldSystem systemEntity = new WorldSystem(id, areaMap);
        entityCache.putAtReservedIndex(id, systemEntity);

        addToEntityMap(EntityType.SYSTEM, systemEntity);
        registerSystem(systemEntity);
        return systemEntity;
    }
}



