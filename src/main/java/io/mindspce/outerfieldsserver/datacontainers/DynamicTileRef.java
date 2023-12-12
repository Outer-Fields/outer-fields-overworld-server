package io.mindspce.outerfieldsserver.datacontainers;

import io.mindspce.outerfieldsserver.area.AreaInstance;
import io.mindspce.outerfieldsserver.area.ChunkData;
import io.mindspce.outerfieldsserver.area.TileData;
import io.mindspice.mindlib.data.geometry.IVector2;


public class DynamicTileRef {
    private AreaInstance areaRef;
    private final IVector2 offset;
    private ChunkData chunkRef;
    private TileData tileRef;

    public DynamicTileRef(AreaInstance area, IVector2 offset) {
        this.areaRef = area;
        this.offset = offset;
    }

    public void updatePos(IVector2 globalPos) {
        updatePos(globalPos.x(), globalPos.y());
    }

    public void updateAreaRef(AreaInstance areaRef) {
        this.areaRef = areaRef;
    }

    public void updatePos(int globalX, int globalY) {
        chunkRef = areaRef.getChunkByGlobalPos(globalX + offset.x(), globalY + offset.y());
        if (chunkRef == null) {
            tileRef = null;
            return;
        }
        tileRef = chunkRef.getTileByGlobalPos(globalX + offset.x(), globalY + offset.y());
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
