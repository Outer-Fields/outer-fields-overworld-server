package io.mindspce.outerfieldsserver.area;

import io.mindspce.outerfieldsserver.entities.LootEntity;
import io.mindspice.mindlib.data.geometry.IVector2;


public record TileData(
        IVector2 index,
        boolean isNavigable,
        boolean isCollision,
        long locationKey,
        long itemKey,
        long lootKey
) {

    public boolean isNavigable() {
        return isNavigable;
    }

    public boolean hasCollision() {
        return isCollision;
    }

    public boolean isLocation() {
        return locationKey > 0;
    }

    public boolean hasItem() {
        return itemKey > 0;
    }

    public boolean hasLoot() {
        return  lootKey > 0;
    }

    public TileData withCollisionChange(boolean collision) {
        return new TileData(index, isNavigable, collision, locationKey, itemKey, lootKey);
    }

    public TileData withNavChange(boolean navigable) {
        return new TileData(index, navigable, isCollision, locationKey, itemKey, lootKey);
    }

    public TileData withLocationChange(long locationKey) {
        return new TileData(index, isNavigable, isCollision, locationKey, itemKey, lootKey);
    }

    public TileData withItemChange(long itemKey) {
        return new TileData(index, isNavigable, isCollision, locationKey, itemKey, lootKey);
    }

    public TileData withLootChange(long lootKey) {
        return new TileData(index, isNavigable, isCollision, locationKey, itemKey, lootKey);

    }

}
