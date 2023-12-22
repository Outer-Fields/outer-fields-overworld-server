package io.mindspce.outerfieldsserver.area;

import io.mindspce.outerfieldsserver.core.GameSettings;
import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.systems.event.Subscribable;
import io.mindspce.outerfieldsserver.entities.player.PlayerState;
import io.mindspce.outerfieldsserver.enums.EventType;
import io.mindspce.outerfieldsserver.util.GridUtils;
import io.mindspice.mindlib.data.geometry.*;
import jakarta.annotation.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Consumer;


public class ChunkData implements Subscribable<PlayerState> {
    private final IVector2 index;
    private final IVector2 globalPos;
    private final IRect2 boundsRect;
    private final HashMap<IVector2, TileData> activeTiles;
    private Set<Entity> activeEntities = new HashSet<>(100);
    private final StampedLock lock = new StampedLock();

    private final Map<EventType, Set<PlayerState>> subscriptions = new ConcurrentHashMap<>();

    public ChunkData(IVector2 index, HashMap<IVector2, TileData> activeTiles) {
        this.index = index;
        this.activeTiles = activeTiles;

        this.globalPos = GridUtils.chunkIndexToGlobal(index);
        boundsRect = IRect2.of(globalPos, GameSettings.GET().chunkSize());
    }

    public void addActiveEntity(Entity entity) {
        System.out.println("Added:" + " Chunk: " + index);

        if (activeEntities.contains(entity)) { return; }
        long stamp = lock.writeLock();
        try {
            activeEntities.add(entity);
        } finally {
            lock.unlockWrite(stamp);

        }
    }

    public void removeActiveEntity(Entity entity) {
        System.out.println("Removed:" + " Chunk: " + index);
        long stamp = lock.writeLock();
        try {
            activeEntities.remove(entity);
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    public List<Entity> getActiveEntitiesCopy() {
        long stamp = -1;
        List<Entity> activeEntitiesList = null;
        do {
            stamp = lock.tryOptimisticRead();
            activeEntitiesList = List.copyOf(activeEntities);
        } while (!lock.validate(stamp));
        return activeEntitiesList;
    }

    public IVector2 getGlobalPos() {
        return globalPos;
    }

    public IVector2 index() {
        return index;
    }

    public IRect2 getBoundsRect() {
        return boundsRect;
    }


//    @Nullable
//    public TileData getTileByLocalPos(IVector2 pos) {
//        int x = pos.x() / GameSettings.GET().tileSize();
//        int y = pos.y() / GameSettings.GET().tileSize();
//        if (index.x() < 0 || index.y() < 0 || index.x() >= tileMap.length || index.y() >= tileMap[0].length) {
//            return null;
//        }
//        return tileMap[x][y];
//    }
//
//    @Nullable
//    public TileData getTileByLocalPos(int posX, int posY) {
//        int x = posX / GameSettings.GET().tileSize();
//        int y = posY / GameSettings.GET().tileSize();
//        if (index.x() < 0 || index.y() < 0 || index.x() >= tileMap.length || index.y() >= tileMap[0].length) {
//            return null;
//        }
//        return tileMap[x][y];
//    }

    @Nullable
    public TileData getTileByGlobalPos(IVector2 pos) {
        return activeTiles.get(GridUtils.globalToLocalTile(index, pos));
//        int x = (pos.x() % GameSettings.GET().chunkSize().x()) / GameSettings.GET().tileSize();
//        int y = (pos.y() % GameSettings.GET().chunkSize().y()) / GameSettings.GET().tileSize();
//        if (x < 0 || y < 0 || x >= tileMap.length || y >= tileMap[0].length) {
//            return null;
//        }
//        return tileMap[x][y];
    }

//    @Nullable
//    public TileData getTileByGlobalPos(int posX, int posY) {
//        int x = (posX % GameSettings.GET().chunkSize().x()) / GameSettings.GET().tileSize();
//        int y = (posY % GameSettings.GET().chunkSize().y()) / GameSettings.GET().tileSize();
//        if (x < 0 || y < 0 || x >= tileMap.length || y >= tileMap[0].length) {
//            return null;
//        }
//        return tileMap[x][y];
//    }

    @Nullable
    public TileData getTileByIndex(IVector2 index) {
        return activeTiles.get(index);
    }
//    @Nullable
//    public TileData getTileByIndex(int x, int y) {
//        if (x < 0 || y < 0 || x >= tileMap.length || y >= tileMap[0].length) {
//            return null;
//        }
//        return tileMap[x][y];
//    }
//
//    public TileData[][] getTileMap() {
//        return tileMap;
//    }
//
//    public IVector2[][] getVectorMap() {
//        IVector2[][] vecMap = new IVector2[tileMap.length][tileMap[0].length];
//        for (int i = 0; i < tileMap.length; ++i) {
//            for (int j = 0; j < tileMap[0].length; ++j) {
//                vecMap[i][j] = tileMap[i][j].index();
//            }
//        }
//        return vecMap;
//    }
//
//    public IVector2 getIndex() {
//        return index;
//    }

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

    static public ChunkData loadFromJson(ChunkJson json) {
        HashMap<IVector2, TileData> tileData = new HashMap<>(50);

        for (var collision : json.collisionMask()) {
            tileData.put(collision, new TileData(collision, null, true, false));
        }
        for (var area : json.areaMask()) {
            if (tileData.containsKey(area)) {
                tileData.get(area).withLocationChange(true);
            }
            tileData.put(area, new TileData(area, null, false, true));
        }

        for (var navigation : json.navMask()) {
            if (tileData.containsKey(navigation)) {
                tileData.get(navigation).withNavChange(new NavData());
            }
            tileData.put(navigation, new TileData(navigation, new NavData(), false, false));
        }
        return new ChunkData(json.index(), tileData);
    }
}
