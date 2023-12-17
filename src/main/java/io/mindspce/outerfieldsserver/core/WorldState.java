package io.mindspce.outerfieldsserver.core;

import io.mindspce.outerfieldsserver.area.AreaInstance;
import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.entities.player.PlayerSession;
import io.mindspce.outerfieldsserver.entities.player.PlayerState;
import io.mindspce.outerfieldsserver.enums.AreaId;
import io.mindspice.mindlib.data.cache.ConcurrentIndexCache;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class WorldState {

    private final Map<AreaId, AreaInstance> areaTable;
    private volatile Collection<AreaInstance> areaList;

    public WorldState(Map<AreaId, AreaInstance> areaTable) {
        this.areaTable = areaTable;
        areaList = areaTable.values();
    }

    public Map<AreaId, AreaInstance> getAreaTable() {
        return areaTable;
    }

    public void addArea(AreaId areaId, AreaInstance area) {
        areaTable.put(areaId, area);
        areaList = List.copyOf(areaTable.values());
    }

    public Collection<AreaInstance> getAreaList() {
        return areaList;
    }


}
