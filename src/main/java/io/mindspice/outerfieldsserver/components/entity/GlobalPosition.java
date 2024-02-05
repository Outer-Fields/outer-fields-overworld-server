package io.mindspice.outerfieldsserver.components.entity;

import io.mindspice.outerfieldsserver.components.Component;
import io.mindspice.outerfieldsserver.core.WorldSettings;
import io.mindspice.outerfieldsserver.entities.Entity;
import io.mindspice.outerfieldsserver.enums.AreaId;
import io.mindspice.outerfieldsserver.enums.ComponentType;
import io.mindspice.outerfieldsserver.enums.EntityType;
import io.mindspice.outerfieldsserver.systems.event.EventData;
import io.mindspice.outerfieldsserver.systems.event.Event;
import io.mindspice.mindlib.data.geometry.*;
import io.mindspice.outerfieldsserver.systems.event.EventType;

import java.util.List;


public class GlobalPosition extends Component<GlobalPosition> {

    private volatile AreaId currArea;
    private final IVector2 currChunkIndex;
    private final IMutLine2 mVector;
    private final IMutRect2 triggerRect;

    protected GlobalPosition(Entity parentEntity, AreaId currArea, IVector2 currChunkIndex, IVector2 mVector, IRect2 triggerRect) {
        super(parentEntity, ComponentType.GLOBAL_POSITION,
                List.of(EventType.ENTITY_AREA_CHANGED, EventType.ENTITY_CHUNK_CHANGED, EventType.ENTITY_POSITION_CHANGED)
        );
        this.currArea = currArea;
        this.currChunkIndex = IVector2.ofMutable(currChunkIndex);
        this.mVector = ILine2.ofMutable(mVector, mVector);
        this.triggerRect = IRect2.ofMutable(triggerRect);
        registerListener(EventType.ENTITY_AREA_UPDATE, GlobalPosition::onSelfAreaUpdate);
        registerListener(EventType.ENTITY_POSITION_UPDATE, GlobalPosition::onPositionUpdate);
    }

    public GlobalPosition(Entity parentEntity) {
        super(parentEntity, ComponentType.GLOBAL_POSITION,
                List.of(EventType.ENTITY_AREA_CHANGED, EventType.ENTITY_CHUNK_CHANGED, EventType.ENTITY_POSITION_CHANGED)
        );
        currArea = AreaId.NONE;
        currChunkIndex = IVector2.ofMutable(0, 0);
        mVector = ILine2.ofMutable(0, 0, 0, 0);
        this.triggerRect = IRect2.ofMutable(0, 0, 96, 96);
    }

    public void updateArea(AreaId newArea) {
        var eventData = new EventData.EntityAreaChanged(
                parentEntity.entityType() == EntityType.PLAYER,
                currArea,
                newArea,
                IVector2.of(currPosition())
        );
        currArea = newArea;

        emitEvent(Event.entityAreaChanged(this, eventData));
    }

    public void onPositionUpdate(Event<IVector2> positionUpdate) {
        updatePosition(positionUpdate.data().x(), positionUpdate.data().y());
    }

    public void updatePosition(IVector2 pos) {
        updatePosition(pos.x(), pos.y());
    }

    public void onSelfAreaUpdate(Event<AreaId> areaUpdate) {
        if (areaUpdate.recipientEntityId() == entityId()) {
            updateArea(areaUpdate.data());
        }
    }

    public void updatePosition(int x, int y) {
        int chunkX = x / WorldSettings.GET().chunkSize().x();
        int chunkY = y / WorldSettings.GET().chunkSize().y();
        if (chunkX != currChunkIndex.x() || chunkY != currChunkIndex.y()) {
            var newChunkIndex = IVector2.of(chunkX, chunkY);
            var eventData = new EventData.EntityChunkChanged(
                    parentEntity.entityType() == EntityType.PLAYER,
                    currChunkIndex,
                    newChunkIndex
            );
            currChunkIndex.setXY(chunkX, chunkY);
            emitEvent(Event.entityChunkUpdate(this, eventData));
        }

        if (currPosition().x() != x || currPosition().y() != y) {
            mVector.shiftLine(x, y);
            var eventData = new EventData.EntityPositionChanged(
                    parentEntity.entityType() == EntityType.PLAYER,
                    IVector2.of(IVector2.of(lastPosition())),
                    IVector2.of(IVector2.of(currPosition()))
            );
            emitEvent(Event.entityPosition(this, eventData));
        }
    }

    public IVector2 currPosition() {
        return mVector.end();
    }

    public IVector2 lastPosition() {
        return mVector.start();
    }

    public IRect2 triggerRect() {
        return triggerRect.reCenter(currPosition());
    }

    public ILine2 mVector() {
        return mVector;
    }


}
