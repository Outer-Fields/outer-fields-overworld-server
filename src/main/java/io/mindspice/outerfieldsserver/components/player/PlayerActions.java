package io.mindspice.outerfieldsserver.components.player;

import io.mindspice.mindlib.data.tuples.Pair;
import io.mindspice.outerfieldsserver.components.Component;
import io.mindspice.outerfieldsserver.core.networking.incoming.NetPlayerActionMsg;
import io.mindspice.outerfieldsserver.core.singletons.EntityManager;
import io.mindspice.outerfieldsserver.entities.Entity;
import io.mindspice.outerfieldsserver.enums.ClothingItem;
import io.mindspice.outerfieldsserver.enums.ComponentType;
import io.mindspice.outerfieldsserver.enums.NetPlayerAction;
import io.mindspice.outerfieldsserver.enums.SeedType;
import io.mindspice.outerfieldsserver.systems.event.Event;
import io.mindspice.outerfieldsserver.systems.event.EventType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class PlayerActions extends Component<PlayerActions> {
    public PlayerActions(Entity parentEntity) {
        super(parentEntity, ComponentType.PLAYER_ACTIONS, List.of());
        registerListener(EventType.NETWORK_IN_PLAYER_ACTION, PlayerActions::onPlayerActions);
    }

    public void onPlayerActions(Event<Map<NetPlayerAction, List<NetPlayerActionMsg>>> event) {
        for (var entry : event.data().entrySet()) {
            switch (entry.getKey()) {
                case ENTER_LOCATION -> {

                }
                case INIT_COMBAT -> { }
                case PICKUP_ITEMS, LOOT_CONTAINER -> onPickupItems(entry.getValue());
                case DROP_ITEMS -> onDropItems(entry.getValue());
                case PUT_CONTAINER -> onContainerPutItems(entry.getValue());
                case PURCHASE_FROM_NPC -> { }
                case SELL_TO_NPC -> { }
                case TRADE_WITH_CHARACTER -> { }
                case ACTIVATE_INV_ITEM -> { }
                case INTERACT_ITEM_WITH -> { }
                case OUTFIT_UPDATE -> onOutfitUpdate(entry.getValue());
                case APPLY_POTION -> { }
                case WITHDRAW_TO_BLOCKCHAIN -> { }
                case PLANT_FARM_PLOT -> onPlantPlot(entry.getValue());
                case HARVEST_FARM_PLOT -> onHarvestPlot(entry.getValue());
                case INV_TO_BANKED -> onInvToBanked(entry.getValue());
                case BANKED_TO_INV -> onBankedToInv(entry.getValue());
                case REDEEM_FOR_BC_ITEM -> { }
            }
        }
    }

    public void onPickupItems(List<NetPlayerActionMsg> values) {
        Map<Integer, Map<Integer, Integer>> mapped = new HashMap<>(2);
        for (NetPlayerActionMsg val : values) {
            Map<Integer, Integer> itemMap = mapped.computeIfAbsent(val.focusId(), HashMap::new);
            itemMap.put(val.value1(), val.amount());
        }
        mapped.forEach((key, val) -> EntityManager.GET().emitEvent(
                Event.playerPickupItems(this, areaId(), entityId(), Pair.of(key, val))
        ));
    }

    public void onDropItems(List<NetPlayerActionMsg> values) {
        Map<Integer, Integer> mapped = new HashMap<>(values.size());
        for (NetPlayerActionMsg val : values) {
            mapped.put(val.value1(), val.amount());
        }
        emitEvent(Event.playerDropItems(this, areaId(), entityId(), mapped));
    }

    public void onHarvestPlot(List<NetPlayerActionMsg> values) {
        values.forEach(e -> EntityManager.GET().emitEvent(Event.farmHarvestPlot(this, areaId(), e.focusId())));
    }

    public void onPlantPlot(List<NetPlayerActionMsg> values) {
        values.forEach(e -> {
            SeedType seedType = SeedType.fromValue(e.value1());
            if (seedType == null) {
                //TODO log and respond
                return;
            }
            EntityManager.GET().emitEvent(Event.farmPlantPlot(this, areaId(), e.focusId(), seedType));
        });
    }

    public void onContainerPutItems(List<NetPlayerActionMsg> values) {
        Map<Integer, Map<Integer, Integer>> mapped = new HashMap<>(2);
        for (NetPlayerActionMsg val : values) {
            Map<Integer, Integer> itemMap = mapped.computeIfAbsent(val.focusId(), HashMap::new);
            itemMap.put(val.value1(), val.amount());
        }
        mapped.forEach((key, val) -> EntityManager.GET().emitEvent(
                Event.playerPutContainer(this, areaId(), entityId(), Pair.of(key, val))
        ));
    }

    public void onInvToBanked(List<NetPlayerActionMsg> values) {
        Map<Integer, Integer> mapped = new HashMap<>(values.size());
        for (NetPlayerActionMsg val : values) {
            mapped.put(val.value1(), val.amount());
        }
        emitEvent(Event.playerInvToBanked(this, areaId(), entityId(), mapped));
    }

    public void onBankedToInv(List<NetPlayerActionMsg> values) {
        Map<Integer, Integer> mapped = new HashMap<>(values.size());
        for (NetPlayerActionMsg val : values) {
            mapped.put(val.value1(), val.amount());
        }
        emitEvent(Event.playerBankedToInv(this, areaId(), entityId(), mapped));
    }

    public void onOutfitUpdate(List<NetPlayerActionMsg> values) {
        List<ClothingItem> clothingItems = values.stream()
                .map(v -> ClothingItem.fromValue(v.value1()))
                .filter(Objects::nonNull).toList();

        emitEvent(Event.characterOutfitUpdate(this, areaId(), entityId(), clothingItems));
    }


}
