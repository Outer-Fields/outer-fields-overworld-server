package io.mindspce.outerfieldsserver.components.world;

import io.mindspce.outerfieldsserver.components.Component;
import io.mindspce.outerfieldsserver.components.logic.PredicateLib;
import io.mindspce.outerfieldsserver.components.serialization.NetSerializer;
import io.mindspce.outerfieldsserver.core.singletons.EntityManager;
import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.enums.ComponentType;
import io.mindspce.outerfieldsserver.networking.NetSerializable;
import io.mindspce.outerfieldsserver.systems.EventData;
import io.mindspce.outerfieldsserver.systems.event.Event;
import io.mindspce.outerfieldsserver.systems.event.EventType;
import io.mindspice.mindlib.data.collections.lists.primative.IntList;
import io.mindspice.mindlib.data.tuples.Pair;
import io.mindspice.mindlib.functional.consumers.BiPredicatedBiConsumer;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.IntPredicate;
import java.util.function.Predicate;

// NOTE needs a listener added for new Entities or area switches depending on context;


public class TrackedEntities extends Component<TrackedEntities> {

    private final Set<Entity> entities = new HashSet<>(50);
    private final Set<NetSerializer> serializableEntities = new HashSet<>(50);

    public TrackedEntities(Entity parentEntity) {
        super(parentEntity, ComponentType.TRACKED_ENTITIES,
                List.of(EventType.SYSTEM_ENTITIES_RESP, EventType.SERIALIZED_ENTITIES_RESP)
        );
    }

    public TrackedEntities(Entity parentEntity, List<Entity> trackedEntities) {
        super(parentEntity, ComponentType.TRACKED_ENTITIES,
                List.of(EventType.SYSTEM_ENTITIES_RESP, EventType.SERIALIZED_ENTITIES_RESP)
        );
        addEntities(trackedEntities);
        registerListener(EventType.NEW_ENTITY, BiPredicatedBiConsumer.of(
                PredicateLib::isSameAreaEvent, TrackedEntities::onNewEntity
        ));
        registerListener(EventType.ENTITY_AREA_CHANGED, TrackedEntities::onEntityAreaChange);
        registerListener(EventType.SYSTEM_ENTITIES_QUERY, BiPredicatedBiConsumer.of(
                PredicateLib::isSameAreaEvent, TrackedEntities::onSystemEntitiesReq
        ));
        registerListener(EventType.SERIALIZED_ENTITIES_REQUEST, BiPredicatedBiConsumer.of(
                PredicateLib::isSameAreaEvent, TrackedEntities::onSerializedEntitiesReq
        ));
    }

    public void onNewEntity(Event<EventData.NewEntity> event) {
        Entity entity = event.data().entity();
        entities.add(entity);
        checkAndAddSerializer(entity);
    }

    public void onEntityAreaChange(Event<EventData.EntityAreaChanged> event) {
        if (event.data().oldArea() == areaId()) {
            entities.removeIf(e -> e.entityId() == event.issuerEntityId());
            serializableEntities.removeIf(s -> s.entityId() == event.issuerEntityId());
        }
        if (event.data().newArea() == areaId()) {
            Entity entity = EntityManager.GET().entityById(event.issuerEntityId());
            entities.add(entity);
            checkAndAddSerializer(entity);
        }
    }

    private void checkAndAddSerializer(Entity entity) {
        if (entity.hasAttachedComponent(ComponentType.NET_SERIALIZER)) {
            NetSerializer entitySerializer = ComponentType.NET_SERIALIZER.castOrNull(
                    entity.getComponent(ComponentType.NET_SERIALIZER).getFirst()
            );
            if (entitySerializer == null) {
                System.out.println("Null serializer found");
                //TODO log this
            }
            serializableEntities.add(entitySerializer);
        }
    }

    public void onSystemEntitiesReq(Event<Predicate<? super Entity>> event) {
        var respList = entities.stream().filter(event.data()).toList();
        emitEvent(Event.responseEvent(this, event, EventType.SYSTEM_ENTITIES_RESP, respList));
    }

    public void onSerializedEntitiesReq(Event<IntPredicate> event) {
        if (serializableEntities.isEmpty()) { return; }
        int byteSize = serializableEntities.stream().mapToInt(s -> s.byteSize).sum();

        ByteBuffer buffer = NetSerializable.getEmptyBuffer(serializableEntities.size() * byteSize);
        IntList ids = new IntList(serializableEntities.size());
        for (var entity : serializableEntities) {
            if (event.data().test(entity.entityId())) {
                entity.serializeToBuffer(buffer);
            }
        }
        byte[] finalBytes = NetSerializable.trimBufferToBytes(buffer);
        emitEvent(Event.responseEvent(this, event, EventType.SERIALIZED_ENTITIES_RESP, Pair.of(ids, finalBytes)));
    }

    public void addEntity(Entity entity) {
        entities.add(entity);
        checkAndAddSerializer(entity);
    }

    public void removeEntity(int entityId) {
        entities.removeIf(e -> e.entityId() == entityId);
        serializableEntities.removeIf(e -> e.entityId() == entityId);
    }

    public void addEntities(List<Entity> entityList) {
        entityList.forEach(this::addEntity);
    }

    public void removeEntities(IntList entityList) {
        entityList.forEach(this::removeEntity);
    }
}
    
    
    
