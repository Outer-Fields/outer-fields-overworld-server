package io.mindspice.outerfieldsserver.entities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.mindspice.outerfieldsserver.enums.AreaId;
import io.mindspice.outerfieldsserver.enums.EntityType;
import io.mindspice.outerfieldsserver.enums.PlayerQuests;
import io.mindspice.mindlib.util.JsonUtils;


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

    public String toString() {
        JsonNode node = new JsonUtils.ObjectBuilder()
                .put("entityType", entityType)
                .put("entityId", id)
                .put("name", name)
                .put("participatingPlayerId", participatingPlayerId)
                .put("quest", quest)
                .put("areaId", areaId)
                .put("chunkIndex", chunkIndex)
                .put("attachComponents", getAttachedComponentTypes())
                .put("listeningFor", listeningForTypes())
                .put("systemRegistry", systemRegistry != null ?  systemRegistry.systemType() : null)
                .buildNode();
        try {
            return JsonUtils.writePretty(node);
        } catch (JsonProcessingException e) {
            return "Error serializing to string";
        }
    }

}
