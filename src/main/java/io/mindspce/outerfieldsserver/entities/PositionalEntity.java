package io.mindspce.outerfieldsserver.entities;

import io.mindspce.outerfieldsserver.components.ComponentFactory;
import io.mindspce.outerfieldsserver.components.GlobalPosition;
import io.mindspce.outerfieldsserver.enums.AreaId;
import io.mindspce.outerfieldsserver.enums.EntityType;
import io.mindspce.outerfieldsserver.systems.EventData;
import io.mindspce.outerfieldsserver.systems.event.EventType;


/**
 * A PositionalEntity used to identify entities that exist tangibly in-game, via a chunk instance.
 * Provides access to  an objects current chunk, position must be queried or listened to via events.
 * Comes pre-initialized with a global position component pass-through hooked into update areaId and chunkIndex.
 * Same as entity all exposed fields are thread-safe, but access to components is not and should be done from
 * the same thread that owns the object.
 */
public abstract class PositionalEntity extends Entity {

    /**
     * Instantiates a new Positional entity.
     *
     * @param id         the entity id
     * @param entityType the entity type
     * @param areaId     the area id
     */
    public PositionalEntity(int id, EntityType entityType,
            AreaId areaId) {
        super(id, entityType, areaId);
        GlobalPosition globalPosition = ComponentFactory.addGlobalPosition(this);
        globalPosition.registerOutputHook(EventType.ENTITY_AREA_CHANGED, this::onAreaChanged, false);
        globalPosition.registerOutputHook(EventType.ENTITY_CHUNK_CHANGED, this::onChunkChanged, false);
    }

    private void onChunkChanged(EventData.EntityChunkChanged entityChunkChanged) {
        this.chunkIndex = entityChunkChanged.newChunk();
    }

    private void onAreaChanged(EventData.EntityAreaChanged entityAreaChanged) {
        this.areaId = entityAreaChanged.newArea();
    }
}
