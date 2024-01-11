package io.mindspce.outerfieldsserver.components.entity;

import io.mindspce.outerfieldsserver.components.Component;
import io.mindspce.outerfieldsserver.components.logic.PredicateLib;
import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.enums.ComponentType;
import io.mindspce.outerfieldsserver.networking.NetSerializable;
import io.mindspce.outerfieldsserver.systems.event.Event;
import io.mindspce.outerfieldsserver.systems.event.EventType;
import io.mindspice.mindlib.data.tuples.Pair;
import io.mindspice.mindlib.functional.consumers.BiPredicatedBiConsumer;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Supplier;


public class EntityProperties extends Component<EntityProperties> implements NetSerializable {
    public final Map<String, String> properties = new HashMap<>(2);
    public Consumer<ByteBuffer> serializer;
    public int byteSize;

    public EntityProperties(Entity parentEntity) {
        super(parentEntity, ComponentType.EntityProperties, List.of(EventType.ENTITY_PROPERTY_CHANGE));
        registerListener(EventType.ENTITY_PROPERTY_UPDATE, BiPredicatedBiConsumer.of(
                PredicateLib::isRecEntitySame, EntityProperties::onPropertyUpdate
        ));
    }

    public void onPropertyUpdate(Event<Pair<String, String>> event) {
        properties.put(event.data().first(), event.data().second());
        emitEvent(Event.entityPropertyChanged(this, areaId(), event.data()));
    }

    public void setSerializer(int byteSize, List<String> fields) {
        this.byteSize = byteSize;
        fields.forEach(f -> {
            if (!properties.containsKey(f)) {
                throw new IllegalStateException("Properties does not contain field");
            }
        });
        serializer = (ByteBuffer b) -> {
            for (var field : fields) {
                b.put(field.getBytes());
            }
        };
    }

    public Optional<Supplier<String>> getSupplier(String field) {
        if (!properties.containsKey(field)) {
            return Optional.empty();
        }
        return Optional.of(() -> properties.get(field));
    }

    @Override
    public int byteSize() {
        return byteSize;
    }

    @Override
    public void addBytesToBuffer(ByteBuffer buffer) {

    }
}
