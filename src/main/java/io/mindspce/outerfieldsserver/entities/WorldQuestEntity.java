package io.mindspce.outerfieldsserver.entities;

import io.mindspce.outerfieldsserver.enums.AreaId;
import io.mindspce.outerfieldsserver.enums.EntityType;
import io.mindspce.outerfieldsserver.enums.PlayerQuests;
import io.mindspce.outerfieldsserver.enums.WorldQuests;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class WorldQuestEntity extends Entity {
    private final WorldQuests quest;
    private final List<Integer> involvedPlayers = new CopyOnWriteArrayList<>();

    public WorldQuestEntity(int id, EntityType entityType, AreaId areaId, WorldQuests quest) {
        super(id, entityType, areaId);
        this.quest = quest;
    }

    public WorldQuests quest() {
        return quest;
    }

    public final long key() {
        return quest.key;
    }

    public List<Integer> involvedPlayers() {
        return involvedPlayers;
    }

    public void addInvolvedPlayer(int playerId) {
        involvedPlayers.add(playerId);
    }

}
