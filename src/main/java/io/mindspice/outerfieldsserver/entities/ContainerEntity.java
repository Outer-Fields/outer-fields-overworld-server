package io.mindspice.outerfieldsserver.entities;

import io.mindspice.mindlib.data.collections.lists.primative.IntList;
import io.mindspice.mindlib.data.geometry.IVector2;
import io.mindspice.outerfieldsserver.components.items.ContainedItems;
import io.mindspice.outerfieldsserver.enums.AreaId;
import io.mindspice.outerfieldsserver.enums.ContainerType;
import io.mindspice.outerfieldsserver.enums.EntityType;
import io.mindspice.outerfieldsserver.enums.TokenType;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;


public class ContainerEntity extends PositionalEntity {
    public final ContainerType containerType;
    private final ContainedItems containedItems;

    public ContainerEntity(int id, ContainerType containerType, AreaId areaId, IVector2 position, Map<TokenType, Integer> tokenMap,
            Map<Long, ItemEntity<?>> itemMap) {
        super(id, EntityType.CONTAINER, areaId, position);
        this.containerType = containerType;
        containedItems = new ContainedItems(this, tokenMap, itemMap);
        addComponent(containedItems);
    }



    public ContainerEntity withRespawnLogic(Consumer<ContainedItems> respawnLogic, boolean runOnAdd) {
        containedItems.withRespawnLogic(respawnLogic, runOnAdd);
        return this;
    }

    public ContainerType containerType() {
        return containerType;
    }
}
