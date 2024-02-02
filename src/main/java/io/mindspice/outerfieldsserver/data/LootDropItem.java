package io.mindspice.outerfieldsserver.data;

import io.mindspice.outerfieldsserver.entities.ItemEntity;


public record LootDropItem(
        ItemEntity<?> item,
        double chance,
        int amountLow,
        int amountHigh
) {
    public static LootDropItem of(ItemEntity<?> item, double change, int amountLow, int amountHigh) {
        return new LootDropItem(item, change, amountLow, amountHigh);
    }
}