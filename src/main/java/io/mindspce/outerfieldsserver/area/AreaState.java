package io.mindspce.outerfieldsserver.area;

import io.mindspce.outerfieldsserver.core.GameSettings;

import io.mindspce.outerfieldsserver.core.singletons.EntityManager;
import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.entities.locations.LocationEntity;
import io.mindspce.outerfieldsserver.entities.player.PlayerState;
import io.mindspce.outerfieldsserver.data.wrappers.ActiveEntityUpdate;
import io.mindspce.outerfieldsserver.enums.AreaId;
import io.mindspce.outerfieldsserver.enums.EntityEventType;
import io.mindspce.outerfieldsserver.systems.event.EventType;
import io.mindspce.outerfieldsserver.systems.event.EntityEvent;
import io.mindspice.mindlib.data.geometry.QuadItem;
import io.mindspice.mindlib.data.geometry.*;

import io.mindspice.mindlib.data.tuples.Pair;
import jakarta.annotation.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;


public class AreaState {
    private final AreaId arenaName;
    private final ChunkData[][] chunkMap;
    private final IVector2 areaSize;
    private final Set<PlayerState> activePlayers = Collections.synchronizedSet(new HashSet<>(20));
    private final Map<IVector2, AtomicInteger> activeChunks = new ConcurrentHashMap<>(100);
    private final IConcurrentVQuadTree<Entity> entityGrid;
    private final IConcurrentPQuadTree<LocationEntity> locationGrid;
    private final IConcurrentPQuadTree<IPolygon2> collisionGrid;

    private final ConcurrentLinkedDeque<ActiveEntityUpdate> entityUpdateQueue = new ConcurrentLinkedDeque<>();

    public AreaState(AreaId arenaName, ChunkData[][] chunkMap) {
        this.arenaName = arenaName;
        this.chunkMap = chunkMap;
        areaSize = IVector2.of(chunkMap.length, chunkMap[0].length);
        entityGrid = new IConcurrentVQuadTree<>(
                IRect2.of(0, 0, chunkMap.length * GameSettings.GET().chunkSize().x(),
                        chunkMap[0].length * GameSettings.GET().chunkSize().y()),
                5
        );
        locationGrid = new IConcurrentPQuadTree<>(
                IRect2.of(0, 0, chunkMap.length * GameSettings.GET().chunkSize().x(),
                        chunkMap[0].length * GameSettings.GET().chunkSize().y()),
                5
        );
        collisionGrid = new IConcurrentPQuadTree<>(
                IRect2.of(0, 0, chunkMap.length * GameSettings.GET().chunkSize().x(),
                        chunkMap[0].length * GameSettings.GET().chunkSize().y()),
                5
        );
    }

    public void submitEntityUpdate(ActiveEntityUpdate entityUpdate) {
        if (entityUpdate == null) { return; }
        entityUpdateQueue.add(entityUpdate);
    }

    public List<QuadItem<Entity>> queryEntityGrid(IRect2 querySpace) {
        return entityGrid.query(querySpace);
    }

    public List<QuadItem<Entity>> queryEntityGrid(IRect2 querySpace, List<QuadItem<Entity>> updateList) {
        return entityGrid.query(querySpace, updateList);
    }

    public void updateGridEntity(IVector2 oldPos, IVector2 newPos, Entity entity) {
        entityGrid.update(oldPos, newPos, entity);
    }

    public void updateGridEntity(ILine2 mVec, Entity entity) {
        entityGrid.update(mVec.start(), mVec.end(), entity);
        EntityManager.GET().emitEntityEvent(new EntityEvent(arenaName, EntityEventType.NEW_POSITION));
    }

    public void addEntityToGrid(IVector2 pos, Entity entity) {
        entityGrid.insert(pos, entity);
    }

    public void removeEntityFromGrid(IVector2 pos, Entity entity) {
        entityGrid.remove(pos, entity);
    }

