package io.mindspce.outerfieldsserver.entities;

import io.mindspce.outerfieldsserver.enums.AreaId;
import io.mindspce.outerfieldsserver.enums.EntityType;
import io.mindspce.outerfieldsserver.enums.PlayerQuests;


public class PlayerQuestEntity extends Entity {
    private final PlayerQuests quest;
    private final int participatingPlayerId;

    public PlayerQuestEntity(int id, PlayerQuests quest, int participatingPlayerId) {
        super(id, EntityType.QUEST_PLAYER, AreaId.NONE);
        this.quest = quest;
        this.participatingPlayerId = participatingPlayerId;
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

}
