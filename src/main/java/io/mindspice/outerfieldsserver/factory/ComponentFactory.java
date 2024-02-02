package io.mindspice.outerfieldsserver.factory;

import io.mindspice.mindlib.functional.consumers.BiPredicatedBiConsumer;
import io.mindspice.outerfieldsserver.components.item.LootDrop;
import io.mindspice.outerfieldsserver.components.npc.CharSpawnController;
import io.mindspice.outerfieldsserver.components.player.*;
import io.mindspice.outerfieldsserver.components.serialization.Visibility;
import io.mindspice.outerfieldsserver.components.world.*;
import io.mindspice.outerfieldsserver.core.singletons.EntityManager;
import io.mindspice.outerfieldsserver.data.LootDropItem;
import io.mindspice.outerfieldsserver.entities.*;
import io.mindspice.outerfieldsserver.components.entity.EntityStateComp;
import io.mindspice.outerfieldsserver.components.entity.GlobalPosition;
import io.mindspice.outerfieldsserver.components.npc.NPCMovement;
import io.mindspice.outerfieldsserver.components.primatives.ComponentSystem;
import io.mindspice.outerfieldsserver.components.primatives.SimpleEmitter;
import io.mindspice.outerfieldsserver.components.primatives.SimpleListener;
import io.mindspice.outerfieldsserver.components.serialization.CharacterSerializer;
import io.mindspice.outerfieldsserver.core.WorldSettings;
import io.mindspice.outerfieldsserver.enums.*;
import io.mindspice.outerfieldsserver.systems.event.Event;
import io.mindspice.outerfieldsserver.systems.event.EventData;
import io.mindspice.outerfieldsserver.systems.event.EventType;
import io.mindspice.mindlib.data.geometry.IConcurrentPQuadTree;
import io.mindspice.mindlib.data.geometry.IPolygon2;
import io.mindspice.mindlib.data.geometry.IRect2;
import io.mindspice.mindlib.data.geometry.IVector2;
import io.mindspice.outerfieldsserver.systems.event.TimedEvent;
import org.springframework.security.core.parameters.P;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;


public class ComponentFactory {

    public static GlobalPosition addGlobalPosition(Entity entity) {
        if (entity.hasAttachedComponent(ComponentType.GLOBAL_POSITION)) {
            //TODO debug log this
            return ComponentType.GLOBAL_POSITION.castOrNull(
                    entity.getComponent(ComponentType.GLOBAL_POSITION));
        }
        GlobalPosition globalPosition = new GlobalPosition(entity);
        entity.addComponent(globalPosition);
        return globalPosition;
    }

    public static SimpleListener addSimpleListener(Entity entity) {
        SimpleListener listener = new SimpleListener(entity);
        entity.addComponent(listener);
        return listener;
    }

    public static SimpleEmitter addSimpleEmitter(Entity entity, List<EventType> emittedEvents) {
        SimpleEmitter simpleEmitter = new SimpleEmitter(entity, emittedEvents);
        entity.addComponent(simpleEmitter);
        return simpleEmitter;
    }

    public static <T> SimpleObject<T> addSimpleObject(Entity entity, T object, List<EventType> emittedEvents) {
        SimpleObject<T> simpleObject = new SimpleObject<>(entity, object, emittedEvents);
        entity.addComponent(simpleObject);
        return simpleObject;
    }

    public static ChunkMap addChunkMap(Entity entity, ChunkEntity[][] chunkEntities) {
        ChunkMap chunkMap = new ChunkMap(entity, chunkEntities);
        entity.addComponent(chunkMap);
        return chunkMap;
    }

    public static EntityGrid addEntityGrid(Entity entity, IRect2 areaRect, int maxPerQuad) {
        EntityGrid grid = new EntityGrid(entity, areaRect, maxPerQuad);
        entity.addComponent(grid);
        return grid;
    }

    public static ActiveEntities addActiveEntities(Entity entity, int initialSetSize) {
        ActiveEntities activeEntities= new ActiveEntities(entity, initialSetSize);
        entity.addComponent(activeEntities);
        return activeEntities;
    }

    public static CollisionGrid addCollisionGrid(Entity entity, IConcurrentPQuadTree<IPolygon2> quadTree) {
        CollisionGrid grid = new CollisionGrid(entity, quadTree);
        entity.addComponent(grid);
        return grid;
    }

//    public static AreaEntities addTrackedEntities(Entity entity, List<Entity> trackedEntities) {
//        var tracker = new AreaEntities(entity, trackedEntities);
//        entity.addComponent(tracker);
//        return tracker;
//    }
//
//    public static AreaEntities addTrackedEntities(Entity entity) {
//        var tracker = new AreaEntities(entity);
//        entity.addComponent(tracker);
//        return tracker;
//    }

