package io.mindspce.outerfieldsserver.components;

import io.mindspce.outerfieldsserver.components.logic.PredicateLib;
import io.mindspce.outerfieldsserver.core.GameSettings;
import io.mindspce.outerfieldsserver.core.Tick;
import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.enums.ComponentType;
import io.mindspce.outerfieldsserver.enums.EntityType;
import io.mindspce.outerfieldsserver.networking.DataType;
import io.mindspce.outerfieldsserver.networking.NetSerializable;
import io.mindspce.outerfieldsserver.systems.EventData;
import io.mindspce.outerfieldsserver.systems.event.Event;
import io.mindspce.outerfieldsserver.systems.event.EventType;
import io.mindspice.mindlib.data.collections.lists.primative.IntList;
import io.mindspice.mindlib.data.tuples.Pair;
import io.mindspice.mindlib.functional.consumers.BiPredicatedBiConsumer;
import org.springframework.web.socket.BinaryMessage;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class PlayerNetOut extends Component<PlayerNetOut> {
    public int fullUpdateTick = 0;
    private final PlayerSession playerSession;
    private final ViewRect viewrect;
    private final KnownEntities knownEntities;
    private List<byte[]> unknownEntityData = new ArrayList<>(EntityType.values().length);
    IntList positionData = new IntList(50 * 3);

    public PlayerNetOut(Entity parentEntity, PlayerSession playerSession, ViewRect viewRect, KnownEntities knownEntities) {
        super(parentEntity, ComponentType.PLAYER_NET_OUT, List.of());

        this.playerSession = playerSession;
        this.viewrect = viewRect;
        this.knownEntities = knownEntities;
        setOnTickConsumer(PlayerNetOut::onTickMethod);

        registerListener(EventType.ENTITY_GRID_RESPONSE, PlayerNetOut::onEntityGridResponse);
//        registerListener(EventType.ENTITY_POSITION_CHANGED, BiPredicatedBiConsumer.of(
//                PredicateLib::isSameAreaEvent, PlayerNetOut::onEntityPositionChanged)
//        );
        registerListener(EventType.ENTITY_POSITION_CHANGED, BiPredicatedBiConsumer.of(
                (PlayerNetOut self, Event<EventData.EntityPositionChanged> event) ->
                        PredicateLib.isSameAreaEvent(self, event) && viewRect.viewRect.contains(event.data().newPosition()),
                PlayerNetOut::onEntityPositionChanged
        ));
        registerListener(EventType.SERIALIZED_ENTITIES_RESP, PlayerNetOut::onSerializedEntitiesResp);
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

    public void onSerializedEntitiesResp(Event<Pair<IntList, byte[]>> event) {
        unknownEntityData.add(event.data().second());
        event.data().first().forEach(id -> knownEntities.knownEntities.set(id, false));
    }

    public void onEntityGridResponse(Event<int[]> event) {
        IntList unknownEntities = new IntList(Arrays.stream(event.data()).filter(id -> !knownEntities.knownEntities.get(id)).toArray());
        emitEvent(Event.serializedEntitiesReq(this, this.areaId(), (Entity entity) -> unknownEntities.contains(entity.entityId())));
    }

    public void onTickMethod(Tick tick) {
        if (fullUpdateTick == 0) {
            fullUpdateTick = GameSettings.GET().tickRate() / 2;
            emitEvent(Event.entityGridQuery(this, areaId(), parentEntity.areaEntity().entityId(), viewrect.viewRect));
        }
        if (unknownEntityData != null) {
            byte[] positionBytes = getPositionBytes();
            int unknownLength = unknownEntityData.stream().mapToInt(b -> b.length).sum() + positionBytes.length + 1 + 4;
            ByteBuffer combinedBuffer = NetSerializable.getEmptyBuffer(unknownLength);
            combinedBuffer.put(DataType.NEW_ENTITY.value);
            combinedBuffer.putInt(unknownLength);
            unknownEntityData.forEach(combinedBuffer::put);
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


}
