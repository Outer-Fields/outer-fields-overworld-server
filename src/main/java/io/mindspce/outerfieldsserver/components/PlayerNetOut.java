package io.mindspce.outerfieldsserver.components;

import io.mindspce.outerfieldsserver.components.logic.PredicateLib;
import io.mindspce.outerfieldsserver.core.GameSettings;
import io.mindspce.outerfieldsserver.core.Tick;
import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.enums.ComponentType;
import io.mindspce.outerfieldsserver.networking.DataType;
import io.mindspce.outerfieldsserver.networking.NetSerializable;
import io.mindspce.outerfieldsserver.systems.EventData;
import io.mindspce.outerfieldsserver.systems.event.Event;
import io.mindspce.outerfieldsserver.systems.event.EventType;
import io.mindspice.mindlib.data.collections.lists.primative.IntList;
import io.mindspice.mindlib.functional.consumers.BiPredicatedBiConsumer;
import org.springframework.web.socket.BinaryMessage;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;


public class PlayerNetOut extends Component<PlayerNetOut> {
    public int fullUpdateTick = 0;
    private final PlayerSession playerSession;
    private final ViewRect viewrect;
    private final KnownEntities knownEntities;
    private byte[] unknownEntityData;
    private ByteBuffer posUpdatesBuffer;
    IntList positionData = new IntList(50 * 3);

    public PlayerNetOut(Entity parentEntity, PlayerSession playerSession, ViewRect viewRect, KnownEntities knownEntities) {
        super(parentEntity, ComponentType.PLAYER_NET_OUT, List.of());

        this.playerSession = playerSession;
        this.viewrect = viewRect;
        this.knownEntities = knownEntities;
        setOnTickConsumer(PlayerNetOut::onTickMethod);

        registerListener(EventType.ENTITY_GRID_RESPONSE, PlayerNetOut::onEntityGridResponse);
        registerListener(EventType.ENTITY_POSITION_CHANGED, BiPredicatedBiConsumer.of(
                PredicateLib::isSameAreaEvent, PlayerNetOut::onEntityPositionChanged)
        );
        registerListener(EventType.ENTITY_POSITION_CHANGED, BiPredicatedBiConsumer.of(
                (PlayerNetOut self, Event<EventData.EntityPositionChanged> event) ->
                        PredicateLib.isSameAreaEvent(self, event) && viewRect.viewRect.contains(event.data().newPosition()),
                PlayerNetOut::onEntityPositionChanged
        ));
    }

    public void onEntityPositionChanged(Event<EventData.EntityPositionChanged> event) {
        if (viewrect.viewRect.contains(event.data().newPosition())) {
            int id = event.issuerEntityId();
            int x = event.data().newPosition().x();
            int y = event.data().newPosition().y();

            if (overWriteIfExisting(id, x, y)) { return; }

            positionData.add(event.issuerEntityId());
            positionData.add(event.data().newPosition().x());
            positionData.add(event.data().newPosition().y());
        }
    }

    public boolean overWriteIfExisting(int id, int x, int y) {
        for (int i = 0; i < positionData.size(); i += 3) {
            if (positionData.get(i) == id) {
                positionData.set(i + 1, x);
                positionData.set(i + 2, y);
                return true;
            }
        }
        return false;
    }

    public void onEntityGridResponse(Event<int[]> event) {
        int[] unknownEntities = Arrays.stream(event.data()).filter(id -> !knownEntities.knownEntities.get(id)).toArray();
        ByteBuffer buffer = NetSerializable.getEmptyBuffer(unknownEntities.length * 8);
        var compEvent = Event.completableEvent(this,
                Event.globalComponentCallback(this, areaId(), ComponentType.NET_SERIALIZER,
                        (NetSerializer ns) -> ns.serializeToBuffer(buffer)),
                Event.directComponentCallback(this, areaId(), componentType, entityId(), componentId(),
                        (PlayerNetOut no) -> {
                            no.onNetSerializationResponse(buffer);
                            knownEntities.addKnownEntities(unknownEntities);
                        })
        );
        emitEvent(compEvent);
    }

    public void onNetSerializationResponse(ByteBuffer buffer) {
        unknownEntityData = NetSerializable.trimBufferToBytes(buffer);
    }

    public void onTickMethod(Tick tick) {
        if (fullUpdateTick == 0) {
            fullUpdateTick = GameSettings.GET().tickRate() / 2;
            emitEvent(Event.entityGridQuery(this, areaId(), parentEntity.areaEntity().entityId(), viewrect.viewRect));
        }
        if (unknownEntityData != null) {
            byte[] positionBytes = getPositionBytes();
            ByteBuffer combinedBuffer = NetSerializable.getEmptyBuffer(unknownEntityData.length + positionBytes.length + 1 + 4);
            combinedBuffer.put(DataType.NEW_ENTITY.value);
            combinedBuffer.putInt(unknownEntityData.length);
            combinedBuffer.put(unknownEntityData);
            combinedBuffer.put(positionBytes);
            playerSession.send(new BinaryMessage(combinedBuffer.array()));
            unknownEntityData = null;
        } else {
            playerSession.send(new BinaryMessage(getPositionBytes()));
        }
    }

    public byte[] getPositionBytes() {
        // TODO later implement the int trim to save on packet size
        ByteBuffer buffer = NetSerializable.getEmptyBuffer((positionData.size() * 4) + 1 + 4);
        buffer.put(DataType.ENTITY_POSITION.value);
        buffer.putInt(positionData.size() * 4);
        positionData.forEach(buffer::putInt);
        positionData.clear();
        return buffer.array();
    }

    public byte[] combineByteArrays(byte[] arr1, byte[] arr2) {
        byte[] combined = new byte[arr1.length + arr2.length];
        System.arraycopy(arr1, 0, combined, 0, arr1.length);
        System.arraycopy(arr2, 0, combined, arr1.length, arr2.length);
        return combined;
    }

}