    public void addLocationToGrid(List<Pair<IPolygon2, LocationEntity>> locations) {
        locations.forEach(l -> addLocationToGrid(l.first(), l.second()));
    }

    public void addLocationToGrid(IPolygon2 locationBounds, LocationEntity location) {
        locationGrid.insert(locationBounds, location);
    }

    public void removeLocationFromGrid(IPolygon2 locationBounds, LocationEntity location) {
        locationGrid.remove(locationBounds, location);
    }

    public void updateLocationInGrid(IPolygon2 oldBounds, IPolygon2 newBounds, LocationEntity location) {
        locationGrid.update(oldBounds, newBounds, location);
    }

    public void addCollisionToGrid(List<IPolygon2> collisionPolys) {
        collisionPolys.forEach(this::addCollisionToGrid);
    }

    public void addCollisionToGrid(IPolygon2 collisionPoly) {
        collisionGrid.insert(collisionPoly, collisionPoly);
    }

    public void removeCollisionFromGrid(IPolygon2 collisionPoly) {
        collisionGrid.remove(collisionPoly, collisionPoly);
    }

    public List<QuadItem<IPolygon2>> queryCollisionGrid(IRect2 areaRect) {
        return collisionGrid.query(areaRect);
    }

//    public void boad() {
//        int queueSize = entityUpdateQueue.size();
//        for (int i = 0; i < queueSize; ++i) {
//            ActiveEntityUpdate entityUpdate = entityUpdateQueue.poll();
//
//        }
//    }

    public void addActivePlayer(PlayerState playerState) {
        activePlayers.add(playerState);
    }

    public void removeActivePlayer(PlayerState playerState) {
        activePlayers.remove(playerState);
    }

    public AreaId getId() {
        return arenaName;
    }

    public IVector2 getAreaSize() {
        return areaSize;
    }

    public ChunkData[][] getChunkMap() {
        return chunkMap;
    }

    @Nullable
    public ChunkData getChunkByGlobalPos(IVector2 pos) {
        int x = pos.x() / GameSettings.GET().chunkSize().x();
        int y = pos.y() / GameSettings.GET().chunkSize().y();
        if (x < 0 || y < 0 || x >= chunkMap.length || y >= chunkMap[0].length) {
            return null;
        }
        return chunkMap[x][y];
    }

    @Nullable
    public ChunkData getChunkByGlobalPos(int posX, int posY) {
        int x = posX / GameSettings.GET().chunkSize().x();
        int y = posY / GameSettings.GET().chunkSize().y();
        if (x < 0 || y < 0 || x >= chunkMap.length || y >= chunkMap[0].length) {
            return null; // return null, this is acceptable and should be handled by caller
        }
        return chunkMap[x][y];
    }

    @Nullable
    public ChunkData getChunkByIndex(IVector2 index) {
        if (index.x() < 0 || index.y() < 0 || index.x() >= chunkMap.length || index.y() >= chunkMap[0].length) {
            return null;
        }
        return chunkMap[index.x()][index.y()];
    }

    @Nullable
    public ChunkData getChunkByIndex(int x, int y) {
        if (x < 0 || y < 0 || x >= chunkMap.length || y >= chunkMap[0].length) {
            return null;
        }
        return chunkMap[x][y];
    }

//

    public Set<PlayerState> getActivePlayers() {
        return activePlayers;
    }

    public void subscribeToChunk(IVector2 chunkIndex, EventType eventType, PlayerState playerState) {
        ChunkData chunk = getChunkByIndex(chunkIndex);
        if (chunk != null) {
            System.out.println("subscribed: " + chunkIndex);
            chunk.subscribe(eventType, playerState);
        }
    }

    public void unSubscribeToChunk(IVector2 chunkIndex, EventType eventType, PlayerState playerState) {
        ChunkData chunk = getChunkByIndex(chunkIndex);
        if (chunk != null) {
            System.out.println("unsubscribed: " + chunkIndex);

            chunk.unsubscribe(eventType, playerState);
        }
    }

}

