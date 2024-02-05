package io.mindspice.outerfieldsserver.entities;

import io.mindspice.mindlib.data.geometry.IRect2;
import io.mindspice.mindlib.data.geometry.IVector2;
import io.mindspice.outerfieldsserver.enums.AreaId;
import io.mindspice.outerfieldsserver.enums.EntityType;


public class FarmPlotEntity extends PositionalEntity{
    private final IRect2 plotArea;
    public FarmPlotEntity(int id, AreaId areaId, IVector2 position, IRect2 plotArea) {
        super(id, EntityType.FARM_PLOT, areaId, position);
        this.plotArea = plotArea;
    }


}
