package io.mindspce.outerfieldsserver.area;

import io.mindspce.outerfieldsserver.core.GameSettings;
import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.systems.event.Subscribable;
import io.mindspce.outerfieldsserver.entities.player.PlayerState;
import io.mindspce.outerfieldsserver.enums.EventType;
import io.mindspice.mindlib.data.geometry.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;


public class ChunkData implements Subscribable<PlayerState> {
    private final IVector2 index;
    private final IVector2 globalPos;
    private final IRect2 boundsRect;
    private final TileData[][] tileMap;
    private final Map<Integer, IPolygon2> collisions;
    private final IConcurrentVQuadTree<Entity> entityGrid;

    private final Map<EventType, Set<PlayerState>> subscriptions = new ConcurrentHashMap<>();

    public ChunkData(IVector2 index, IVector2 globalPos, IVector2 size, TileData[][] tileMap, Map<Integer, IPolygon2> collisions) {
        this.index = index;
        this.tileMap = tileMap;
        this.collisions = collisions;
        this.globalPos = globalPos;
        this.entityGrid = new IConcurrentVQuadTree<>(IRect2.of(globalPos, size), 6);
        boundsRect = IRect2.of(globalPos, GameSettings.GET().chunkSize());
    }

    public void updateEntityPosition(Entity entity) {
        // TODO could debug log unfound entities on updates;
        entityGrid.update(entity.globalPosition(), entity.globalPosition(), entity);
    }

    public void addEntity(Entity entity) {
        entityGrid.insert(entity.globalPosition(), entity);
    }

    public void removeEntity(Entity entity) {
        entityGrid.remove(entity.globalPosition(), entity);
    }

    public List<QuadItem<Entity>> queryEntityGrid(IRect2 querySpace) {
        return entityGrid.query(querySpace);
    }

    public List<QuadItem<Entity>> queryEntityGrid(IRect2 querySpace, List<QuadItem<Entity>> updateList) {
        return entityGrid.query(querySpace, updateList);
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

    // Subscribable logic
    @Override
    public void subscribe(EventType eventType, PlayerState session) {
        subscriptions.computeIfAbsent(eventType, k ->
                Collections.newSetFromMap(new ConcurrentHashMap<>())
        ).add(session);
    }

    @Override
    public void unsubscribe(EventType eventType, PlayerState session) {
        if (subscriptions.containsKey(eventType)) {
            subscriptions.get(eventType).remove(session);
        }
    }

    @Override
    public void broadcast(EventType eventType, Consumer<PlayerState> action) {
        getSubscribers(eventType).forEach(action);
    }

    @Override
    public Set<PlayerState> getSubscribers(EventType eventType) {
        return subscriptions.getOrDefault(eventType, Set.of());
    }
}
