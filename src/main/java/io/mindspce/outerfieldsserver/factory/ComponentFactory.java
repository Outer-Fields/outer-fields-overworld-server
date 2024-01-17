package io.mindspce.outerfieldsserver.factory;

import io.mindspce.outerfieldsserver.area.ChunkEntity;
import io.mindspce.outerfieldsserver.components.entity.EntityStateComp;
import io.mindspce.outerfieldsserver.components.entity.GlobalPosition;
import io.mindspce.outerfieldsserver.components.npc.TravelController;
import io.mindspce.outerfieldsserver.components.player.*;
import io.mindspce.outerfieldsserver.components.primatives.ComponentSystem;
import io.mindspce.outerfieldsserver.components.primatives.SimpleEmitter;
import io.mindspce.outerfieldsserver.components.primatives.SimpleListener;
import io.mindspce.outerfieldsserver.components.serialization.CharacterSerializer;
import io.mindspce.outerfieldsserver.components.world.*;
import io.mindspce.outerfieldsserver.core.GameSettings;
import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.entities.NonPlayerEntity;
import io.mindspce.outerfieldsserver.entities.PositionalEntity;
import io.mindspce.outerfieldsserver.entities.PlayerEntity;
import io.mindspce.outerfieldsserver.enums.*;
import io.mindspce.outerfieldsserver.systems.event.EventType;
import io.mindspice.mindlib.data.geometry.IConcurrentPQuadTree;
import io.mindspice.mindlib.data.geometry.IPolygon2;
import io.mindspice.mindlib.data.geometry.IRect2;
import io.mindspice.mindlib.data.geometry.IVector2;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;


public class ComponentFactory {

