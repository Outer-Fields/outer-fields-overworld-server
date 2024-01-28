package io.mindspice.outerfieldsserver.components.player;

import io.mindspice.outerfieldsserver.components.Component;
import io.mindspice.outerfieldsserver.components.logic.PredicateLib;
import io.mindspice.outerfieldsserver.core.WorldSettings;
import io.mindspice.outerfieldsserver.core.Tick;
import io.mindspice.outerfieldsserver.core.networking.proto.EntityProto;
import io.mindspice.outerfieldsserver.entities.Entity;
import io.mindspice.outerfieldsserver.enums.ComponentType;
import io.mindspice.outerfieldsserver.systems.event.EventData;
import io.mindspice.outerfieldsserver.systems.event.Event;
import io.mindspice.outerfieldsserver.systems.event.EventType;
import io.mindspice.mindlib.data.collections.lists.primative.IntList;
import io.mindspice.mindlib.data.geometry.IVector2;
import io.mindspice.mindlib.functional.consumers.BiPredicatedBiConsumer;

import java.util.Arrays;
import java.util.List;


public class PlayerNetOut extends Component<PlayerNetOut> {
    public int fullUpdateTick = 0;
    private final PlayerSession playerSession;
    private final ViewRect viewrect;
    private final KnownEntities knownEntities;
    private final IntList positionData = new IntList(50 * 3);
    private EntityProto.GamePacket.Builder gamePacket = EntityProto.GamePacket.newBuilder();
    private boolean haveData = false;

    public PlayerNetOut(Entity parentEntity, PlayerSession playerSession, ViewRect viewRect, KnownEntities knownEntities) {
        super(parentEntity, ComponentType.PLAYER_NET_OUT, List.of());

        this.playerSession = playerSession;
        this.viewrect = viewRect;
        this.knownEntities = knownEntities;
        setOnTickConsumer(PlayerNetOut::onTickMethod);

        registerListener(EventType.ENTITY_POSITION_CHANGED, BiPredicatedBiConsumer.of(
                (PlayerNetOut self, Event<EventData.EntityPositionChanged> event) ->
                        PredicateLib.isSameAreaEvent(self, event) && viewRect.viewRect.contains(event.data().newPosition()),
                PlayerNetOut::onEntityPositionChanged
        ));
        registerListener(EventType.SERIALIZED_CHARACTER_RESP, PlayerNetOut::onSerializedCharacterResp);
        registerListener(EventType.SERIALIZED_LOC_ITEM_RESP, PlayerNetOut::onSerializedLocItemResp);
        registerListener(EventType.ENTITY_GRID_RESPONSE, PlayerNetOut::onEntityGridResponse);
    }

    public void onEntityPositionChanged(Event<EventData.EntityPositionChanged> event) {
        haveData = true;
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

    public void authCorrection(IVector2 pos) {
        System.out.println("auth correction");
        haveData = true;
        var correction = EntityProto.AuthCorrection.newBuilder()
                .setPosX(pos.x())
                .setPosY(pos.y())
                .build();
        gamePacket.setAuthCorrection(correction);
    }

    public void onSerializedCharacterResp(Event<EntityProto.CharacterEntity> event) {
        haveData = true;
        EntityProto.GameEntity entity = EntityProto.GameEntity.newBuilder()
                .setCharacter(event.data())
                .build();
        gamePacket.addEntities(entity);
        knownEntities.knownEntities.set(event.issuerEntityId());
    }

    public void onSerializedLocItemResp(Event<EntityProto.LocationItemEntity> event) {
        haveData = true;
        EntityProto.GameEntity entity = EntityProto.GameEntity.newBuilder()
                .setLocationItem(event.data())
                .build();
        gamePacket.addEntities(entity);
        knownEntities.knownEntities.set(event.issuerEntityId());
    }

    public void onEntityGridResponse(Event<int[]> event) {
        IntList unknownEntities = new IntList(
                Arrays.stream(event.data()).filter(id -> !knownEntities.knownEntities.get(id) && id != entityId()).toArray()
        );
        unknownEntities.forEach(e -> emitEvent(Event.serializedEntityRequest(this, areaId(), e)));
    }

    public PlayerSession playerSession() {
        return playerSession;
    }

    public void onTickMethod(Tick tick) {
        if (--fullUpdateTick < 0) {
            fullUpdateTick = WorldSettings.GET().tickRate();
            emitEvent(Event.entityGridQuery(this, areaId(), areaId().entityId, viewrect.viewRect));
        }
        if (!haveData) { return; }
        for (int i = 0; i < positionData.size(); i += 3) {
            EntityProto.PositionUpdate posUpdate = EntityProto.PositionUpdate.newBuilder()
                    .setId(positionData.get(i))
                    .setPosX(positionData.get(i + 1))
                    .setPosY(positionData.get(i + 2))
                    .build();
            gamePacket.addPositionUpdates(posUpdate);
        }
        // System.out.println(gamePacket);
        playerSession.send(gamePacket.build().toByteArray());
        gamePacket = EntityProto.GamePacket.newBuilder();
        positionData.clear();
        haveData = false;

    }
}