    public static ViewRect addViewRect(PositionalEntity entity, IVector2 size, IVector2 position, boolean emitMutable) {
        GlobalPosition globalPosition = ComponentType.GLOBAL_POSITION.castOrNull(entity.getComponent(ComponentType.GLOBAL_POSITION));
        if (globalPosition == null) {
            throw new IllegalStateException("Entity must have an existing GlobalPosition component to add ViewRect, " +
                    "Use AreaMonitor for static entities");
        }
        ViewRect viewRect = new ViewRect(entity, size, position, emitMutable);
        globalPosition.registerOutputHook(EventType.ENTITY_POSITION_CHANGED, viewRect::onSelfPositionChanged, false);
        return viewRect;
    }

    public static class CompSystem {

        public static void attachPlayerEntityComponents(PlayerEntity entity, IVector2 currPosition, AreaId currArea,
                List<EntityState> initStates, ClothingItem[] initOutFit, WebSocketSession webSocketSession) {

            GlobalPosition globalPosition = ComponentType.GLOBAL_POSITION.castOrNull(entity.getComponent(ComponentType.GLOBAL_POSITION));
            if (globalPosition == null) { throw new IllegalStateException("Entity must have an existing GlobalPosition component"); }

            Visibility visibility = ComponentType.VISIBILITY.castOrNull(entity.getComponent(ComponentType.VISIBILITY));
            if (visibility == null) { throw new IllegalStateException("Entity must have an existing Visibility component"); }

            ViewRect viewRect = new ViewRect(entity, WorldSettings.GET().playerViewWithBuffer(), currPosition, true);
            LocalTileGrid localTileGrid = new LocalTileGrid(entity, 5, currArea);
            PlayerSession playerSession = new PlayerSession(entity, webSocketSession);
            KnownEntities knownEntities = new KnownEntities(entity);
            PlayerNetOut playerNetOut = new PlayerNetOut(entity, playerSession, viewRect, knownEntities);
            NetPlayerPosition netPlayerPosition = new NetPlayerPosition(entity, currPosition, localTileGrid.tileGrid(),
                    viewRect.getRect(), playerNetOut::authCorrection, globalPosition::updatePosition
            );

            ComponentSystem playerNetInSystem = new ComponentSystem(
                    entity,
                    List.of(netPlayerPosition, globalPosition, viewRect, localTileGrid, playerSession, knownEntities, playerNetOut),
                    EventProcMode.PASS_THROUGH
            ).withComponentName("PlayerNetworkController");

            EntityStateComp stateComp = new EntityStateComp(entity, initStates);
            CharacterOutfit outfit = new CharacterOutfit(entity, initOutFit);
            CharacterSerializer characterSerializer = new CharacterSerializer(
                    entity,
                    globalPosition::currPosition,
                    stateComp::currStates,
                    outfit::currOutfit,
                    visibility::isActive,
                    visibility::isVisibleToEntity
            );
            PlayerItemsAndFunds itemsAndFunds = new PlayerItemsAndFunds(entity, globalPosition::currPosition);

            globalPosition.registerOutputHook(EventType.ENTITY_POSITION_CHANGED, viewRect::onSelfPositionChanged, false);
            globalPosition.registerOutputHook(EventType.ENTITY_POSITION_CHANGED, localTileGrid::onSelfPositionChanged, false);
            globalPosition.registerOutputHook(EventType.ENTITY_AREA_CHANGED, localTileGrid::onSelfAreaChanged, false);
            globalPosition.registerOutputHook(EventType.ENTITY_AREA_CHANGED, knownEntities::onPlayerAreaChanged, false);
            viewRect.registerOutputHook(EventType.ENTITY_VIEW_RECT_CHANGED, knownEntities::onSelfViewRectChanged, false);

            entity.addComponents(List.of(playerNetInSystem, itemsAndFunds, stateComp, outfit, characterSerializer, playerNetOut));

        }

