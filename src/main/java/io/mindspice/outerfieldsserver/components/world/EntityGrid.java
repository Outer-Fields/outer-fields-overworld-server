package io.mindspice.outerfieldsserver.components.world;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import io.mindspice.mindlib.data.tuples.Pair;
import io.mindspice.outerfieldsserver.components.Component;
import io.mindspice.outerfieldsserver.components.logic.PredicateLib;
import io.mindspice.outerfieldsserver.core.singletons.EntityManager;
import io.mindspice.outerfieldsserver.entities.Entity;
import io.mindspice.outerfieldsserver.enums.AreaId;
import io.mindspice.outerfieldsserver.enums.ComponentType;
import io.mindspice.outerfieldsserver.systems.event.EventData;
import io.mindspice.outerfieldsserver.systems.event.Event;
import io.mindspice.outerfieldsserver.systems.event.EventType;
import io.mindspice.mindlib.data.geometry.IRect2;
import io.mindspice.mindlib.data.geometry.IVector2;
import io.mindspice.mindlib.data.geometry.IVectorQuadTree;
import io.mindspice.mindlib.data.geometry.QuadItemId;
import io.mindspice.mindlib.functional.consumers.BiPredicatedBiConsumer;

import java.util.List;


public class EntityGrid extends Component<EntityGrid> {
    public final IVectorQuadTree<Entity> entityGrid;

    public EntityGrid(Entity parentEntity, IRect2 gridArea, int maxPerQuad) {
        super(parentEntity, ComponentType.ENTITY_GRID, List.of());
        entityGrid = new IVectorQuadTree<>(gridArea, maxPerQuad);

        registerListener(EventType.NEW_POSITIONAL_ENTITY, BiPredicatedBiConsumer.of(
                PredicateLib::isSameAreaEvent, EntityGrid::onNewEntityEvent
        ));
        registerListener(EventType.ENTITY_POSITION_CHANGED, BiPredicatedBiConsumer.of(
                PredicateLib::isSameAreaEvent, EntityGrid::onEntityPositionChanged
        ));

        registerListener(EventType.ENTITY_DESTROY, BiPredicatedBiConsumer.of(
                PredicateLib::isSameAreaEvent, EntityGrid::onDestroyEntity
        ));

        registerListener(EventType.ENTITY_AREA_CHANGED, EntityGrid::onEntityAreaChanged);
        registerListener(EventType.ENTITY_GRID_QUERY, EntityGrid::onEntityGridQuery);

    }

    public void onNewEntityEvent(Event<EventData.NewPositionalEntity> event) {
        entityGrid.insert(event.data().entity().entityId(), event.data().position(), event.data().entity());
    }

    public void onEntityAreaChanged(Event<EventData.EntityAreaChanged> event) {
        if (event.data().oldArea() == areaId()) {
            entityGrid.remove(event.issuerEntityId());
        }
        if (event.data().newArea() == areaId()) {
            Entity entity = EntityManager.GET().entityById(event.issuerEntityId());
            if (entity == null) {
                // TODO LOG THIS
                return;
            }
            entityGrid.insert(entity.entityId(), event.data().position(), entity);
        }
    }

    public void onEntityPositionChanged(Event<EventData.EntityPositionChanged> event) {
        entityGrid.update(event.issuerEntityId(), event.data().newPosition());
    }

    public void onDestroyEntity(Event<Entity> event) {
        entityGrid.remove(event.data().entityId());
    }


    public void addActiveEntity(Entity entity, IVector2 position) {
        entityGrid.insert(entity.entityId(), position, entity);
    }

    public void removeEntity(int id) {
        entityGrid.remove(id);
    }

    public void onEntityGridQuery(Event<IRect2> event) {
        emitEvent(Event.responseEvent(
                this,
                event,
                EventType.ENTITY_GRID_RESPONSE,
                entityGrid.query(event.data()).stream().mapToInt(QuadItemId::id).toArray())
        );
    }


}
