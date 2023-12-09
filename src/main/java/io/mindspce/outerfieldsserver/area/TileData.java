package io.mindspce.outerfieldsserver.area;

import io.mindspice.mindlib.data.geometry.IRect2;
import io.mindspice.mindlib.data.geometry.IVector2;


public record TileData(
        IVector2 index,
        NavData navData,
        boolean hasCollision
) {

    public boolean isNavigable() {
        return navData != null;
    }

    public boolean hasCollision() {
        return hasCollision;
    }


}
