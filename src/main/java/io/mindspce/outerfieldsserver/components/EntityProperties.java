package io.mindspce.outerfieldsserver.components;

import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.enums.ComponentType;
import io.mindspce.outerfieldsserver.networking.NetSerializable;
import io.mindspce.outerfieldsserver.systems.event.Event;
import io.mindspce.outerfieldsserver.systems.event.EventType;

import java.nio.ByteBuffer;
import java.util.List;


public class EntityProperties extends Component<EntityProperties> implements NetSerializable {
    public final int key;
    public String name;

    public EntityProperties(Entity parentEntity, int key, String name) {
        super(parentEntity, ComponentType.EntityProperties, List.of(EventType.ENTITY_NAME_CHANGE));
        this.key = key;
        this.name = name;
        registerListener(EventType.ENTITY_NAME_UPDATE, EntityProperties::onNameChange);
    }

    public void setName(String name) {
        this.name = name;
        emitEvent(Event.entityNameChange(this, name));
    }

    public void onNameChange(Event<String> event) {
        setName(event.data());
    }

    @Override
    public int byteSize() {
        return 0;
    }

    @Override
    public byte[] getBytes() {
        ByteBuffer buffer = NetSerializable.getEmptyBuffer(4 + name.getBytes().length);
        buffer.put(name.getBytes());
        buffer.putInt(key);
        return buffer.array();
    }

    @Override
    public void addBytesToBuffer(ByteBuffer buffer) {
        buffer.put(name.getBytes());
        buffer.putInt(key);
    }
}
