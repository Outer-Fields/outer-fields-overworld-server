package io.mindspce.outerfieldsserver.entities;

import io.mindspce.outerfieldsserver.enums.AreaId;
import io.mindspce.outerfieldsserver.enums.EntityType;
import io.mindspice.mindlib.data.geometry.IVector2;
import io.mindspice.mindlib.data.tuples.Pair;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;


public class LootEntity extends PositionalEntity {
    private final List<Pair<String, Double>> lootAndChance;
    private volatile Pair<Integer, Integer> lootAmount;
    private volatile Function<PlayerEntity, List<String>> lootCalcFunction;

    public LootEntity(int id, AreaId areaId, IVector2 position, List<Pair<String, Double>> lootAndChance,
            Pair<Integer, Integer> lootAmount, Function<PlayerEntity, List<String>> lootCalcFunction) {
        super(id, EntityType.LOOT, areaId, position);
        this.lootAndChance = new CopyOnWriteArrayList<>(lootAndChance);
        this.lootAmount = lootAmount;
        this.lootCalcFunction = lootCalcFunction;
    }

    public List<String> calculateLootDrop(PlayerEntity entity) { return lootCalcFunction.apply(entity); }

    public List<Pair<String, Double>> lootAndChance() { return Collections.unmodifiableList(lootAndChance); }

    public Pair<Integer, Integer> lootAmount() { return lootAmount; }

    public void addItem(String itemName, double chance) { lootAndChance.add(Pair.of(itemName, chance)); }

    public void addItem(Pair<String, Double> itemAndChance) { lootAndChance.add(itemAndChance); }

    public void addItems(List<Pair<String, Double>> itemsAndChances) { lootAndChance.addAll(itemsAndChances); }

    public void removeItem(String itemName) { lootAndChance.removeIf(i -> i.first().equals(itemName)); }

    public void removeItems(List<String> itemNames) { itemNames.forEach(i -> lootAndChance.removeIf(existing -> existing.first().equals(i))); }

    public void setLootAmounts(int min, int max) { this.lootAmount = Pair.of(min, max); }

    public void setLootCalcFunction(Function<PlayerEntity, List<String>> lootCalcFunction) { this.lootCalcFunction = lootCalcFunction; }


}
