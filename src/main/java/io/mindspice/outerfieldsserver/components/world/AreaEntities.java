package io.mindspice.outerfieldsserver.components.world;

import io.mindspice.outerfieldsserver.components.Component;
import io.mindspice.outerfieldsserver.entities.Entity;
import io.mindspice.outerfieldsserver.enums.ComponentType;
import io.mindspice.outerfieldsserver.systems.event.EventType;

import java.util.List;

// NOTE needs a listener added for new Entities or area switches depending on context;


public class AreaEntities extends Component<AreaEntities> {
    public AreaEntities(Entity parentEntity, ComponentType componentType, List<EventType> emittedEvents) {
        super(parentEntity, componentType, emittedEvents);
    }

//    private final Set<PlayerEntity> PlayerEntities = new HashSet<>(50);
//    private final Set<NonPlayerEntity> NonPlayerEntities = new HashSet<>(50);
//    private final Set<LocationEntities> areaLocationEntities = new HashSet<>()
//
//    public AreaEntities(Entity parentEntity) {
//        super(parentEntity, ComponentType.AREA_ENTITIES,
//                List.of(EventType.SYSTEM_ENTITIES_RESP)
//        );
//    }
//
//    public AreaEntities(Entity parentEntity, List<Entity> trackedEntities) {
//        super(parentEntity, ComponentType.AREA_ENTITIES,
//                List.of(EventType.AREA_ENTITIES_RESPONSE)
//        );
//        addEntities(trackedEntities);
//        registerListener(EventType.NEW_ENTITY, BiPredicatedBiConsumer.of(
//                PredicateLib::isSameAreaEvent, AreaEntities::onNewEntity
//        ));
//        registerListener(EventType.ENTITY_AREA_CHANGED, AreaEntities::onEntityAreaChange);
//        registerListener(EventType.AREA_ENTITIES_QUERY, BiPredicatedBiConsumer.of(
//                PredicateLib::isSameAreaEvent, AreaEntities::onAreaEntitiesQuery
//        ));
//    }
//
//    public void onNewEntity(Event<EventData.NewEntity> event) {
//        Entity entity = event.data().entity();
//        entities.add(entity);
//    }
//
//    public void onEntityAreaChange(Event<EventData.EntityAreaChanged> event) {
//        if (event.data().oldArea() == areaId()) {
//            entities.removeIf(e -> e.entityId() == event.issuerEntityId());
//        }
//        if (event.data().newArea() == areaId()) {
//            Entity entity = EntityManager.GET().entityById(event.issuerEntityId());
//            entities.add(entity);
//        }
//    }
//
//    public void onAreaEntitiesQuery(Event<AreaId> event) {
//        emitEvent(Event.responseEvent(this, event, EventType.SYSTEM_ENTITIES_RESP, new ArrayList<>(entities)));
//    }
//
//    public void addEntity(Entity entity) {
//        entities.add(entity);
//    }
//
//    public void removeEntity(int entityId) {
//        entities.removeIf(e -> e.entityId() == entityId);
//    }
//
//    public void addEntities(List<Entity> entityList) {
//        entityList.forEach(this::addEntity);
//    }
//
//    public void removeEntities(IntList entityList) {
//        entityList.forEach(this::removeEntity);
//    }
}
    
    
    
