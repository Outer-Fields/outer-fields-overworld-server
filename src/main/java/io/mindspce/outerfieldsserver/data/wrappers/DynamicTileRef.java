package io.mindspce.outerfieldsserver.data.wrappers;

import io.mindspce.outerfieldsserver.area.AreaInstance;
import io.mindspce.outerfieldsserver.area.ChunkData;
import io.mindspce.outerfieldsserver.area.TileData;
import io.mindspce.outerfieldsserver.core.GameSettings;
import io.mindspice.mindlib.data.geometry.IVector2;


public class DynamicTileRef {
    private volatile AreaInstance areaRef;
    private final IVector2 offset;
    private volatile ChunkData chunkRef;
    private volatile TileData tileRef;

    public DynamicTileRef(AreaInstance area, IVector2 offset) {
        this.areaRef = area;
        this.offset = offset;
    }

    public void updateAreaRef(AreaInstance areaRef) {
        if (areaRef == null) {
            throw new IllegalStateException("null area reference passed");
        }
        this.areaRef = areaRef;
    }

    public void updatePos(IVector2 pos) {
        chunkRef = areaRef.getChunkByGlobalPos(
                pos.x() + (offset.x() * GameSettings.GET().tileSize()),
                pos.y() + (offset.y() * GameSettings.GET().tileSize())
        );
        if (chunkRef == null) {
            tileRef = null;
            return;
        }
        tileRef = chunkRef.getTileByGlobalPos(pos);
    }

    public TileData getTileRef() {
        return tileRef;
    }

    public ChunkData getChunkRef() {
        return chunkRef;
    }

    public AreaInstance getAreaRef() {
        return areaRef;
    }

}
