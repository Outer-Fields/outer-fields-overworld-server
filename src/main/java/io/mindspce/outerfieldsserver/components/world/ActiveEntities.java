package io.mindspce.outerfieldsserver.components.world;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import io.mindspce.outerfieldsserver.components.Component;
import io.mindspce.outerfieldsserver.components.logic.PredicateLib;
import io.mindspce.outerfieldsserver.core.singletons.EntityManager;
import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.enums.ComponentType;
import io.mindspce.outerfieldsserver.systems.EventData;
import io.mindspce.outerfieldsserver.systems.event.Event;
import io.mindspce.outerfieldsserver.systems.event.EventType;
import io.mindspice.mindlib.data.geometry.IRect2;
import io.mindspice.mindlib.data.geometry.IVector2;
import io.mindspice.mindlib.data.geometry.IVectorQuadTree;
import io.mindspice.mindlib.data.geometry.QuadItemId;
import io.mindspice.mindlib.functional.consumers.BiPredicatedBiConsumer;

import java.util.List;


public class ActiveEntities extends Component<ActiveEntities> {
    public TIntSet activeEntities;
    public IVectorQuadTree<Entity> entityGrid;

    public ActiveEntities(Entity parentEntity, int initialSetSize, IRect2 gridArea, int maxPerQuad) {
        super(parentEntity, ComponentType.ACTIVE_ENTITIES, List.of());
        activeEntities = new TIntHashSet(100);
        entityGrid = new IVectorQuadTree<>(gridArea, maxPerQuad);
        registerListener(
                EventType.NEW_ENTITY,
                BiPredicatedBiConsumer.of(PredicateLib::isSameAreaEvent, ActiveEntities::onNewEntityEvent)
        );
        registerListener(
                EventType.ENTITY_POSITION_CHANGED,
                BiPredicatedBiConsumer.of(PredicateLib::isSameAreaEvent, ActiveEntities::onEntityPositionChanged)
        );
        registerListener(EventType.ENTITY_AREA_CHANGED, ActiveEntities::onEntityAreaChanged);
        registerListener(EventType.ENTITY_GRID_QUERY, ActiveEntities::onEntityGridQuery);
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
        System.out.println("Added active entity");
        activeEntities.add(entity.entityId());
        entityGrid.insert(entity.entityId(), position, entity);

    }

    public void onEntityGridQuery(Event<IRect2> event) {
        emitEvent(Event.responseEvent(
                this,
                event,
                EventType.ENTITY_GRID_RESPONSE,
                entityGrid.query(event.data()).stream().mapToInt(QuadItemId::id).toArray())
        );
    }

    public void removeActiveEntity(int id) {
        activeEntities.remove(id);
        entityGrid.remove(id);
    }
}
