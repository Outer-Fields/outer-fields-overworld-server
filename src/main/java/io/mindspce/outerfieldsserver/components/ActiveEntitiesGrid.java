package io.mindspce.outerfieldsserver.components;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import io.mindspce.outerfieldsserver.components.logic.PredicateLib;
import io.mindspce.outerfieldsserver.core.singletons.EntityManager;
import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.enums.ComponentType;
import io.mindspce.outerfieldsserver.enums.QueryType;
import io.mindspce.outerfieldsserver.systems.EventData;
import io.mindspce.outerfieldsserver.systems.event.Event;
import io.mindspce.outerfieldsserver.systems.event.EventType;
import io.mindspice.mindlib.data.geometry.IRect2;
import io.mindspice.mindlib.data.geometry.IVector2;
import io.mindspice.mindlib.data.geometry.IVectorQuadTree;
import io.mindspice.mindlib.functional.consumers.BiPredicatedBiConsumer;

import java.util.List;


public class ActiveEntitiesGrid extends Component<ActiveEntitiesGrid> {
    public TIntSet activeEntities;
    public IVectorQuadTree<Entity> entityGrid;

    public ActiveEntitiesGrid(Entity parentEntity, int initialSetSize, IRect2 gridArea, int maxPerQuad) {
        super(parentEntity, ComponentType.ACTIVE_ENTITIES, List.of());
        activeEntities = new TIntHashSet(100);
        entityGrid = new IVectorQuadTree<>(gridArea, maxPerQuad);
        registerListener(EventType.NEW_ENTITY, BiPredicatedBiConsumer.of(PredicateLib::isSameAreaEvent, ActiveEntitiesGrid::onNewEntityEvent));
        registerListener(EventType.ENTITY_AREA_CHANGED, ActiveEntitiesGrid::onEntityAreaChanged);
        registerListener(EventType.ENTITY_POSITION_CHANGED, BiPredicatedBiConsumer.of(PredicateLib::isSameAreaEvent, ActiveEntitiesGrid::onEntityPositionChanged));
        registerQueryListener(QueryType.AREA_ACTIVE_PLAYERS, BiPredicatedBiConsumer.of(PredicateLib::isSameAreaEvent, ActiveEntitiesGrid::onActivePlayersQuery));

    }

    public void onNewEntityEvent(Event<EventData.NewEntity> event) {
        addActiveEntity(EntityManager.GET().getEntityById(event.issuerEntityId()), event.data().position());
    }

    public void onEntityAreaChanged(Event<EventData.EntityAreaChanged> event) {
        if (event.data().oldArea() == areaId()) {
            removeActiveEntity(event.issuerEntityId());
        }
        if (event.data().newArea() == areaId()) {
            addActiveEntity(EntityManager.GET().getEntityById(event.issuerEntityId()), event.data().position());
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
        entityGrid.insert(entityId(), position, entity);
    }

    public void onActivePlayersQuery(Event<EventData.Query<?, TIntSet, IVector2>> query) {

        var queryResponse = new EventData.QueryResponse<>(new TIntHashSet(activeEntities), query.data().queryCallBack());
        emitEvent(Event.Factory.newQueryResponse(this, queryResponse, query.issuerEntityId(), query.issuerComponentId()));
    }

    public void removeActiveEntity(int id) {
        activeEntities.remove(id);
        entityGrid.remove(id);
    }
}
