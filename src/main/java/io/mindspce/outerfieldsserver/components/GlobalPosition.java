package io.mindspce.outerfieldsserver.components;

import io.mindspce.outerfieldsserver.core.GameSettings;
import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.enums.AreaId;
import io.mindspce.outerfieldsserver.enums.ComponentType;
import io.mindspce.outerfieldsserver.enums.EntityType;
import io.mindspce.outerfieldsserver.systems.EventData;
import io.mindspce.outerfieldsserver.systems.event.Event;
import io.mindspice.mindlib.data.geometry.*;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static io.mindspce.outerfieldsserver.systems.event.EventType.*;


public class GlobalPosition extends Component<GlobalPosition> {

    private volatile AreaId currArea;
    private volatile IVector2 currChunkIndex;
    private volatile IMutLine2 mVector;

    protected GlobalPosition(Entity parentEntity, AreaId currArea,  IVector2 currChunkIndex, IVector2 mVector) {
        super(parentEntity, ComponentType.GLOBAL_POSITION,
                List.of(ENTITY_AREA_CHANGED, ENTITY_CHUNK_CHANGED, ENTITY_POSITION_CHANGED)
        );
        this.currArea = currArea;
        this.currChunkIndex = IVector2.of(currChunkIndex);
        this.mVector = ILine2.ofMutable(mVector, mVector);
        registerListener(ENTITY_POSITION_CHANGED, GlobalPosition::onSelfPositionUpdate);
        registerListener(ENTITY_AREA_UPDATE, GlobalPosition::onSelfPositionUpdate);
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
                parentEntity.entityType() == EntityType.PLAYER,
                currArea,
                newArea,
                IVector2.of(currPosition())
        );
        currArea = newArea;

        emitEvent(Event.Factory.newEntityAreaChanged(this, eventData));
    }

    public void onSelfPositionUpdate(Event<IVector2> positionUpdate) {
        if (positionUpdate.recipientEntityId() == entityId()) {
            updatePosition(positionUpdate.data().x(), positionUpdate.data().y());
        }
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
                    parentEntity.entityType() == EntityType.PLAYER,
                    currChunkIndex,
                    newChunkIndex
            );
            currChunkIndex = newChunkIndex;
            emitEvent(Event.Factory.newEntityChunkUpdate(this, eventData));
        }

        if (currPosition().x() != x || currPosition().y() != y) {
            mVector.shiftLine(x, y);
            var eventData = new EventData.EntityPositionChanged(
                    parentEntity.entityType() == EntityType.PLAYER,
                    IVector2.of(IVector2.of(lastPosition())),
                    IVector2.of(IVector2.of(currPosition()))
            );
            emitEvent(Event.Factory.newEntityPosition(this, eventData));
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

    public void addConsumer(Consumer<GlobalPosition> consumer) {

    }
}
