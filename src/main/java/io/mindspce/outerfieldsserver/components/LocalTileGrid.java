package io.mindspce.outerfieldsserver.components;

import io.mindspce.outerfieldsserver.area.AreaEntity;
import io.mindspce.outerfieldsserver.core.singletons.EntityManager;
import io.mindspce.outerfieldsserver.data.wrappers.DynamicTileRef;
import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.enums.AreaId;
import io.mindspce.outerfieldsserver.enums.ComponentType;
import io.mindspce.outerfieldsserver.enums.QueryType;
import io.mindspce.outerfieldsserver.systems.EventData;
import io.mindspce.outerfieldsserver.systems.event.Event;
import io.mindspce.outerfieldsserver.systems.event.EventType;
import io.mindspice.mindlib.data.collections.other.GridArray;
import io.mindspice.mindlib.data.geometry.IVector2;

import java.util.List;


public class LocalTileGrid extends Component<LocalTileGrid> {
    private final GridArray<DynamicTileRef> localTileGrid;

    public LocalTileGrid(Entity parentEntity, int size) {
        super(parentEntity, ComponentType.LOCAL_TILE_GRID, List.of());
        this.localTileGrid = new GridArray<>(size, size);
        registerListener(EventType.ENTITY_POSITION_CHANGED, LocalTileGrid::onSelfPositionChanged);
        registerListener(EventType.ENTITY_AREA_CHANGED, LocalTileGrid::onSelfAreaChanged);

    }

    public void onSelfPositionChanged(Event<EventData.EntityPositionChanged> event) {
        calcPositionChange(event.data().newPosition());
    }

    public void onSelfAreaChanged(Event<EventData.EntityAreaChanged> event) {
        calcAreaChange(event.data().newArea());
    }

    private void calcPositionChange(IVector2 selfPosition) {
        for (int x = 0; x < localTileGrid.getWidth(); ++x) {
            for (int y = 0; y < localTileGrid.getHeight(); ++y) {
                localTileGrid.get(x, y).updatePos(selfPosition);
            }
        }
    }

    public void calcAreaChange(AreaId areaId) {
        AreaEntity area = EntityManager.GET().areaState(areaId);
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
