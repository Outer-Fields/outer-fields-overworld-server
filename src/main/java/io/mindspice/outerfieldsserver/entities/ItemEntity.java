package io.mindspice.outerfieldsserver.entities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.mindspice.outerfieldsserver.enums.AreaId;
import io.mindspice.outerfieldsserver.enums.EntityType;
import io.mindspice.mindlib.data.geometry.IVector2;
import io.mindspice.mindlib.util.JsonUtils;


public class ItemEntity extends PositionalEntity {
    private final long key;
    private volatile int ownerPlayerId = -1;

    public ItemEntity(int id, AreaId areaId, IVector2 position, String itemName, long key) {
        super(id, EntityType.ITEM, areaId, position);
        this.name = itemName;
        this.key = key;
    }

    public int ownerPlayerId() { return ownerPlayerId; }

    public void setOwner(int ownerPlayerId) { this.ownerPlayerId = ownerPlayerId; }

    public boolean isOwned() { return ownerPlayerId > 0; }

    public String itemName() { return name; }

    public long key() { return key; }


    public String toString() {
        JsonNode node = new JsonUtils.ObjectBuilder()
                .put("entityType", entityType)
                .put("entityId", id)
                .put("name", name)
                .put("ownerPlayerId", ownerPlayerId)
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
