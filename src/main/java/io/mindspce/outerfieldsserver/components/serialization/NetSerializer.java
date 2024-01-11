package io.mindspce.outerfieldsserver.components.serialization;

import io.mindspce.outerfieldsserver.components.Component;
import io.mindspce.outerfieldsserver.components.logic.PredicateLib;
import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.enums.ComponentType;
import io.mindspce.outerfieldsserver.networking.NetSerializable;
import io.mindspce.outerfieldsserver.systems.event.Event;
import io.mindspce.outerfieldsserver.systems.event.EventType;
import io.mindspice.mindlib.functional.consumers.BiPredicatedBiConsumer;

import java.nio.ByteBuffer;
import java.util.List;


public class NetSerializer extends Component<NetSerializer> {
    public List<NetSerializable> serializable;
    public final int byteSize;

    public NetSerializer(Entity parentEntity, List<NetSerializable> serializable) {
        super(parentEntity, ComponentType.NET_SERIALIZER, List.of());
        this.serializable = serializable;
        registerListener(EventType.SERIALIZED_ENTITY_REQUEST, BiPredicatedBiConsumer.of(
                PredicateLib::isRecEntitySame,
                NetSerializer::onSerializationRequest
        ));
        byteSize = serializable.stream().mapToInt(NetSerializable::byteSize).sum();
    }

    public void onSerializationRequest(Event<Integer> event) {
        Event.responseEvent(this, event, EventType.SERIALIZED_ENTITY_RESP, serializeToBytes());
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
        return NetSerializable.trimBufferToBytes(buffer);
    }

}
