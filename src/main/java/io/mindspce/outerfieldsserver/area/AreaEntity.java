package io.mindspce.outerfieldsserver.area;

import io.mindspce.outerfieldsserver.components.ActiveEntities;
import io.mindspce.outerfieldsserver.components.ChunkMap;
import io.mindspce.outerfieldsserver.components.ComponentFactory;
import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.enums.AreaId;
import io.mindspce.outerfieldsserver.enums.EntityType;

import io.mindspice.mindlib.data.geometry.QuadItem;
import io.mindspice.mindlib.data.geometry.*;

import java.util.*;


public class AreaEntity extends Entity {
    private final IRect2 areaSize;
    private final IConcurrentPQuadTree<IPolygon2> collisionGrid;
    private final ChunkMap chunkMap;
    private final ActiveEntities activeEntities;
    private final IVector2 chunkSize;

    public AreaEntity(int id, AreaId arenaName, ChunkEntity[][] chunkMap, IRect2 areaSize, IVector2 chunkSize) {
        super(id, EntityType.AREA_ENTITY, arenaName);
        this.areaSize = areaSize;
        this.chunkSize = chunkSize;
        this.chunkMap = ComponentFactory.addChunkMap(this, chunkMap);
        this.activeEntities = ComponentFactory.addActiveEntityGrid(this, 100, areaSize, 6);
        this.collisionGrid = ComponentFactory.addCollisionGrid(this, new IConcurrentPQuadTree<>(areaSize, 6)).collisionGrid;
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

