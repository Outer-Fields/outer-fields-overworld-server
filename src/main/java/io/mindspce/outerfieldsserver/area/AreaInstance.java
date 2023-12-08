package io.mindspce.outerfieldsserver.area;

import io.mindspce.outerfieldsserver.core.configuration.GameSettings;
import io.mindspice.mindlib.data.geometry.IVector2;


public class AreaInstance {
    private final String arenaName;
    private final ChunkData[][] chunkMap;
    private final IVector2 areaSize;

    public AreaInstance(String arenaName, ChunkData[][] chunkMap) {
        this.arenaName = arenaName;
        this.chunkMap = chunkMap;
        areaSize = IVector2.of(chunkMap.length, chunkMap[0].length);
    }

    public String getArenaName() {
        return arenaName;
    }

    public IVector2 getAreaSize() {
        return areaSize;
    }

    public ChunkData[][] getChunkMap() {
        return chunkMap;
    }

    public ChunkData getChunkByPos(IVector2 pos) {
        int x = pos.x() / GameSettings.GET().chunkSize().x();
        int y = pos.y() / GameSettings.GET().chunkSize().y();
        if (x > chunkMap.length || y > chunkMap[0].length) {
            throw new IndexOutOfBoundsException("Position out of chunk bounds");
        }
        return chunkMap[x][y];
    }

    public ChunkData getChunkByIndex(IVector2 index) {
        if (index.x() > chunkMap.length || index.y() > chunkMap[0].length) {
            throw new IndexOutOfBoundsException("Position out of chunk bounds");
        }
        return chunkMap[index.x()][index.y()];
    }

    public IVector2[][] getVectorMap() {
        IVector2[][] vecMap = new IVector2[chunkMap.length][chunkMap[0].length];
        for (int i = 0; i < chunkMap.length; ++i) {
            for (int j = 0; j < chunkMap[0].length; ++j) {
                vecMap[i][j] = chunkMap[i][j].getIndex();
            }
        }
        return vecMap;
    }


}

