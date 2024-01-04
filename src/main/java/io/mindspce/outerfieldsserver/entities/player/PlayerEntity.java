package io.mindspce.outerfieldsserver.entities.player;

import io.mindspce.outerfieldsserver.components.*;
import io.mindspce.outerfieldsserver.entities.PositionalEntity;
import io.mindspce.outerfieldsserver.enums.AreaId;
import io.mindspce.outerfieldsserver.enums.ClothingItem;
import io.mindspce.outerfieldsserver.enums.EntityState;
import io.mindspce.outerfieldsserver.enums.EntityType;
import io.mindspice.mindlib.data.geometry.IVector2;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.function.Consumer;


public class PlayerEntity extends PositionalEntity {
    private final int playerId;

    public PlayerEntity(int entityId, int playerId, String playerName, List<EntityState> initStates,
            ClothingItem[] initOutfit, AreaId currArea,
            IVector2 currPosition, WebSocketSession webSocketSession) {
        super(entityId, EntityType.PLAYER, currArea, currPosition);
        super.name = playerName;
        this.playerId = playerId;

        ComponentFactory.System.initPlayerEntityComponents(
                this, currPosition, initStates, initOutfit, webSocketSession
        );
    }

    public int getPlayerId() { return playerId; }

    public String playerName() { return name; }


}
