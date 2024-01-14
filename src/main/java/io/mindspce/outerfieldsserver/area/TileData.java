package io.mindspce.outerfieldsserver.area;

import io.mindspice.mindlib.data.geometry.IVector2;


public record TileData(
        IVector2 index,
        boolean isNavigable,
        boolean isCollision,
        boolean isLocation
) {

    public boolean isNavigable() {
        return isNavigable;
    }

    public boolean hasCollision() {
        return isCollision;
    }

    public boolean isLocation() {
        return isCollision;
    }

    public TileData withCollisionChange(boolean collision) {
        return new TileData(index, isNavigable, collision, isLocation);
    }

    public TileData withNavChange(boolean navigable) {
        return new TileData(index, navigable, isCollision, isLocation);
    }

    public TileData withLocationChange(boolean location) {
        return new TileData(index, isNavigable, isCollision, location);
    }


}
