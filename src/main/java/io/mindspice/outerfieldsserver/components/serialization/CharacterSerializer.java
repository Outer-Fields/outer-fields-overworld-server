package io.mindspice.outerfieldsserver.components.serialization;

import io.mindspice.outerfieldsserver.components.Component;
import io.mindspice.outerfieldsserver.core.networking.proto.EntityProto;
import io.mindspice.outerfieldsserver.entities.PositionalEntity;
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


public class CharacterSerializer extends Component<CharacterSerializer> {
    private final Supplier<IVector2> position;
    private final Supplier<int[]> states;
    private final Supplier<int[]> outfit;
    private final BooleanSupplier isActiveSupplier;
    private final Function<Integer, Boolean> isVisibleToSupplier;
    private EntityProto.CharacterEntity lastSerialization;
    private long lastSerializationTime;

    public CharacterSerializer(PositionalEntity parentEntity, Supplier<IVector2> position,
            Supplier<int[]> states, Supplier<int[]> outfit, BooleanSupplier isActiveSupplier,
            Function<Integer, Boolean> isVisibleToSupplier) {
        super(parentEntity, ComponentType.CHARACTER_SERIALIZER,
                List.of(EventType.SERIALIZED_ENTITY_REQUEST, EventType.SERIALIZED_ENTITIES_REQUEST)
        );
        this.position = position;
        this.states = states;
        this.outfit = outfit;
        this.isActiveSupplier = isActiveSupplier;
        this.isVisibleToSupplier = isVisibleToSupplier;

        registerListener(EventType.SERIALIZED_ENTITY_REQUEST, BiPredicatedBiConsumer.of(
                (CharacterSerializer cs, Event<Integer> ev) -> ev.eventArea() == areaId() && ev.recipientEntityId() == entityId(),
                CharacterSerializer::onSerializedEntityRequest
        ));
    }

    public void onSerializedEntityRequest(Event<Integer> event) {
        if (!isActiveSupplier.getAsBoolean() || !isVisibleToSupplier.apply(entityId())) {
            return;
        }

        if (lastSerialization == null || System.currentTimeMillis() - lastSerializationTime > 20) {
            var pos = position.get();
            var builder = EntityProto.CharacterEntity.newBuilder()
                    .setIsPlayer(parentEntity.entityType() == EntityType.PLAYER)
                    .setId(entityId())
                    .setName(parentEntity.name())
                    .setPosX(pos.x())
                    .setPosY(pos.x());
            for (int i : states.get()) { builder.addStates(i); }
            for (int i : outfit.get()) { builder.addOutfit(i); }
            lastSerialization = builder.build();
            lastSerializationTime = System.currentTimeMillis();
        }
        emitSerialization(event, lastSerialization);
    }

    public void emitSerialization(Event<Integer> respEvent, EntityProto.CharacterEntity data) {
        emitEvent(Event.responseEvent(this, respEvent, EventType.SERIALIZED_CHARACTER_RESP, data));

    }

}
