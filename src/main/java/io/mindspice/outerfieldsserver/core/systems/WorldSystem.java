package io.mindspice.outerfieldsserver.core.systems;

import io.mindspice.outerfieldsserver.entities.AreaEntity;
import io.mindspice.outerfieldsserver.enums.AreaId;
import io.mindspice.outerfieldsserver.enums.SystemType;
import io.mindspice.outerfieldsserver.systems.event.SystemListener;

import java.util.Map;


public class WorldSystem extends SystemListener {
    private final Map<AreaId, AreaEntity> areaMap;

    public WorldSystem(int id, Map<AreaId, AreaEntity> areaMap) {
        super(id, SystemType.WORLD, true);
        this.areaMap = areaMap;
        areaMap.values().forEach(this::onRegisterComponent);
    }
}
