package io.mindspce.outerfieldsserver.datacontainers;

import io.mindspice.mindlib.data.geometry.IVector2;


public record ChunkTileIndex(
        IVector2 chunkIndex,
        IVector2 tileIndex
) {

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        ChunkTileIndex that = (ChunkTileIndex) o;
        if (!chunkIndex.equals(that.chunkIndex)) { return false; }
        return tileIndex.equals(that.tileIndex);
    }

    @Override
    public int hashCode() {
        int result = chunkIndex != null ? chunkIndex.hashCode() : 0;
        result = 31 * result + (tileIndex != null ? tileIndex.hashCode() : 0);
        return result;
    }
}