    public static GlobalPosition addGlobalPosition(Entity entity) {
        if (entity.hasAttachedComponent(ComponentType.GLOBAL_POSITION)) {
            //TODO debug log this
            return ComponentType.GLOBAL_POSITION.castOrNull(
                    entity.getComponent(ComponentType.GLOBAL_POSITION).getFirst());
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

    public static ActiveEntities addActiveEntityGrid(Entity entity, int initialSetSize, IRect2 areaRect, int maxPerQuad) {
        ActiveEntities grid = new ActiveEntities(entity, initialSetSize, areaRect, maxPerQuad);
        entity.addComponent(grid);
        return grid;
    }

    public static CollisionGrid addCollisionGrid(Entity entity, IConcurrentPQuadTree<IPolygon2> quadTree) {
        CollisionGrid grid = new CollisionGrid(entity, quadTree);
        entity.addComponent(grid);
        return grid;
    }

    public static AreaEntities addTrackedEntities(Entity entity, List<Entity> trackedEntities) {
        var tracker = new AreaEntities(entity, trackedEntities);
        entity.addComponent(tracker);
        return tracker;
    }

    public static AreaEntities addTrackedEntities(Entity entity) {
        var tracker = new AreaEntities(entity);
        entity.addComponent(tracker);
        return tracker;
    }

    public static ViewRect addViewRect(PositionalEntity entity, IVector2 size, IVector2 position, boolean emitMutable) {
        GlobalPosition globalPosition = ComponentType.GLOBAL_POSITION.castOrNull(
                entity.getComponent(ComponentType.GLOBAL_POSITION).getFirst()
        );
        if (globalPosition == null) {
            throw new IllegalStateException("Entity must have an existing GlobalPosition component to add ViewRect, " +
                    "Use AreaMonitor for static entities");
        }
        ViewRect viewRect = new ViewRect(entity, size, position, emitMutable);
        globalPosition.registerOutputHook(EventType.ENTITY_POSITION_CHANGED, viewRect::onSelfPositionChanged, false);
        return viewRect;
    }

    public static class System {

        public static void attachPlayerEntityComponents(PlayerEntity entity, IVector2 currPosition, AreaId currArea,
                List<EntityState> initStates, ClothingItem[] initOutFit, WebSocketSession webSocketSession) {
            GlobalPosition globalPosition = ComponentType.GLOBAL_POSITION.castOrNull(
                    entity.getComponent(ComponentType.GLOBAL_POSITION).getFirst()
            );
            if (globalPosition == null) { throw new IllegalStateException("Entity must have an existing GlobalPosition component"); }

            ViewRect viewRect = new ViewRect(entity, GameSettings.GET().playerViewWithBuffer(), currPosition, true);
            LocalTileGrid localTileGrid = new LocalTileGrid(entity, 5, currArea);
            PlayerSession playerSession = new PlayerSession(entity, webSocketSession);
            KnownEntities knownEntities = new KnownEntities(entity);
            PlayerNetOut playerNetOut = new PlayerNetOut(entity, playerSession, viewRect, knownEntities);
            PlayerMovement playerMovement = new PlayerMovement(entity, currPosition, localTileGrid.tileGrid(),
                    viewRect.getRect(), playerNetOut::authCorrection, globalPosition::updatePosition
            );

            ComponentSystem playerNetInSystem = new ComponentSystem(
                    entity,
                    List.of(playerMovement, globalPosition, viewRect, localTileGrid, playerSession, knownEntities, playerNetOut),
                    EventProcMode.PASS_THROUGH
            ).withComponentName("PlayerNetworkController");

            EntityStateComp stateComp = new EntityStateComp(entity, initStates);
            CharacterOutfit outfit = new CharacterOutfit(entity, initOutFit);
            CharacterSerializer characterSerializer = new CharacterSerializer(
                    entity,
                    globalPosition::currPosition,
                    stateComp::currStates,
                    outfit::currOutfit
            );

            globalPosition.registerOutputHook(EventType.ENTITY_POSITION_CHANGED, viewRect::onSelfPositionChanged, false);
            globalPosition.registerOutputHook(EventType.ENTITY_POSITION_CHANGED, localTileGrid::onSelfPositionChanged, false);
            globalPosition.registerOutputHook(EventType.ENTITY_AREA_CHANGED, localTileGrid::onSelfAreaChanged, false);
            globalPosition.registerOutputHook(EventType.ENTITY_AREA_CHANGED, knownEntities::onPlayerAreaChanged, false);
            viewRect.registerOutputHook(EventType.ENTITY_VIEW_RECT_CHANGED, knownEntities::onSelfViewRectChanged, false);


            entity.addComponents(List.of(playerNetInSystem, stateComp, outfit, characterSerializer, playerNetOut));
        }

        public static  void attachNPCComponents(NonPlayerEntity entity, IVector2 currPosition,
                List<EntityState> initStates, ClothingItem[] initOutFit, IVector2 viewRectSize) {

            GlobalPosition globalPosition = ComponentType.GLOBAL_POSITION.castOrNull(
                    entity.getComponent(ComponentType.GLOBAL_POSITION).getFirst()
            );
            if (globalPosition == null) { throw new IllegalStateException("Entity must have an existing GlobalPosition component"); }

            ViewRect viewRect = new ViewRect(entity, viewRectSize, currPosition, true);
            TravelController travelController = new TravelController(entity, currPosition, globalPosition::currPosition);
            EntityStateComp stateComp = new EntityStateComp(entity, initStates);
            CharacterOutfit outfit = new CharacterOutfit(entity, initOutFit);
            CharacterSerializer characterSerializer = new CharacterSerializer(
                    entity,
                    globalPosition::currPosition,
                    stateComp::currStates,
                    outfit::currOutfit
            );

            travelController.registerOutputHook(EventType.ENTITY_POSITION_UPDATE, globalPosition::onPositionUpdate, true);
            globalPosition.registerOutputHook(EventType.ENTITY_POSITION_CHANGED, viewRect::onSelfPositionChanged, false);

            entity.addComponents(List.of(travelController, stateComp, outfit, characterSerializer));
        }
    }


}
