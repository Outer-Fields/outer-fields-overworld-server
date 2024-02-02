package io.mindspice.outerfieldsserver.entities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.mindspice.mindlib.data.geometry.IVector2;
import io.mindspice.mindlib.data.tuples.Triple;
import io.mindspice.outerfieldsserver.data.LootDropItem;
import io.mindspice.outerfieldsserver.enums.AreaId;
import io.mindspice.outerfieldsserver.enums.EntityType;
import io.mindspice.mindlib.data.tuples.Pair;
import io.mindspice.mindlib.util.JsonUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiFunction;


public class LootEntity extends Entity {
    private final List<LootDropItem> lootChanceAndAmount;
    private volatile BiFunction<LootEntity, PlayerEntity, List<ItemEntity<?>>> lootCalcFunction;

    // TODO add destroy on drop on drop
    public LootEntity(int id, AreaId areaId, List<LootDropItem> lootChanceAndAmount,
            BiFunction<LootEntity, PlayerEntity, List<ItemEntity<?>>> lootCalcFunction) {
        super(id, EntityType.LOOT, areaId);
        this.lootChanceAndAmount = new CopyOnWriteArrayList<>(lootChanceAndAmount);
        this.lootCalcFunction = lootCalcFunction;
    }

    public List<ItemEntity<?>> calculateLootDrop(PlayerEntity player) {
        return lootCalcFunction.apply(this, player);
    }

    public List<ItemEntity<?>> calculateLootDrop(PlayerEntity player,
            BiFunction<LootEntity, PlayerEntity, List<ItemEntity<?>>> calcFunc) {
        return calcFunc.apply(this, player);
    }

    public void setLootCalcFunction(BiFunction<LootEntity, PlayerEntity, List<ItemEntity<?>>> lootCalcFunction) {
        this.lootCalcFunction = lootCalcFunction;
    }

    public List<LootDropItem> lootAndChance() {
        return Collections.unmodifiableList(lootChanceAndAmount);
    }

    public void addItem(ItemEntity<?> item, double chance, int amountLow, int amountHigh) {
        lootChanceAndAmount.add(LootDropItem.of(item, chance, amountLow, amountHigh));
    }

    public void addItem(LootDropItem lootDropItem) {
        this.lootChanceAndAmount.add(lootDropItem);
    }

    public void addItems(List<LootDropItem> lootDropItems) {
        this.lootChanceAndAmount.addAll(lootDropItems);
    }

    public void removeItems(LootDropItem lootDropItem) {
        lootAndChance().remove(lootDropItem);
    }

    public void removeItem(String itemName) {
        lootChanceAndAmount.removeIf(i -> Objects.equals(i.item().itemName(), itemName));
    }

    public void removeItem(ItemEntity<?> item) {
        lootChanceAndAmount.removeIf(i -> i.item().equals(item));
    }

    public String toString() {
        JsonNode node = new JsonUtils.ObjectBuilder()
                .put("entityType", entityType)
                .put("entityId", id)
                .put("name", name)
                .put("lootAndChance", lootChanceAndAmount)
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
