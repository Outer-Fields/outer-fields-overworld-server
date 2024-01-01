package io.mindspce.outerfieldsserver.core.statemanagers;

import io.mindspce.outerfieldsserver.area.AreaEntity;
import io.mindspce.outerfieldsserver.core.singletons.EntityManager;
import io.mindspce.outerfieldsserver.enums.AreaId;
import io.mindspce.outerfieldsserver.enums.SystemType;
import io.mindspce.outerfieldsserver.systems.event.*;

import java.util.Map;


public class WorldState extends CoreSystem {
    private final Map<AreaId, AreaEntity> areaMap;

    public WorldState(boolean doStart, Map<AreaId, AreaEntity> areaMap) {
        super(SystemType.WORLD_STATE, doStart);
        this.areaMap = areaMap;
        areaMap.values().forEach(area -> area.registerComponents(this));
        EntityManager.GET().registerEventListener(this);
    }


}
