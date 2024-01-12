package io.mindspce.outerfieldsserver.components.entity;

import io.mindspce.outerfieldsserver.components.Component;
import io.mindspce.outerfieldsserver.core.GameSettings;
import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.enums.AreaId;
import io.mindspce.outerfieldsserver.enums.ComponentType;
import io.mindspce.outerfieldsserver.enums.EntityType;
import io.mindspce.outerfieldsserver.networking.NetSerializable;
import io.mindspce.outerfieldsserver.systems.EventData;
import io.mindspce.outerfieldsserver.systems.event.Event;
import io.mindspice.mindlib.data.geometry.*;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static io.mindspce.outerfieldsserver.systems.event.EventType.*;


public class GlobalPosition extends Component<GlobalPosition> implements NetSerializable {

    private volatile AreaId currArea;
    private volatile IVector2 currChunkIndex;
    private volatile IMutLine2 mVector;

    protected GlobalPosition(Entity parentEntity, AreaId currArea, IVector2 currChunkIndex, IVector2 mVector) {
        super(parentEntity, ComponentType.GLOBAL_POSITION,
                List.of(ENTITY_AREA_CHANGED, ENTITY_CHUNK_CHANGED, ENTITY_POSITION_CHANGED)
        );
        this.currArea = currArea;
        this.currChunkIndex = IVector2.of(currChunkIndex);
        this.mVector = ILine2.ofMutable(mVector, mVector);
      //  registerListener(ENTITY_POSITION_CHANGED, GlobalPosition::onSelfPositionUpdate);
        registerListener(ENTITY_AREA_UPDATE, GlobalPosition::onSelfAreaUpdate);
    }

    public GlobalPosition(Entity parentEntity) {
        super(parentEntity, ComponentType.GLOBAL_POSITION,
                List.of(ENTITY_AREA_CHANGED, ENTITY_CHUNK_CHANGED, ENTITY_POSITION_CHANGED)
        );
        currArea = AreaId.NONE;
        currChunkIndex = IVector2.of(0, 0);
        mVector = ILine2.ofMutable(0, 0, 0, 0);
    }

    public void updateArea(AreaId newArea) {
        var eventData = new EventData.EntityAreaChanged(
                parentEntity.entityType() == EntityType.PLAYER_ENTITY,
                currArea,
                newArea,
                IVector2.of(currPosition())
        );
        currArea = newArea;

        emitEvent(Event.entityAreaChanged(this, eventData));
    }

    public void onSelfPositionUpdate(Event<IVector2> positionUpdate) {
            updatePosition(positionUpdate.data().x(), positionUpdate.data().y());
    }

    public void onSelfPositionUpdate(IVector2 pos) {
        updatePosition(pos.x(), pos.y());
    }

    public void onSelfAreaUpdate(Event<AreaId> areaUpdate) {
        if (areaUpdate.recipientEntityId() == entityId()) {
            updateArea(areaUpdate.data());
        }
    }

    public void updatePosition(int x, int y) {
        int chunkX = x / GameSettings.GET().chunkSize().x();
        int chunkY = y / GameSettings.GET().chunkSize().y();
        if (chunkX != currChunkIndex.x() || chunkY != currChunkIndex.y()) {
            var newChunkIndex = IVector2.of(chunkX, chunkY);
            var eventData = new EventData.EntityChunkChanged(
                    parentEntity.entityType() == EntityType.PLAYER_ENTITY,
                    currChunkIndex,
                    newChunkIndex
            );
            currChunkIndex = newChunkIndex;
            emitEvent(Event.entityChunkUpdate(this, eventData));
        }

        if (currPosition().x() != x || currPosition().y() != y) {
            mVector.shiftLine(x, y);
            var eventData = new EventData.EntityPositionChanged(
                    parentEntity.entityType() == EntityType.PLAYER_ENTITY,
                    IVector2.of(IVector2.of(lastPosition())),
                    IVector2.of(IVector2.of(currPosition()))
            );
            emitEvent(Event.entityPosition(this, eventData));
        }
    }

    public IVector2 currPosition() {
        return mVector.end();
    }

    public Supplier<IVector2> currPositionSupplier() {
        return this::currPosition;
    }

    public IVector2 lastPosition() {
        return mVector.start();
    }

    public ILine2 mVector() {
        return mVector;
    }

    public Supplier<IVector2> lastPositionSupplier() {
        return this::lastPosition;
    }

    public Supplier<ILine2> mVectorSupplier() {
        return this::mVector;
    }



    @Override
    public int byteSize() {
        return 8;
    }

//    @Override
//    public byte[] getBytes() {
//        ByteBuffer buffer = NetSerializable.getEmptyBuffer(8);
//        buffer.putInt(currPosition().x());
//        buffer.putInt(currPosition().y());
//        return buffer.array();
//    }

    @Override
    public void addBytesToBuffer(ByteBuffer buffer) {
        buffer.putInt(currPosition().x());
        buffer.putInt(currPosition().y());
    }
}
