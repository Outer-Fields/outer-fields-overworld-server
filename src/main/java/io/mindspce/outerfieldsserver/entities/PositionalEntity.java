package io.mindspce.outerfieldsserver.entities;

import io.mindspce.outerfieldsserver.factory.ComponentFactory;
import io.mindspce.outerfieldsserver.components.entity.GlobalPosition;
import io.mindspce.outerfieldsserver.enums.AreaId;
import io.mindspce.outerfieldsserver.enums.EntityType;
import io.mindspce.outerfieldsserver.systems.EventData;
import io.mindspce.outerfieldsserver.systems.event.EventType;
import io.mindspice.mindlib.data.geometry.IVector2;


public class PositionalEntity extends Entity {

    public PositionalEntity(int id, EntityType entityType,
            AreaId areaId) {
        super(id, entityType, areaId);
        GlobalPosition globalPosition = ComponentFactory.addGlobalPosition(this);
        globalPosition.registerOutputHook(EventType.ENTITY_AREA_CHANGED, this::onAreaChanged, false);
        globalPosition.registerOutputHook(EventType.ENTITY_CHUNK_CHANGED, this::onChunkChanged, false);
    }

    public PositionalEntity(int id, EntityType entityType,
            AreaId areaId, IVector2 position) {
        super(id, entityType, areaId);
        GlobalPosition globalPosition = ComponentFactory.addGlobalPosition(this);
        globalPosition.registerOutputHook(EventType.ENTITY_AREA_CHANGED, this::onAreaChanged, false);
        globalPosition.registerOutputHook(EventType.ENTITY_CHUNK_CHANGED, this::onChunkChanged, false);
        globalPosition.updatePosition(position.x(), position.y());
    }

    private void onChunkChanged(EventData.EntityChunkChanged entityChunkChanged) {
        this.chunkIndex = entityChunkChanged.newChunk();
    }

    private void onAreaChanged(EventData.EntityAreaChanged entityAreaChanged) {
        this.areaId = entityAreaChanged.newArea();
//        this.areaEntity = EntityManager.GET().areaById(areaId);
    }
}
