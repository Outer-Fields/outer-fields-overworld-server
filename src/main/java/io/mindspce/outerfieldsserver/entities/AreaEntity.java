package io.mindspce.outerfieldsserver.entities;

import io.mindspce.outerfieldsserver.components.world.EntityGrid;
import io.mindspce.outerfieldsserver.components.world.ChunkMap;
import io.mindspce.outerfieldsserver.enums.AreaId;
import io.mindspce.outerfieldsserver.enums.EntityType;

import io.mindspce.outerfieldsserver.factory.ComponentFactory;
import io.mindspice.mindlib.data.geometry.QuadItem;
import io.mindspice.mindlib.data.geometry.*;
import io.mindspice.mindlib.data.tuples.Pair;

import java.util.*;


public class AreaEntity extends Entity {
    private final IRect2 areaSize;
    private final IVector2 chunkSize;
    private final IConcurrentPQuadTree<IPolygon2> collisionGrid;
    private ChunkMap chunkMap;
    private final EntityGrid entityGrid;

    public AreaEntity(int id, AreaId arenaName, IRect2 areaSize, IVector2 chunkSize,
            List<Pair<LocationEntity, IVector2>> initLocations, List<Pair<ItemEntity, IVector2>> initItems) {
        super(id, EntityType.AREA, arenaName);
        this.areaSize = areaSize;
        this.chunkSize = chunkSize;
        this.entityGrid = ComponentFactory.addEntityGrid(this, 100, areaSize, 6);
        this.collisionGrid = ComponentFactory.addCollisionGrid(this, new IConcurrentPQuadTree<>(areaSize, 6)).collisionGrid;

        initLocations.forEach(l -> entityGrid.addActiveEntity(l.first(), l.second()));
        initItems.forEach(i -> entityGrid.addActiveEntity(i.first(), i.second()));
    }

    public void setChunkMap(ChunkEntity[][] chunkMap) {
        this.chunkMap = ComponentFactory.addChunkMap(this, chunkMap);
    }

    public List<QuadItem<IPolygon2>> queryCollisionGrid(IRect2 areaRect) {
        return collisionGrid.query(areaRect);
    }

    public IVector2 getAreaSize() {
        return areaSize.size();
    }

    public ChunkEntity getChunkByGlobalPos(IVector2 globalPosition) {
        return chunkMap.getChunkByGlobalPos(globalPosition);
    }

    public ChunkEntity getChunkByIndex(IVector2 index) {
        return chunkMap.getChunkByIndex(index);
    }

    public void addCollisionToGrid(List<IPolygon2> collisionPolys) {
        collisionPolys.forEach(poly -> collisionGrid.insert(poly, poly));
    }
}

