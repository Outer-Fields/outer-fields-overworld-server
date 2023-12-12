package io.mindspce.outerfieldsserver.area;

import io.mindspce.outerfieldsserver.core.GameSettings;
import io.mindspce.outerfieldsserver.entities.item.ItemState;
import io.mindspce.outerfieldsserver.entities.locations.LocationState;
import io.mindspce.outerfieldsserver.entities.nonplayer.EnemyState;
import io.mindspce.outerfieldsserver.entities.nonplayer.NpcState;
import io.mindspce.outerfieldsserver.entities.player.PlayerCharacter;
import io.mindspice.mindlib.data.geometry.IPolygon2;
import io.mindspice.mindlib.data.geometry.IRect2;
import io.mindspice.mindlib.data.geometry.IVector2;

import java.util.*;


public class ChunkData {
    private final IVector2 index;
    private final IVector2 globalPos;
    private final IRect2 boundsRect;
    private final TileData[][] tileMap;
    private final Map<Integer, IPolygon2> collisions;
    private final Set<PlayerCharacter> activePlayers = Collections.synchronizedSet(new HashSet<>());
    private final Set<NpcState> activeNpcs = Collections.synchronizedSet(new HashSet<>());
    private final Set<EnemyState> activeEnemies = Collections.synchronizedSet(new HashSet<>());
    private final Set<ItemState> activeItems = Collections.synchronizedSet(new HashSet<>());
    private final Set<LocationState> locationStates = Collections.synchronizedSet(new HashSet<>());

    public ChunkData(IVector2 index, TileData[][] tileMap, Map<Integer, IPolygon2> collisions) {
        this.index = index;
        this.tileMap = tileMap;
        this.collisions = collisions;
        globalPos = IVector2.of(
                index.x() * GameSettings.GET().chunkSize().x(),
                index.y() * GameSettings.GET().chunkSize().y()
        );
        boundsRect = IRect2.of(globalPos, GameSettings.GET().chunkSize());

    }

    public Set<PlayerCharacter> getActivePlayers() {
        return activePlayers;
    }

    public Set<NpcState> getActiveNpcs() {
        return activeNpcs;
    }

    public Set<ItemState> getActiveItems() {
        return activeItems;
    }

    public Set<EnemyState> getActiveEnemies() {
        return activeEnemies;
    }

    public Set<LocationState> getLocationStates() {
        return locationStates;
    }


    public IVector2 getGlobalPos() {
        return globalPos;
    }

    public IRect2 getBoundsRect() {
        return boundsRect;
    }

    public IPolygon2 getCollision(int collisionId) {
        return collisions.get(collisionId);
    }

    public TileData getTileByLocalPos(IVector2 pos) {
        int x = pos.x() / GameSettings.GET().tileSize();
        int y = pos.y() / GameSettings.GET().tileSize();
        if (x > tileMap.length || y > tileMap[0].length) {
            throw new IndexOutOfBoundsException("Position out of chunk bounds");
        }
        return tileMap[x][y];
    }

    public TileData getTileByLocalPos(int posX, int posY) {
        int x = posX / GameSettings.GET().tileSize();
        int y = posY / GameSettings.GET().tileSize();
        if (x > tileMap.length || y > tileMap[0].length) {
            throw new IndexOutOfBoundsException("Position out of chunk bounds");
        }
        return tileMap[x][y];
    }

    public TileData getTileByGlobalPos(IVector2 pos) {
        int x = (pos.x() % GameSettings.GET().chunkSize().x()) / GameSettings.GET().tileSize();
        int y = (pos.y() % GameSettings.GET().chunkSize().y()) / GameSettings.GET().tileSize();
        if (x > tileMap.length || y > tileMap[0].length) {
            throw new IndexOutOfBoundsException("Position out of chunk bounds");
        }
        return tileMap[x][y];
    }

    public TileData getTileByGlobalPos(int posX, int posY) {
        int x = (posX % GameSettings.GET().chunkSize().x()) / GameSettings.GET().tileSize();
        int y = (posY % GameSettings.GET().chunkSize().y()) / GameSettings.GET().tileSize();
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

    public TileData getTileByIndex(int x, int y) {
        if (x > tileMap.length || y > tileMap[0].length) {
            return null;
        }
        if (x < 0 || y < 0) {
            return null;
        }
        return tileMap[x][y];
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
