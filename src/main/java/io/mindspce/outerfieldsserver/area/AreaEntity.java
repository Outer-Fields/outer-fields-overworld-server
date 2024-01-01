package io.mindspce.outerfieldsserver.area;

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

    public AreaEntity(int id, AreaId arenaName, ChunkEntity[][] chunkMap, IRect2 areaSize,
            IConcurrentPQuadTree<IPolygon2> collisionGrid) {
        super(id, EntityType.AREA, arenaName);
        this.areaSize = areaSize;
        ComponentFactory.addChunkMap(this, chunkMap);
        ComponentFactory.addActiveEntityGrid(this, 100, areaSize, 6);
        this.collisionGrid = ComponentFactory.addCollisionGrid(this, collisionGrid).collisionGrid;


    }

    public List<QuadItem<IPolygon2>> queryCollisionGrid(IRect2 areaRect) {
        return collisionGrid.query(areaRect);
    }

    public IVector2 getAreaSize() {
        return areaSize.size();
    }




}

