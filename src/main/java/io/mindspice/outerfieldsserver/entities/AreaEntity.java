package io.mindspice.outerfieldsserver.entities;

import io.mindspice.outerfieldsserver.area.TileData;
import io.mindspice.outerfieldsserver.components.world.ActiveEntities;
import io.mindspice.outerfieldsserver.components.world.EntityGrid;
import io.mindspice.outerfieldsserver.components.world.ChunkMap;
import io.mindspice.outerfieldsserver.enums.AreaId;
import io.mindspice.outerfieldsserver.enums.EntityType;

import io.mindspice.outerfieldsserver.factory.ComponentFactory;
import io.mindspice.mindlib.data.geometry.QuadItem;
import io.mindspice.mindlib.data.geometry.*;
import io.mindspice.mindlib.data.tuples.Pair;

import java.util.*;


public class AreaEntity extends Entity {
    private final IVector2 chunkSize;
    private final IConcurrentPQuadTree<IPolygon2> collisionGrid;
    private ChunkMap chunkMap;
    private final EntityGrid entityGrid;
    private final IRect2 areaSize;
    protected final ActiveEntities activeEntities;

    public AreaEntity(int id, AreaId arenaName, IRect2 areaSize, IVector2 chunkSize,
            List<Pair<LocationEntity, IVector2>> initLocations, int initialSetSize) {
        super(id, EntityType.AREA, arenaName);
        this.areaSize = areaSize;
        this.chunkSize = chunkSize;
        this.entityGrid = ComponentFactory.addEntityGrid(this, areaSize, 6);
        this.collisionGrid = ComponentFactory.addCollisionGrid(this, new IConcurrentPQuadTree<>(areaSize, 6)).collisionGrid;
        activeEntities = ComponentFactory.addActiveEntities(this, initialSetSize);
        initLocations.forEach(l -> activeEntities.activeEntities.add(l.first().entityId()));
        initLocations.forEach(l -> entityGrid.addActiveEntity(l.first(), l.second()));

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

    public TileData getTileByGlobalPosition(IVector2 globalPosition) {
        ChunkEntity chunk = chunkMap.getChunkByGlobalPos(globalPosition);
        return chunk == null ? null : chunk.getTileByGlobalPos(globalPosition);
    }

    public ChunkEntity getChunkByIndex(IVector2 index) {
        return chunkMap.getChunkByIndex(index);
    }

    public void addCollisionToGrid(List<IPolygon2> collisionPolys) {
        collisionPolys.forEach(poly -> collisionGrid.insert(poly, poly));
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", AreaEntity.class.getSimpleName() + "[", "]")
                .add("areaSize=" + areaSize)
                .add("chunkSize=" + chunkSize)
                .add("entityType=" + entityType)
                .add("id=" + id)
                .add("name='" + name + "'")
                .add("areaId=" + areaId)
                .add("chunkIndex=" + chunkIndex)
                .add("attachedComponents=" + getAttachedComponentTypes())
                .add("listeningFor=" + getListeningFor())
                .add("systemRegistry=" + systemRegistry.systemType())
                .toString();
    }
}

