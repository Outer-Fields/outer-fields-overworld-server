package io.mindspice.outerfieldsserver.components.serialization;

import io.mindspice.outerfieldsserver.components.Component;
import io.mindspice.outerfieldsserver.core.networking.proto.EntityProto;
import io.mindspice.outerfieldsserver.entities.Entity;
import io.mindspice.outerfieldsserver.enums.ComponentType;
import io.mindspice.outerfieldsserver.enums.EntityType;
import io.mindspice.outerfieldsserver.systems.event.Event;
import io.mindspice.outerfieldsserver.systems.event.EventType;
import io.mindspice.mindlib.data.geometry.IVector2;
import io.mindspice.mindlib.functional.consumers.BiPredicatedBiConsumer;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;


public class LocationItemSerializer extends Component<LocationItemSerializer> {
    private final Supplier<IVector2> position;
    private final Supplier<int[]> states;
    private final long key;
    private final BooleanSupplier isActiveSupplier;
    private final Function<Integer, Boolean> isVisibleToSupplier;
    private EntityProto.LocationItemEntity lastSerialization;
    private long lastSerializationTime;


    public LocationItemSerializer(Entity parentEntity, long key, Supplier<IVector2> position,
            Supplier<int[]> states, BooleanSupplier isActiveSupplier, Function<Integer, Boolean> isVisibleToSupplier) {
        super(parentEntity, ComponentType.CHARACTER_SERIALIZER,
                List.of(EventType.SERIALIZED_ENTITY_REQUEST, EventType.SERIALIZED_ENTITIES_REQUEST)
        );
        this.position = position;
        this.states = states;
        this.key = key;
        this.isActiveSupplier = isActiveSupplier;
        this.isVisibleToSupplier = isVisibleToSupplier;

        registerListener(EventType.SERIALIZED_ENTITY_REQUEST, BiPredicatedBiConsumer.of(
                (LocationItemSerializer cs, Event<Integer> ev) -> ev.eventArea() == areaId() && ev.recipientEntityId() == entityId(),
                LocationItemSerializer::onSerializedEntityRequest
        ));
    }

    public void onSerializedEntityRequest(Event<Integer> event) {
        if (!isActiveSupplier.getAsBoolean() || !isVisibleToSupplier.apply(entityId())) {
            return;
        }

        if (lastSerialization == null || System.currentTimeMillis() - lastSerializationTime > 20) {
            var pos = position.get();
            var builder = EntityProto.LocationItemEntity.newBuilder()
                    .setIsLocation(parentEntity.entityType() == EntityType.LOCATION)
                    .setId(entityId())
                    .setName(parentEntity.name())
                    .setPosX(pos.x())
                    .setPosY(pos.x())
                    .setKey(key);
            for (int i : states.get()) { builder.addStates(i); }

            lastSerialization = builder.build();
            lastSerializationTime = System.currentTimeMillis();
        }
        emitSerialization(event, lastSerialization);
    }

    public void emitSerialization(Event<Integer> respEvent, EntityProto.LocationItemEntity data) {
        Event.responseEvent(this, respEvent, EventType.SERIALIZED_LOC_ITEM_RESP, data);
    }

}
