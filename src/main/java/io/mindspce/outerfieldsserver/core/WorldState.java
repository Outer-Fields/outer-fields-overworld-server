package io.mindspce.outerfieldsserver.core;

import io.mindspce.outerfieldsserver.area.AreaState;
import io.mindspce.outerfieldsserver.enums.AreaId;

import java.util.*;


public class WorldState {
    private static final WorldState INSTANCE = new WorldState();
    private Map<AreaId, AreaState> areaTable;
    private volatile Collection<AreaState> areaList;

    public WorldState() {

    }

    public void init(Map<AreaId, AreaState> areaTable) {
        this.areaTable = areaTable;
        areaList = areaTable.values();
    }

    public Map<AreaId, AreaState> getAreaTable() {
        return areaTable;
    }

    public void addArea(AreaId areaId, AreaState area) {
        areaTable.put(areaId, area);
        areaList = List.copyOf(areaTable.values());
    }

    public Collection<AreaState> getAreaList() {
        return areaList;
    }

    public static WorldState GET() {
        return INSTANCE;
    }
}
