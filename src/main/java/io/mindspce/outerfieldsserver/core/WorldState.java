package io.mindspce.outerfieldsserver.core;

import io.mindspce.outerfieldsserver.area.AreaEntity;
import io.mindspce.outerfieldsserver.enums.AreaId;

import java.util.*;


public class WorldState {
    private static final WorldState INSTANCE = new WorldState();
    private Map<AreaId, AreaEntity> areaTable;
    private volatile Collection<AreaEntity> areaList;

    public WorldState() {

    }

    public void init(Map<AreaId, AreaEntity> areaTable) {
        this.areaTable = areaTable;
        areaList = areaTable.values();
    }

    public Map<AreaId, AreaEntity> getAreaTable() {
        return areaTable;
    }

    public void addArea(AreaId areaId, AreaEntity area) {
        areaTable.put(areaId, area);
        areaList = List.copyOf(areaTable.values());
    }

    public Collection<AreaEntity> getAreaList() {
        return areaList;
    }

    public static WorldState GET() {
        return INSTANCE;
    }
}
