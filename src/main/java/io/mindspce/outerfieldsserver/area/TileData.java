package io.mindspce.outerfieldsserver.area;

import io.mindspice.mindlib.data.geometry.IVector2;


public record TileData(
        IVector2 index,
        NavData navData,
        int collisionId
) {

    public TileData(IVector2 index) {
        this(index, null, -1);
    }

    public TileData(IVector2 index, NavData navData) {
        this(index, navData, -1);
    }

    public TileData(IVector2 index, int collisionId) {
        this(index, null, collisionId);
    }

    public boolean isNavigable() {
        return navData != null;
    }

    public boolean hasCollision() {
        return collisionId != -1;
    }
}
