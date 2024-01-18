package io.mindspce.outerfieldsserver.entities;

import io.mindspce.outerfieldsserver.enums.AreaId;
import io.mindspce.outerfieldsserver.enums.EntityType;
import io.mindspice.mindlib.data.geometry.IVector2;


public class ItemEntity extends PositionalEntity {
    private final String itemName;
    private final long key;
    private volatile int ownerPlayerId = -1;

    public ItemEntity(int id, AreaId areaId, IVector2 position, String itemName, long key) {
        super(id, EntityType.ITEM, areaId, position);
        this.itemName = itemName;
        this.key = key;
    }

    public int ownerPlayerId() { return ownerPlayerId; }

    public void setOwner(int ownerPlayerId) { this.ownerPlayerId = ownerPlayerId; }

    public boolean isOwned() { return ownerPlayerId > 0; }

    public String itemName() { return itemName; }

    public long key() { return key; }
}
