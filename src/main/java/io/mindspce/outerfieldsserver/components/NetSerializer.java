package io.mindspce.outerfieldsserver.components;

import io.mindspce.outerfieldsserver.components.logic.PredicateLib;
import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.enums.ComponentType;
import io.mindspce.outerfieldsserver.networking.NetSerializable;
import io.mindspce.outerfieldsserver.systems.event.Event;
import io.mindspce.outerfieldsserver.systems.event.EventType;
import io.mindspice.mindlib.functional.consumers.BiPredicatedBiConsumer;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.function.BiConsumer;


public class NetSerializer extends Component<NetSerializer> {
    List<NetSerializable> serializable;

    public NetSerializer(Entity parentEntity, List<NetSerializable> serializable) {
        super(parentEntity, ComponentType.NET_SERIALIZER, List.of());
        this.serializable = serializable;
        registerListener(EventType.SERIALIZED_ENTITY_REQUEST, BiPredicatedBiConsumer.of(
                PredicateLib::isRecEntitySame,
                NetSerializer::onSerializationRequest
        ));
    }

    public void onSerializationRequest(Event<Object> event) {
        Event.responseEvent(this, event, EventType.SERIALIZED_ENTITY_RESPONSE, serializeToBytes());
    }

    public void serializeToBuffer(ByteBuffer buffer) {
        buffer.put(entityType().value);
        buffer.putInt(entityId());
        buffer.put(entityName().getBytes());
        serializable.forEach(s -> s.addBytesToBuffer(buffer));
    }

    public byte[] serializeToBytes() {
        var buffer = NetSerializable.getEmptyBuffer(serializable.stream()
                .mapToInt(NetSerializable::byteSize).sum());
        serializable.forEach(s -> s.addBytesToBuffer(buffer));
        return buffer.array();
    }

}
