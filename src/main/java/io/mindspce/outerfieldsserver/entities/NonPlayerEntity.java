package io.mindspce.outerfieldsserver.entities;

import io.mindspce.outerfieldsserver.components.ComponentFactory;
import io.mindspce.outerfieldsserver.components.ai.ThoughtModule;
import io.mindspce.outerfieldsserver.enums.AreaId;
import io.mindspce.outerfieldsserver.enums.ClothingItem;
import io.mindspce.outerfieldsserver.enums.EntityState;
import io.mindspce.outerfieldsserver.enums.EntityType;
import io.mindspice.mindlib.data.geometry.IVector2;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;


public class NonPlayerEntity extends PositionalEntity {
    long key;

    public NonPlayerEntity(int entityId, long key, String characterName, List<EntityState> initStates,
            ClothingItem[] initOutfit, AreaId currArea, IVector2 currPosition, IVector2 viewRectSize) {
        super(entityId, EntityType.NON_PLAYER_ENTITY, currArea, currPosition);
        super.name = characterName;
        this.key = key;

        ComponentFactory.System.attachNPCComponents(
                this, currPosition, initStates, initOutfit, viewRectSize);
    }

    public long getKey() { return key; }

}
