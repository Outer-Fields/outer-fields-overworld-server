package io.mindspce.outerfieldsserver.entities;

import io.mindspce.outerfieldsserver.enums.*;
import io.mindspce.outerfieldsserver.factory.ComponentFactory;
import io.mindspice.mindlib.data.geometry.IVector2;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class PlayerEntity extends CharacterEntity {
    private final int playerId;
    private List<FactionType> factions = new CopyOnWriteArrayList<>();

    public PlayerEntity(int entityId, int playerId, String playerName, List<EntityState> initStates,
            ClothingItem[] initOutfit, AreaId currArea,
            IVector2 currPosition, WebSocketSession webSocketSession) {
        super(entityId, EntityType.PLAYER, currArea, currPosition);
        super.name = playerName;
        this.playerId = playerId;
        factions.add(FactionType.PLAYER);

        ComponentFactory.CompSystem.attachPlayerEntityComponents(
                this, currPosition, currArea, initStates, initOutfit, webSocketSession
        );
    }

    public int playerId() { return playerId; }

    public String playerName() { return name; }


}
