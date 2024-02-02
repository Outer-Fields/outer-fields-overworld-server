package io.mindspice.outerfieldsserver.components.world;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import io.mindspice.mindlib.functional.consumers.BiPredicatedBiConsumer;
import io.mindspice.outerfieldsserver.components.Component;
import io.mindspice.outerfieldsserver.components.logic.PredicateLib;
import io.mindspice.outerfieldsserver.entities.Entity;
import io.mindspice.outerfieldsserver.enums.AreaId;
import io.mindspice.outerfieldsserver.enums.ComponentType;
import io.mindspice.outerfieldsserver.systems.event.Event;
import io.mindspice.outerfieldsserver.systems.event.EventData;
import io.mindspice.outerfieldsserver.systems.event.EventType;

import java.util.List;


public class ActiveEntities extends Component<ActiveEntities> {
    public final TIntSet activeEntities;

    public ActiveEntities(Entity parentEntity, int initialSetSize) {
        super(parentEntity, ComponentType.ACTIVE_ENTITIES, List.of());
        this.activeEntities = new TIntHashSet(initialSetSize);

        registerListener(EventType.NEW_POSITIONAL_ENTITY, BiPredicatedBiConsumer.of(
                PredicateLib::isSameAreaEvent, ActiveEntities::onNetEntity
        ));
        registerListener(EventType.ENTITY_DESTROY, BiPredicatedBiConsumer.of(
                PredicateLib::isSameAreaEvent, ActiveEntities::onDestroyEntity
        ));
        registerListener(EventType.AREA_ENTITIES_QUERY, BiPredicatedBiConsumer.of(
                PredicateLib::isSameAreaEvent, ActiveEntities::onAreaEntitiesQuery
        ));
        registerListener(EventType.ENTITY_AREA_CHANGED, ActiveEntities::onEntityAreaChanged);

    }

    public void onDestroyEntity(Event<Entity> event) {
        activeEntities.remove(event.data().entityId());
    }

    public void onEntityAreaChanged(Event<EventData.EntityAreaChanged> event) {
        if (event.data().oldArea() == areaId()) {
            activeEntities.remove(event.issuerEntityId());
        }
        if (event.data().newArea() == areaId()) {
            if (activeEntities.contains(event.issuerEntityId())) {
                // TODO log this
            }
            activeEntities.add(event.issuerEntityId());
        }
    }

    public void onNetEntity(Event<EventData.NewPositionalEntity> event) {
        activeEntities.add(event.data().entity().entityId());
    }

    public void onAreaEntitiesQuery(Event<AreaId> event) {
        emitEvent(Event.responseEvent(this, event, EventType.SYSTEM_ENTITIES_RESP, activeEntities.toArray()));
    }
}
