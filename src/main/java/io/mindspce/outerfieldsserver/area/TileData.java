package io.mindspce.outerfieldsserver.area;

import io.mindspice.mindlib.data.geometry.IVector2;


public record TileData(
        IVector2 index,
        NavData navData,
        boolean isCollision,
        boolean isLocation
) {

    public boolean isNavigable() {
        return navData != null;
    }

    public boolean hasCollision() {
        return isCollision;
    }

    public boolean isLocation() {
        return isCollision;
    }

    public TileData withCollisionChange(boolean collision) {
        return new TileData(index, navData, collision, isLocation);
    }

    public TileData withNavChange(NavData data) {
        return new TileData(index, data, isCollision, isLocation);
    }

    public TileData withLocationChange(boolean location) {
        return new TileData(index, navData, isCollision, location);
    }


}
