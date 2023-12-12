package io.mindspce.outerfieldsserver.core;

import io.mindspce.outerfieldsserver.area.AreaInstance;
import io.mindspce.outerfieldsserver.entities.player.PlayerSession;
import io.mindspce.outerfieldsserver.enums.AreaId;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class WorldState {
    private final Map<Integer, PlayerSession> playerTable;
    private final Map<AreaId, AreaInstance> areaTable;

    public WorldState(Map<Integer, PlayerSession> playerTable, Map<AreaId, AreaInstance> areaTable) {
        this.playerTable = playerTable;
        this.areaTable = areaTable;
    }

    public Map<Integer, PlayerSession> getPlayerTable() {
        return playerTable;
    }

    public Map<AreaId, AreaInstance> getAreaTable() {
        return areaTable;
    }
}
