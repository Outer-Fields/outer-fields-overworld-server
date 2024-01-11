package io.mindspce.outerfieldsserver.area;

import io.mindspce.outerfieldsserver.components.*;
import io.mindspce.outerfieldsserver.components.logic.PredicateLib;
import io.mindspce.outerfieldsserver.components.primatives.ComponentSystem;
import io.mindspce.outerfieldsserver.components.primatives.SimpleListener;
import io.mindspce.outerfieldsserver.components.world.ActiveEntities;
import io.mindspce.outerfieldsserver.components.world.ChunkMap;
import io.mindspce.outerfieldsserver.components.world.TrackedEntities;
import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.entities.LocationEntity;
import io.mindspce.outerfieldsserver.enums.AreaId;
import io.mindspce.outerfieldsserver.enums.EntityType;

import io.mindspce.outerfieldsserver.systems.EventData;
import io.mindspce.outerfieldsserver.systems.event.Event;
import io.mindspce.outerfieldsserver.systems.event.EventType;
import io.mindspice.mindlib.data.geometry.QuadItem;
import io.mindspice.mindlib.data.geometry.*;
import io.mindspice.mindlib.data.tuples.Pair;
import io.mindspice.mindlib.functional.consumers.BiPredicatedBiConsumer;

import java.util.*;


public class AreaEntity extends Entity {
    private final IRect2 areaSize;
    private final IVector2 chunkSize;
    private final IConcurrentPQuadTree<IPolygon2> collisionGrid;
    private ChunkMap chunkMap;
    private final ActiveEntities activeEntities;
    private final TrackedEntities trackedEntities;

    public AreaEntity(int id, AreaId arenaName, IRect2 areaSize, IVector2 chunkSize,
            List<Pair<LocationEntity, IVector2>> initLocations) {
        super(id, EntityType.AREA_ENTITY, arenaName);
        this.areaSize = areaSize;
        this.chunkSize = chunkSize;
        this.activeEntities = ComponentFactory.addActiveEntityGrid(this, 100, areaSize, 6);
        this.collisionGrid = ComponentFactory.addCollisionGrid(this, new IConcurrentPQuadTree<>(areaSize, 6)).collisionGrid;
        this.trackedEntities = ComponentFactory.addTrackedEntities(this);

        initLocations.forEach(l -> {
            activeEntities.addActiveEntity(l.first(), l.second());
            trackedEntities.addEntity(l.first());
        });

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

