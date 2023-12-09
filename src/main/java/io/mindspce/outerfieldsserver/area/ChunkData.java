package io.mindspce.outerfieldsserver.area;

import io.mindspce.outerfieldsserver.core.NavCalc.GameSettings;
import io.mindspce.outerfieldsserver.objects.item.ItemState;
import io.mindspce.outerfieldsserver.objects.nonplayer.EnemyState;
import io.mindspce.outerfieldsserver.objects.nonplayer.NpcState;
import io.mindspce.outerfieldsserver.objects.player.PlayerLocation;
import io.mindspice.mindlib.data.geometry.IVector2;

import java.util.*;


public class ChunkData {
    private final IVector2 index;
    private final TileData[][] tileMap;
    private final Set<PlayerLocation> activePlayers = Collections.synchronizedSet(new HashSet<>());
    private final Set<ItemState> activeItemData = Collections.synchronizedSet(new HashSet<>());
    private final Set<NpcState> activeNpcData = Collections.synchronizedSet(new HashSet<>());
    private final Set<EnemyState> activeEnemies = Collections.synchronizedSet(new HashSet<>());

    public ChunkData(IVector2 index, TileData[][] tileMap) {
        this.index = index;
        this.tileMap = tileMap;
    }

    public TileData getTileByPos(IVector2 pos) {
        int x = pos.x() / GameSettings.GET().chunkSize().x();
        int y = pos.y() / GameSettings.GET().chunkSize().y();
        if (x > tileMap.length || y > tileMap[0].length) {
            throw new IndexOutOfBoundsException("Position out of chunk bounds");
        }
        return tileMap[x][y];
    }

    public TileData getTileByIndex(IVector2 index) {
        if (index.x() > tileMap.length || index.y() > tileMap[0].length) {
            throw new IndexOutOfBoundsException("Position out of chunk bounds");
        }
        return tileMap[index.x()][index.y()];
    }

    public TileData[][] getTileMap() {
        return tileMap;
    }

    public IVector2[][] getVectorMap() {
        IVector2[][] vecMap = new IVector2[tileMap.length][tileMap[0].length];
        for (int i = 0; i < tileMap.length; ++i) {
            for (int j = 0; j < tileMap[0].length; ++j) {
                vecMap[i][j] = tileMap[i][j].index();
            }
        }
        return vecMap;
    }

    public IVector2 getIndex() {
        return index;
    }
}
