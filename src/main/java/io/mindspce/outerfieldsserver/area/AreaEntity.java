package io.mindspce.outerfieldsserver.area;

import io.mindspce.outerfieldsserver.components.*;
import io.mindspce.outerfieldsserver.components.logic.PredicateLib;
import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.entities.LocationEntity;
import io.mindspce.outerfieldsserver.enums.AreaId;
import io.mindspce.outerfieldsserver.enums.EntityType;

import io.mindspce.outerfieldsserver.systems.EventData;
import io.mindspce.outerfieldsserver.systems.event.Event;
import io.mindspce.outerfieldsserver.systems.event.EventType;
import io.mindspice.mindlib.data.geometry.QuadItem;
import io.mindspice.mindlib.data.geometry.*;
import io.mindspice.mindlib.functional.consumers.BiPredicatedBiConsumer;

import java.util.*;


public class AreaEntity extends Entity {
    private final IRect2 areaSize;
    private final IConcurrentPQuadTree<IPolygon2> collisionGrid;
    private final ChunkMap chunkMap;
    private final ActiveEntities activeEntities;
    private final IVector2 chunkSize;
    private final TrackedEntities<LocationEntity> locationEntities;
    private final SimpleListener entityListener;

    public AreaEntity(int id, AreaId arenaName, ChunkEntity[][] chunkMap, IRect2 areaSize, IVector2 chunkSize,
            List<LocationEntity> initEntities) {
        super(id, EntityType.AREA_ENTITY, arenaName);
        this.areaSize = areaSize;
        this.chunkSize = chunkSize;
        this.chunkMap = ComponentFactory.addChunkMap(this, chunkMap);
        this.activeEntities = ComponentFactory.addActiveEntityGrid(this, 100, areaSize, 6);
        this.collisionGrid = ComponentFactory.addCollisionGrid(this, new IConcurrentPQuadTree<>(areaSize, 6)).collisionGrid;
        this.locationEntities = ComponentFactory.addTrackedEntities(this, initEntities);
        this.entityListener = ComponentFactory.addSimpleListener(this);

        // Listeners to add new locations
        locationEntities.registerListener(EventType.NEW_ENTITY, BiPredicatedBiConsumer.of(
                (TrackedEntities<LocationEntity> te, Event<EventData.NewEntity> event) ->
                        (PredicateLib.isSameAreaEvent(te, event) && event.issuerEntityType() == EntityType.LOCATION_ENTITY),
                (TrackedEntities<LocationEntity> te, Event<EventData.NewEntity> event) -> {
                    LocationEntity entity = EntityType.LOCATION_ENTITY.castOrNull(event.data().entity());
                    if (entity == null) {
                        //TODO log this
                        return;
                    }
                    te.addEntity(entity);
                }
        ));
    }

    public void onNewLocation(Event<EventData.NewEntity> event) {
        LocationEntity entity = EntityType.LOCATION_ENTITY.castOrNull(event.data().entity());
        if (entity == null) {
            //TODO log this
            return;
        }
        locationEntities.addEntity(entity);
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

