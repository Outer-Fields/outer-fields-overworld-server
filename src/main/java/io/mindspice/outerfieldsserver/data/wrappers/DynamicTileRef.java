package io.mindspice.outerfieldsserver.data.wrappers;

import io.mindspice.outerfieldsserver.entities.AreaEntity;
import io.mindspice.outerfieldsserver.entities.ChunkEntity;
import io.mindspice.outerfieldsserver.area.TileData;
import io.mindspice.mindlib.data.geometry.IVector2;


public class DynamicTileRef {
    private volatile AreaEntity areaRef;
    private final IVector2 offset;
    private volatile ChunkEntity chunkRef;
    private volatile TileData tileRef;

    public DynamicTileRef(AreaEntity area, IVector2 offset) {
        this.areaRef = area;
        this.offset = offset;
    }

    public void updateAreaRef(AreaEntity areaRef) {
        if (areaRef == null) {
            throw new IllegalStateException("null area reference passed");
        }
        this.areaRef = areaRef;
    }

    public void updatePos(IVector2 pos) {
        chunkRef = areaRef.getChunkByGlobalPos(pos);
        if (chunkRef == null) {
            tileRef = null;
            return;
        }

        tileRef = chunkRef.getTileByGlobalPos(pos);
    }

    public TileData getTileRef() {
        return tileRef;
    }

    public ChunkEntity getChunkRef() {
        return chunkRef;
    }

    public AreaEntity getAreaRef() {
        return areaRef;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DynamicTileRef: ");
        sb.append("\n  areaRef: ").append(areaRef);
        sb.append(",\n  offset: ").append(offset);
        sb.append(",\n  chunkRef: ").append(chunkRef);
        sb.append(",\n  tileRef: ").append(tileRef);
        sb.append("\n");
        return sb.toString();
    }
}
