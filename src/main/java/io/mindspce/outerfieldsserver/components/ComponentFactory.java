package io.mindspce.outerfieldsserver.components;

import io.mindspce.outerfieldsserver.area.ChunkEntity;
import io.mindspce.outerfieldsserver.core.GameSettings;
import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.entities.PositionalEntity;
import io.mindspce.outerfieldsserver.enums.ComponentType;
import io.mindspce.outerfieldsserver.enums.EventProcMode;
import io.mindspce.outerfieldsserver.enums.QueryType;
import io.mindspce.outerfieldsserver.systems.event.EventType;
import io.mindspice.mindlib.data.geometry.IConcurrentPQuadTree;
import io.mindspice.mindlib.data.geometry.IPolygon2;
import io.mindspice.mindlib.data.geometry.IRect2;
import io.mindspice.mindlib.data.geometry.IVector2;

import java.util.List;


public class ComponentFactory {

    public static GlobalPosition addGlobalPosition(Entity entity) {
        if (entity.hasAttachedComponent(ComponentType.GLOBAL_POSITION)) {
            //TODO debug log this
            return ComponentType.GLOBAL_POSITION.castComponent(
                    entity.getComponent(ComponentType.GLOBAL_POSITION).getFirst());
        }
        GlobalPosition globalPosition = new GlobalPosition(entity);
        entity.addComponent(globalPosition);
        return globalPosition;
    }

    public static SimpleListener addSimpleListener(Entity entity, List<QueryType> queryTypes) {
        SimpleListener listener = new SimpleListener(entity, queryTypes);
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

    public static ActiveEntitiesGrid addActiveEntityGrid(Entity entity, int initialSetSize, IRect2 areaRect, int maxPerQuad) {
        ActiveEntitiesGrid grid = new ActiveEntitiesGrid(entity, initialSetSize, areaRect, maxPerQuad);
        entity.addComponent(grid);
        return grid;
    }

    public static CollisionGrid addCollisionGrid(Entity entity, IConcurrentPQuadTree<IPolygon2> quadTree) {
        CollisionGrid grid = new CollisionGrid(entity, quadTree);
        entity.addComponent(grid);
        return grid;
    }

    public static ViewRect addViewRect(PositionalEntity entity, IVector2 size, IVector2 position, boolean emitMutable) {
        GlobalPosition globalPosition = ComponentType.GLOBAL_POSITION.castComponent(
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

        /**
         * Returns a movement subsystem for a player entity, include the following:
         * <br><br>
         * PlayerMovement component: Accepts and validates player movement packets, broadcast the result to the event system.
         * <br><br>
         * ViewRect component: Provides the coordinates and rect of the players view buffer, used to query entities and for
         * collision validation area.
         * <br><br>
         * LocalTileGrid component: Use for collision testing, stays update with a 5x5 tile grid around the player that can be
         * queried for existing collisions
         * <br><br>
         * GlobalPosition component: Keeps track of the players position in the world. Receives the PLAYER_VALID_MOVEMENT event
         * directly from PlayerMovement, Transmit Position, Chunk and area updates as needed to the event system.
         * < <br><br>
         * ViewRect and local tile grid emit no events and register hooks into global position to keep their data upto date.
         *
         * @param entity the entity to apply the component to.
         * @return returns the positional entity back, though component is mutably applied.
         */
        static SubSystem playerNetworkIn(PositionalEntity entity) {
            GlobalPosition globalPosition = new GlobalPosition(entity);
            ViewRect viewRect = new ViewRect(
                    entity,
                    GameSettings.GET().playerViewWithBuffer(),
                    IVector2.of(0, 0),
                    true
            );
            // PlayerMovement requires a reference to local tile grid
            LocalTileGrid localTileGrid = new LocalTileGrid(entity, 5);
            PlayerMovement playerMovement = new PlayerMovement(entity, localTileGrid.tileGrid(), viewRect.getRect());

            // Intercept and hook the valid movement event from PlayerMovement to GlobalPosition
            playerMovement.registerOutputHook(EventType.PLAYER_VALID_MOVEMENT, globalPosition::onSelfPositionUpdate, true);

            // Hook into the position and area changed events from global position to the view rect and local tile grid to sync directly
            globalPosition.registerOutputHook(EventType.ENTITY_POSITION_CHANGED, viewRect::onSelfPositionChanged, false);
            globalPosition.registerOutputHook(EventType.ENTITY_POSITION_CHANGED, localTileGrid::onSelfPositionChanged, false);
            globalPosition.registerOutputHook(EventType.ENTITY_AREA_CHANGED, localTileGrid::onSelfAreaChanged, false);

            // Combine into a SubSystem for easy management
            SubSystem componentSystem = new SubSystem(
                    entity,
                    List.of(globalPosition, viewRect, localTileGrid, playerMovement),
                    EventProcMode.PASS_THROUGH
            ).withName("NetworkOutSubSystem");

            entity.addComponent(componentSystem);
            return componentSystem;
        }

        static SubSystem playerNetworkOut(PositionalEntity entity) {
            GlobalPosition globalPosition = ComponentType.GLOBAL_POSITION.castComponent(
                    entity.getComponent(ComponentType.GLOBAL_POSITION).getFirst()
            );
            ViewRect viewRect = ComponentType.VIEW_RECT.castComponent(
                    entity.getComponent(ComponentType.VIEW_RECT).getFirst()
            );
            if (globalPosition == null) {
                throw new IllegalStateException("Entity must have an existing GlobalPosition component to add NetworkOutSubSystem");
            }
            if (viewRect == null) {
                throw new IllegalStateException("Entity must have an existing ViewRectComponent component to add NetworkOutSubSystem");
            }


        }


    }

}
