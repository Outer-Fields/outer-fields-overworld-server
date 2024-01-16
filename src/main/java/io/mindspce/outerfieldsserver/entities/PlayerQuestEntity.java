package io.mindspce.outerfieldsserver.entities;

import io.mindspce.outerfieldsserver.enums.AreaId;
import io.mindspce.outerfieldsserver.enums.EntityType;
import io.mindspce.outerfieldsserver.enums.PlayerQuests;


public class PlayerQuestEntity extends Entity {
    private final PlayerQuests quest;
    private final int participatingPlayerId;
    private final int participatingEntityId;

    public PlayerQuestEntity(int id, EntityType entityType, AreaId areaId, PlayerQuests quest,
            int participatingPlayerId, int participatingEntityId) {
        super(id, entityType, areaId);
        this.quest = quest;
        this.participatingPlayerId = participatingPlayerId;
        this.participatingEntityId = participatingEntityId;
    }

    public PlayerQuests quest() {
        return quest;
    }

    public final long key() {
        return quest.key;
    }

    public int participatingPlayerId() {
        return participatingPlayerId;
    }

    public int participatingEntityId() {
        return participatingEntityId;
    }
}
