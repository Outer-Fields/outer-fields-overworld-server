package io.mindspice.outerfieldsserver.entities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.mindspice.outerfieldsserver.enums.AreaId;
import io.mindspice.outerfieldsserver.enums.EntityType;
import io.mindspice.mindlib.data.geometry.IVector2;
import io.mindspice.mindlib.util.JsonUtils;
import io.mindspice.outerfieldsserver.enums.ItemType;
import io.mindspice.outerfieldsserver.enums.TokenType;

import java.util.Map;


public class ItemEntity<T> extends PositionalEntity {
    private ItemType itemType;
    private final long key;
    private volatile int ownerPlayerId = -1;
    private final T item;
    private final int amount;

    public ItemEntity(int id, AreaId areaId, IVector2 position, ItemType itemType, String itemName, long key, T item, int amount) {
        super(id, EntityType.ITEM, areaId, position);
        this.name = itemName;
        this.key = key;
        this.item = item;
        this.itemType = itemType;
        this.amount = amount;
        if (!itemType.validate(item)) {
            throw new IllegalStateException("Failed to validate itemType: " + itemType + " with object: " + item.getClass());
        }
    }

    public int ownerPlayerId() { return ownerPlayerId; }

    public void setOwner(int ownerPlayerId) { this.ownerPlayerId = ownerPlayerId; }

    public boolean isOwned() { return ownerPlayerId > 0; }

    public String itemName() { return name; }

    public T item() { return item; }

    public long key() { return key; }

    public ItemType itemType() { return itemType; }

    public int amount() { return amount; }

    public Map.Entry<TokenType, Integer> getAsTokenEntry() {
        if (itemType != ItemType.TOKEN) { return null; }
        return (Map.entry((TokenType) item, amount));
    }

    public Map.Entry<Long, ItemEntity<?>> getAsItemEntry(){
        if (itemType == ItemType.TOKEN) {return null;}
        return (Map.entry(key, this));
    }

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
                .put("systemRegistry", systemRegistry != null ? systemRegistry.systemType() : null)
                .buildNode();
        try {
            return JsonUtils.writePretty(node);
        } catch (JsonProcessingException e) {
            return "Error serializing to string";
        }
    }

}
