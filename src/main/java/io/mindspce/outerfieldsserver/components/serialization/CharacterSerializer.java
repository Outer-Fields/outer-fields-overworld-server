package io.mindspce.outerfieldsserver.components.serialization;

import io.mindspce.outerfieldsserver.components.Component;
import io.mindspce.outerfieldsserver.core.networking.proto.EntityProto;
import io.mindspce.outerfieldsserver.entities.PositionalEntity;
import io.mindspce.outerfieldsserver.enums.ComponentType;
import io.mindspce.outerfieldsserver.enums.EntityType;
import io.mindspce.outerfieldsserver.systems.event.Event;
import io.mindspce.outerfieldsserver.systems.event.EventType;
import io.mindspice.mindlib.data.geometry.IVector2;
import io.mindspice.mindlib.functional.consumers.BiPredicatedBiConsumer;

import java.util.List;
import java.util.function.Supplier;


public class CharacterSerializer extends Component<CharacterSerializer> {
    private final Supplier<IVector2> position;
    private final Supplier<int[]> states;
    private final Supplier<int[]> outfit;
    private EntityProto.CharacterEntity lastSerialization;
    private long lastSerializationTime;

    public  CharacterSerializer(PositionalEntity parentEntity, Supplier<IVector2> position,
            Supplier<int[]> states, Supplier<int[]> outfit) {
        super(parentEntity, ComponentType.CHARACTER_SERIALIZER,
                List.of(EventType.SERIALIZED_ENTITY_REQUEST, EventType.SERIALIZED_ENTITIES_REQUEST)
        );
        this.position = position;
        this.states = states;
        this.outfit = outfit;

        registerListener(EventType.SERIALIZED_ENTITY_REQUEST, BiPredicatedBiConsumer.of(
                (CharacterSerializer cs, Event<Integer> ev) -> ev.eventArea() == areaId() && ev.recipientEntityId() == entityId(),
                CharacterSerializer::onSerializedEntityRequest
        ));
    }

    public void onSerializedEntityRequest(Event<Integer> event) {
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
