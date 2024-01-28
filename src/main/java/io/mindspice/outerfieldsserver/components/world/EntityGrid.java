package io.mindspice.outerfieldsserver.components.world;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
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
    public TIntSet activeEntities;
    public IVectorQuadTree<Entity> entityGrid;

    public EntityGrid(Entity parentEntity, int initialSetSize, IRect2 gridArea, int maxPerQuad) {
        super(parentEntity, ComponentType.ACTIVE_ENTITIES, List.of());
        activeEntities = new TIntHashSet(100);
        entityGrid = new IVectorQuadTree<>(gridArea, maxPerQuad);

        registerListener(
                EventType.NEW_ENTITY,
                BiPredicatedBiConsumer.of(PredicateLib::isSameAreaEvent, EntityGrid::onNewEntityEvent)
        );
        registerListener(
                EventType.ENTITY_POSITION_CHANGED,
                BiPredicatedBiConsumer.of(PredicateLib::isSameAreaEvent, EntityGrid::onEntityPositionChanged)
        );
        registerListener(
                EventType.AREA_ENTITIES_QUERY,
                BiPredicatedBiConsumer.of(PredicateLib::isSameAreaEvent, EntityGrid::onAreaEntitiesQuery)
        );
        registerListener(EventType.ENTITY_AREA_CHANGED, EntityGrid::onEntityAreaChanged);
        registerListener(EventType.ENTITY_GRID_QUERY, EntityGrid::onEntityGridQuery);

    }

    public void onNewEntityEvent(Event<EventData.NewEntity> event) {
        addActiveEntity(event.data().entity(), event.data().position());
    }

    public void onEntityAreaChanged(Event<EventData.EntityAreaChanged> event) {
        if (event.data().oldArea() == areaId()) {
            removeActiveEntity(event.issuerEntityId());
        }
        if (event.data().newArea() == areaId()) {
            addActiveEntity(EntityManager.GET().entityById(event.issuerEntityId()), event.data().position());
        }
    }

    public void onEntityPositionChanged(Event<EventData.EntityPositionChanged> event) {
        entityGrid.update(event.issuerEntityId(), event.data().newPosition());
    }

    public void addActiveEntity(Entity entity, IVector2 position) {
        if (activeEntities.contains(entity.entityId())) {
            // TODO log this important error
            // this will trigger on if a new player event is sent and then a new area event for the same player-area
            return;
        }
        activeEntities.add(entity.entityId());
        entityGrid.insert(entity.entityId(), position, entity);
    }

    public void removeActiveEntity(int id) {
        activeEntities.remove(id);
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

    public void onAreaEntitiesQuery(Event<AreaId> event) {
        emitEvent(Event.responseEvent(this, event, EventType.SYSTEM_ENTITIES_RESP, activeEntities.toArray()));
    }


}
