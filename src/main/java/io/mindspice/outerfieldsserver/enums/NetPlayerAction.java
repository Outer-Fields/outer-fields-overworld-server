package io.mindspice.outerfieldsserver.enums;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;


public enum NetPlayerAction {
    ENTER_LOCATION(1),
    INIT_COMBAT(2),
    PICKUP_ITEMS(3),
    DROP_ITEMS(4),
    PURCHASE_FROM_NPC(5),
    SELL_TO_NPC(6),
    TRADE_WITH_CHARACTER(7),
    ACTIVATE_INV_ITEM(8),
    INTERACT_ITEM_WITH(9),
    OUTFIT_UPDATE(10),
    APPLY_POTION(11),
    WITHDRAW_TO_BLOCKCHAIN(12),
    PLANT_FARM_PLOT(13),
    HARVEST_FARM_PLOT(14),
    LOOT_CONTAINER(15),
    PUT_CONTAINER(16),
    INV_TO_BANKED(17),
    BANKED_TO_INV(18),
    REDEEM_FOR_BC_ITEM(19);

    public final int value;
    private static final TIntObjectMap<NetPlayerAction> lookup = new TIntObjectHashMap<>(NetPlayerAction.values().length);

    static {
        for (var action : NetPlayerAction.values()) {
            lookup.put(action.value, action);
        }
    }

    NetPlayerAction(int value) { this.value = value; }

    public static NetPlayerAction fromValue(int value) {
        return lookup.get(value);
    }
}
