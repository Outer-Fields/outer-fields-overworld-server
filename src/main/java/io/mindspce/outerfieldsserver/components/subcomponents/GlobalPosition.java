package io.mindspce.outerfieldsserver.components.subcomponents;

import io.mindspce.outerfieldsserver.components.Component;
import io.mindspce.outerfieldsserver.core.GameSettings;
import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.enums.AreaId;
import io.mindspce.outerfieldsserver.enums.ComponentType;
import io.mindspce.outerfieldsserver.enums.EntityType;
import io.mindspce.outerfieldsserver.systems.EventData;
import io.mindspce.outerfieldsserver.systems.event.Event;
import io.mindspce.outerfieldsserver.systems.event.EventType;
import io.mindspice.mindlib.data.geometry.IAtomicLine2;
import io.mindspice.mindlib.data.geometry.ILine2;
import io.mindspice.mindlib.data.geometry.IVector2;

import java.util.function.Consumer;


public class GlobalPosition extends Component<GlobalPosition> {

    private volatile AreaId currArea;
    private volatile IVector2 currChunkIndex;
    private volatile IAtomicLine2 globalPosition;
    private volatile long lastUpdateTime;

    public GlobalPosition(Entity parentEntity, AreaId currArea, IVector2 currChunkIndex, IVector2 globalPosition) {
        super(parentEntity, ComponentType.GLOBAL_POSITION);
        this.currArea = currArea;
        this.currChunkIndex = IVector2.of(currChunkIndex);
        this.globalPosition = ILine2.ofAtomic(globalPosition, globalPosition);
    }

    public void updateArea(AreaId newArea) {
        var eventData = new EventData.AreaUpdate(
                parentEntity.entityType() == EntityType.PLAYER,
                currArea,
                newArea
        );
        currArea = newArea;
        Event.Emit.newEntityAreaUpdate(parentEntity, eventData);
    }

    public void updatePosition(int x, int y) {
        int chunkX = x / GameSettings.GET().chunkSize().x();
        int chunkY = y / GameSettings.GET().chunkSize().y();
        if (chunkX != currChunkIndex.x() || chunkY != currChunkIndex.y()) {
            var newChunkIndex = IVector2.of(chunkX, chunkY);
            var eventData = new EventData.ChunkUpdate(
                    parentEntity.entityType() == EntityType.PLAYER,
                    currChunkIndex,
                    newChunkIndex
            );
            currChunkIndex = newChunkIndex;
            Event.Emit.newEntityChunkUpdate(parentEntity, eventData);
        }

        if (currPosition().x() != x || currPosition().y() != y) {
            globalPosition.shiftLine(x, y);
            var eventData = new EventData.PositionUpdate(
                    parentEntity.entityType() == EntityType.PLAYER,
                    IVector2.of(IVector2.of(currPosition()))
            );
            Event.Emit.newEntityPosition(parentEntity, eventData);
        }
    }

    public IVector2 currPosition() {
        return globalPosition.end();
    }

    public IVector2 lastPosition() {
        return globalPosition.start();
    }

    @Override
    public void onEvent(Event<?> event) {
        listenerCache.handleEvent(this, event);
    }

    @Override
    public void onCallBack(Consumer<GlobalPosition> consumer) {
        consumer.accept(this);
    }

    @Override
    public boolean isListenerFor(EventType eventType) {
        return listenerCache.isListenerFor(eventType);
    }

    @Override
    public void onTick(long tickTime, double delta) {

    }


}
