package io.mindspice.outerfieldsserver.entities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.mindspice.outerfieldsserver.enums.*;
import io.mindspice.outerfieldsserver.enums.*;
import io.mindspice.outerfieldsserver.factory.ComponentFactory;
import io.mindspice.mindlib.data.geometry.IVector2;
import io.mindspice.mindlib.util.JsonUtils;

import java.util.List;


public class NonPlayerEntity extends CharacterEntity {
    private final long key;


    public NonPlayerEntity(int entityId, long key, String characterName, List<EntityState> initStates,
            ClothingItem[] initOutfit, AreaId currArea, IVector2 currPosition, IVector2 viewRectSize) {
        super(entityId, EntityType.NON_PLAYER, currArea, currPosition);
        super.name = characterName;
        this.key = key;
        factions.add(FactionType.NON_PLAYER);

        ComponentFactory.CompSystem.attachBaseNPCComponents(
                this, currPosition, initStates, initOutfit, viewRectSize);
    }


    public long getKey() { return key; }


    public String toString() {
        JsonNode node = new JsonUtils.ObjectBuilder()
                .put("entityType", entityType)
                .put("entityId", id)
                .put("name", name)
                .put("key", key)
                .put("areaId", areaId)
                .put("chunkIndex", chunkIndex)
                .put("attachedComponents", getAttachedComponentTypes())
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
