package io.mindspce.outerfieldsserver.components;

import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.enums.ComponentType;
import io.mindspce.outerfieldsserver.networking.NetSerializable;
import io.mindspce.outerfieldsserver.systems.event.Event;
import io.mindspce.outerfieldsserver.systems.event.EventType;
import io.mindspice.mindlib.data.collections.lists.primative.IntList;
import io.mindspice.mindlib.data.tuples.Pair;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;


// NOTE needs a listener added for new Entities or area switches depending on context;

public class TrackedEntities<T extends Entity> extends Component<TrackedEntities<T>> {

    private final List<T> entities = new ArrayList<>(50);
    private final List<NetSerializer> serializableEntities = new ArrayList<>(50);
    private int byteSize = -1;

    public TrackedEntities(Entity parentEntity) {
        super(parentEntity, ComponentType.TRACKED_ENTITIES,
                List.of(EventType.SYSTEM_ENTITIES_RESP, EventType.SERIALIZED_ENTITIES_RESP)
        );
    }

    public TrackedEntities(Entity parentEntity, List<T> trackedEntities) {
        super(parentEntity, ComponentType.TRACKED_ENTITIES,
                List.of(EventType.SYSTEM_ENTITIES_RESP, EventType.SERIALIZED_ENTITIES_RESP)
        );
        addEntities(trackedEntities);
    }

    public void onSystemEntitiesReq(Event<Predicate<? super Entity>> event) {
        var respList = entities.stream().filter(event.data()).toList();
        emitEvent(Event.responseEvent(this, event, EventType.SYSTEM_ENTITIES_RESP, respList));
    }

    public void onSerializedEntitiesReq(Event<Predicate<NetSerializer>> event) {
        if (serializableEntities.isEmpty()) { return; }
        if (byteSize == -1) {
            byteSize = serializableEntities.getFirst().serializable.stream().mapToInt(NetSerializable::byteSize).sum();
        }

        ByteBuffer buffer = NetSerializable.getEmptyBuffer(serializableEntities.size() * byteSize);
        IntList ids = new IntList(serializableEntities.size());
        for (var entity : serializableEntities) {
            if (event.data().test(entity)) {
                entity.serializeToBuffer(buffer);
            }
        }
        byte[] finalBytes = NetSerializable.trimBufferToBytes(buffer);
        emitEvent(Event.responseEvent(this, event, EventType.SERIALIZED_ENTITIES_RESP, Pair.of(ids, finalBytes)));
    }

    public void addEntity(T entity) {
        entities.add(entity);
        if (entity.hasAttachedComponent(ComponentType.NET_SERIALIZER)) {
            NetSerializer ns = ComponentType.NET_SERIALIZER.castOrNull(
                    entity.getComponent(ComponentType.NET_SERIALIZER).getFirst()
            );
            if (ns == null) {
                // TODO log this;
            }
            serializableEntities.add(ns);
        }
    }

    public void removeEntity(int entityId) {
        entities.removeIf(e -> e.entityId() == entityId);
        serializableEntities.removeIf(e -> e.entityId() == entityId);
    }

    public void addEntities(List<T> entityList) {
        entityList.forEach(this::addEntity);
    }

    public void removeEntities(IntList entityList) {
        entityList.forEach(this::removeEntity);
    }
}
    
    
    
