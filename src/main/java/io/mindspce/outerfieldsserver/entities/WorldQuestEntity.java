package io.mindspce.outerfieldsserver.entities;

import io.mindspce.outerfieldsserver.enums.AreaId;
import io.mindspce.outerfieldsserver.enums.EntityType;
import io.mindspce.outerfieldsserver.enums.PlayerQuests;
import io.mindspce.outerfieldsserver.enums.WorldQuests;


public class WorldQuestEntity extends Entity {
    private final WorldQuests quest;


    public WorldQuestEntity(int id, EntityType entityType, AreaId areaId, WorldQuests quest,
            int participatingPlayerId, int participatingEntityId) {
        super(id, entityType, areaId);
        this.quest = quest;
    }

    public WorldQuests quest() {
        return quest;
    }

    public final long key() {
        return quest.key;
    }

}
