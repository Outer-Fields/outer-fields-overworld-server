package io.mindspce.outerfieldsserver.entities;

import io.mindspce.outerfieldsserver.factory.ComponentFactory;
import io.mindspce.outerfieldsserver.enums.AreaId;
import io.mindspce.outerfieldsserver.enums.ClothingItem;
import io.mindspce.outerfieldsserver.enums.EntityState;
import io.mindspce.outerfieldsserver.enums.EntityType;
import io.mindspice.mindlib.data.geometry.IVector2;

import java.util.List;


public class NonPlayerEntity extends PositionalEntity {
    private final long key;

    public NonPlayerEntity(int entityId, long key, String characterName, List<EntityState> initStates,
            ClothingItem[] initOutfit, AreaId currArea, IVector2 currPosition, IVector2 viewRectSize) {
        super(entityId, EntityType.NON_PLAYER, currArea, currPosition);
        super.name = characterName;
        this.key = key;

        ComponentFactory.System.attachBaseNPCComponents(
                this, currPosition, initStates, initOutfit, viewRectSize);
    }

    public long getKey() { return key; }

}
