package io.mindspice.outerfieldsserver.components.player;

import io.mindspice.outerfieldsserver.entities.AreaEntity;
import io.mindspice.outerfieldsserver.components.Component;
import io.mindspice.outerfieldsserver.core.singletons.EntityManager;
import io.mindspice.outerfieldsserver.data.wrappers.DynamicTileRef;
import io.mindspice.outerfieldsserver.entities.Entity;
import io.mindspice.outerfieldsserver.enums.AreaId;
import io.mindspice.outerfieldsserver.enums.ComponentType;
import io.mindspice.outerfieldsserver.systems.event.EventData;
import io.mindspice.outerfieldsserver.systems.event.Event;
import io.mindspice.mindlib.data.collections.other.GridArray;
import io.mindspice.mindlib.data.geometry.IVector2;

import java.util.List;


public class LocalTileGrid extends Component<LocalTileGrid> {
    private final GridArray<DynamicTileRef> localTileGrid;

    public LocalTileGrid(Entity parentEntity, int size, AreaId areaId) {
        super(parentEntity, ComponentType.LOCAL_TILE_GRID, List.of());
        this.localTileGrid = new GridArray<>(size, size);
        for (int x = 0; x < size; ++x) {
            for (int y = 0; y < size; ++y) {
                localTileGrid.set(x, y, new DynamicTileRef(areaId.entity, IVector2.of(-x, -y)));
            }
        }

    }

    public void onSelfPositionChanged(Event<EventData.EntityPositionChanged> event) {
        calcPositionChange(event.data().newPosition());
    }

    public void onSelfAreaChanged(Event<EventData.EntityAreaChanged> event) {
        calcAreaChange(event.data().newArea());
    }

    private void calcPositionChange(IVector2 selfPosition) {
        var t = System.nanoTime();
        for (int x = 0; x < localTileGrid.getWidth(); ++x) {
            for (int y = 0; y < localTileGrid.getHeight(); ++y) {
                localTileGrid.get(x, y).updatePos(selfPosition);
            }
        }
    }

    public void calcAreaChange(AreaId areaId) {
        AreaEntity area = EntityManager.GET().areaById(areaId);
        for (int x = 0; x < localTileGrid.getWidth(); ++x) {
            for (int y = 0; y < localTileGrid.getHeight(); ++y) {
                localTileGrid.get(x, y).updateAreaRef(area);
            }
        }
    }

    // Note provides direct access can change, should only be used internally for controllers
    public GridArray<DynamicTileRef> tileGrid() {
        return localTileGrid;
    }


}