        public static void attachBaseNPCComponents(NonPlayerEntity entity, IVector2 currPosition, List<EntityState> initStates,
                ClothingItem[] initOutFit, IVector2 viewRectSize, IRect2 spawnArea, IVector2 respawnTimes) {

            GlobalPosition globalPosition = ComponentType.GLOBAL_POSITION.castOrNull(entity.getComponent(ComponentType.GLOBAL_POSITION));
            if (globalPosition == null) { throw new IllegalStateException("Entity must have an existing GlobalPosition component"); }

            Visibility visibility = ComponentType.VISIBILITY.castOrNull(entity.getComponent(ComponentType.VISIBILITY));
            if (visibility == null) { throw new IllegalStateException("Entity must have an existing Visibility component"); }

            ViewRect viewRect = new ViewRect(entity, viewRectSize, currPosition, true);
            NPCMovement NPCMovement = new NPCMovement(entity, currPosition, globalPosition::currPosition);
            EntityStateComp stateComp = new EntityStateComp(entity, initStates);
            CharacterOutfit outfit = new CharacterOutfit(entity, initOutFit);
            CharSpawnController charSpawnController = new CharSpawnController(
                    entity,
                    spawnArea,
                    respawnTimes,
                    visibility::isActive

            );
            CharacterSerializer characterSerializer = new CharacterSerializer(
                    entity,
                    globalPosition::currPosition,
                    stateComp::currStates,
                    outfit::currOutfit,
                    visibility::isActive,
                    visibility::isVisibleToEntity
            );

            NPCMovement.registerOutputHook(EventType.ENTITY_POSITION_UPDATE, globalPosition::onPositionUpdate, true);
            globalPosition.registerOutputHook(EventType.ENTITY_POSITION_CHANGED, viewRect::onSelfPositionChanged, false);
            globalPosition.registerInputHook(EventType.ENTITY_POSITION_UPDATE, globalPosition::onPositionUpdate, false);
            charSpawnController.registerOutputHook(EventType.ENTITY_SET_ACTIVE, visibility::onSetActive, false);

            entity.addComponents(List.of(NPCMovement, stateComp, outfit, viewRect, characterSerializer, charSpawnController));

        }
    }

    public static void attachNpcDeathLoot(NonPlayerEntity entity, List<LootDropItem> lootItems,
            BiFunction<LootEntity, PlayerEntity, List<ItemEntity<?>>> lootCalcFunc) {

        LootEntity lootEntity = EntityManager.GET().newLootEntity(entity.areaId(), lootItems, lootCalcFunc);
        LootDrop lootDrop = new LootDrop(entity, lootEntity);

        lootDrop.registerListener(EventType.CHARACTER_DEATH, BiPredicatedBiConsumer.of(
                (LootDrop drop, Event<EventData.CharacterDeath> event) -> event.data().deadEntityId() == drop.entityId(),
                (LootDrop drop, Event<EventData.CharacterDeath> event) -> {
                    PlayerEntity player = EntityType.PLAYER.castOrNull(
                            EntityManager.GET().entityById(event.data().killerEntityId())
                    );
                    if (player == null) {
                        //TODO log this
                        return;
                    }
                    List<ItemEntity<?>> loot = lootDrop.getLootEntity().calculateLootDrop((player));
                    Map<TokenType, Integer> tokens = loot.stream().map(ItemEntity::getAsTokenEntry)
                            .filter(Objects::nonNull) // Instead of filtering twice, filter once and remove nulls for non-token items
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

                    Map<Long, ItemEntity<?>> items = loot.stream().map(ItemEntity::getAsItemEntry)
                            .filter(Objects::nonNull) // same here filter out tokens instead of filtering twice
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

                    // Since position is encapsulated as it isn't made thread-safe due to frequent updates, emit
                    // a callback to the GlobalPosition component to get the drop location and spawn entity from it
                    Consumer<GlobalPosition> addPosConsumer = (GlobalPosition pos) -> {
                        // Instance new container that is "dropped" and added to the world
                        ContainerEntity container = EntityManager.GET().newContainerEntity(
                                ContainerType.BAG, pos.areaId(), pos.currPosition(), tokens, items, false
                        );

                        Visibility contVis = (Visibility) container.getComponent(ComponentType.VISIBILITY);
                        contVis.initVisibleIds(entity.entityId());

                        EntityManager.GET().submitTimedEvent(TimedEvent.ofOffsetMinutes(
                                60 * 12, Event.destroyEntity(container)
                        ));

                        EntityManager.GET().submitTimedEvent(TimedEvent.ofOffsetMinutes(
                                60,
                                Event.builder(EventType.ENTITY_VISIBILITY_UPDATE)
                                        .setEventArea(entity.areaId())
                                        .setRecipientEntityId(entity.entityId())
                                        .setData(EventData.VisibilityUpdate.newVisibleToAll())
                                        .build()
                        ));

                        Event.emitAndRegisterPositionalEntity(SystemType.WORLD, AreaId.NONE, pos.currPosition(), container);


                    };

                    EntityManager.GET().emitEvent(Event.directEntityCallback(lootDrop, player.areaId(),
                            player.playerId(), ComponentType.GLOBAL_POSITION, addPosConsumer
                    ));
                }
        ));

        entity.addComponent(lootDrop);
    }

    public static void attachNPCAttackComponents(NonPlayerEntity entity) {
        ViewRect viewRect = ComponentType.VIEW_RECT.castOrNull(entity.getComponent(ComponentType.VIEW_RECT));
        if (viewRect == null) { throw new IllegalStateException("Entity must have existing ViewRect"); }


    }


}
