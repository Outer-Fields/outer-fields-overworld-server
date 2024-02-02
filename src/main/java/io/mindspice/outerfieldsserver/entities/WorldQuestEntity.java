package io.mindspice.outerfieldsserver.entities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.mindspice.outerfieldsserver.enums.AreaId;
import io.mindspice.outerfieldsserver.enums.EntityType;
import io.mindspice.outerfieldsserver.enums.WorldQuests;
import io.mindspice.mindlib.util.JsonUtils;

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

    public String toString() {
        JsonNode node = new JsonUtils.ObjectBuilder()
                .put("entityType", entityType)
                .put("entityId", id)
                .put("name", name)
                .put("involvedPlayers", involvedPlayers)
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