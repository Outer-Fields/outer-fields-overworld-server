package io.mindspce.outerfieldsserver.core.systems;

import io.mindspce.outerfieldsserver.entities.AreaEntity;
import io.mindspce.outerfieldsserver.core.singletons.EntityManager;
import io.mindspce.outerfieldsserver.enums.AreaId;
import io.mindspce.outerfieldsserver.enums.SystemType;
import io.mindspce.outerfieldsserver.systems.event.*;

import java.util.Map;


public class WorldSystem extends SystemListener {
    private final Map<AreaId, AreaEntity> areaMap;

    public WorldSystem(boolean doStart, Map<AreaId, AreaEntity> areaMap) {
        super(SystemType.WORLD, doStart);
        this.areaMap = areaMap;
        areaMap.values().forEach(this::onRegisterComponent);
        EntityManager.GET().registerSystem(this);
    }
}
