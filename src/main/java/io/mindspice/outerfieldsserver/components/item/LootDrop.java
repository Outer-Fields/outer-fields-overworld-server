package io.mindspice.outerfieldsserver.components.item;

import io.mindspice.outerfieldsserver.components.Component;
import io.mindspice.outerfieldsserver.entities.Entity;
import io.mindspice.outerfieldsserver.entities.LootEntity;
import io.mindspice.outerfieldsserver.enums.ComponentType;

import java.util.List;


public class LootDrop extends Component<LootDrop> {
    public LootEntity lootDrop;

    public LootDrop(Entity parentEntity, LootEntity lootEntity) {
        super(parentEntity, ComponentType.LOOT_DROP, List.of());
        this.lootDrop = lootEntity;
    }

    public LootEntity getLootEntity() {
        return lootDrop;
    }


}
