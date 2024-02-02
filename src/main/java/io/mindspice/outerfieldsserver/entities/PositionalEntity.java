package io.mindspice.outerfieldsserver.entities;

import io.mindspice.mindlib.functional.consumers.PredicatedConsumer;
import io.mindspice.outerfieldsserver.components.entity.GlobalPosition;
import io.mindspice.outerfieldsserver.components.primatives.SimpleListener;
import io.mindspice.outerfieldsserver.components.serialization.Visibility;
import io.mindspice.outerfieldsserver.core.singletons.EntityManager;
import io.mindspice.outerfieldsserver.enums.AreaId;
import io.mindspice.outerfieldsserver.enums.EntityType;
import io.mindspice.outerfieldsserver.systems.event.Event;
import io.mindspice.outerfieldsserver.systems.event.EventData;
import io.mindspice.outerfieldsserver.systems.event.EventType;
import io.mindspice.mindlib.data.geometry.IVector2;

import java.util.List;


public class PositionalEntity extends Entity {
    private SimpleListener selfListener;

    public PositionalEntity(int id, EntityType entityType, AreaId areaId, IVector2 position) {
        super(id, entityType, areaId);
        GlobalPosition globalPosition = new GlobalPosition(this);
        addComponent(globalPosition);
        globalPosition.registerOutputHook(EventType.ENTITY_AREA_CHANGED, this::onAreaChanged, false);
        globalPosition.registerOutputHook(EventType.ENTITY_CHUNK_CHANGED, this::onChunkChanged, false);
        globalPosition.updatePosition(position.x(), position.y());

        Visibility visibility = new Visibility(this);
        addComponent(visibility);
    }

    private void onChunkChanged(EventData.EntityChunkChanged entityChunkChanged) {
        this.chunkIndex = entityChunkChanged.newChunk();
    }

    private void onAreaChanged(EventData.EntityAreaChanged entityAreaChanged) {
        this.areaId = entityAreaChanged.newArea();
//        this.areaEntity = EntityManager.GET().areaById(areaId);
    }


}
