package io.mindspice.outerfieldsserver.components.player;

import io.mindspice.mindlib.data.geometry.IVector2;
import io.mindspice.mindlib.functional.consumers.BiPredicatedBiConsumer;
import io.mindspice.outerfieldsserver.components.Component;
import io.mindspice.outerfieldsserver.components.items.ContainedItems;
import io.mindspice.outerfieldsserver.components.logic.PredicateLib;
import io.mindspice.outerfieldsserver.core.singletons.EntityManager;
import io.mindspice.outerfieldsserver.entities.ContainerEntity;
import io.mindspice.outerfieldsserver.entities.Entity;
import io.mindspice.outerfieldsserver.entities.ItemEntity;
import io.mindspice.outerfieldsserver.enums.ComponentType;
import io.mindspice.outerfieldsserver.enums.ContainerType;
import io.mindspice.outerfieldsserver.enums.TokenType;
import io.mindspice.outerfieldsserver.systems.event.Event;
import io.mindspice.outerfieldsserver.systems.event.EventData;
import io.mindspice.outerfieldsserver.systems.event.EventType;

import java.util.*;
import java.util.function.Supplier;


public class PlayerItemsAndFunds extends Component<PlayerItemsAndFunds> {
    public Map<TokenType, Integer> inventoryTokens = new EnumMap<>(TokenType.class);
    public Map<TokenType, Integer> bankedTokens = new EnumMap<>(TokenType.class);
    public Map<Long, ItemEntity<?>> inventoryItems = new HashMap<>();
    public Map<Long, ItemEntity<?>> bankedItems = new HashMap<>();
    public final Supplier<IVector2> playerPositionSupplier;

    public PlayerItemsAndFunds(Entity parentEntity, Supplier<IVector2> positionSupplier) {
        super(parentEntity, ComponentType.PLAYER_ITEMS_AND_FUNDS, List.of(EventType.PLAYER_FUNDS_ITEMS_RESP));
        this.playerPositionSupplier = positionSupplier;

        registerListener(EventType.PLAYER_FUNDS_ITEMS_QUERY, BiPredicatedBiConsumer.of(
                PredicateLib::isRecEntitySame, PlayerItemsAndFunds::onFundAndItemQuery
        ));

        // Deposit/Withdraw
        registerListener(EventType.PLAYER_TOKENS_BANKED_TO_INV, BiPredicatedBiConsumer.of(
                PredicateLib::isRecEntitySame, PlayerItemsAndFunds::onTokensBankedToInv
        ));
        registerListener(EventType.PLAYER_TOKENS_INV_TO_BANKED, BiPredicatedBiConsumer.of(
                PredicateLib::isRecEntitySame, PlayerItemsAndFunds::onTokensInvToBanked
        ));
        registerListener(EventType.PLAYER_ITEMS_BANKED_TO_INV, BiPredicatedBiConsumer.of(
                PredicateLib::isRecEntitySame, PlayerItemsAndFunds::onItemsBankedToInv
        ));
        registerListener(EventType.PLAYER_ITEMS_INV_TO_BANKED, BiPredicatedBiConsumer.of(
                PredicateLib::isRecEntitySame, PlayerItemsAndFunds::onItemsInvToBanked
        ));

        //Add
        registerListener(EventType.PLAYER_ADD_BANKED_TOKENS, BiPredicatedBiConsumer.of(
                PredicateLib::isRecEntitySame, PlayerItemsAndFunds::onAddBankedTokens
        ));
        registerListener(EventType.PLAYER_ADD_INV_TOKENS, BiPredicatedBiConsumer.of(
                PredicateLib::isRecEntitySame, PlayerItemsAndFunds::onAddInvTokens
        ));
        registerListener(EventType.PLAYER_ADD_BANKED_ITEMS, BiPredicatedBiConsumer.of(
                PredicateLib::isRecEntitySame, PlayerItemsAndFunds::onAddBankedItems
        ));
        registerListener(EventType.PLAYER_ADD_INV_ITEMS, BiPredicatedBiConsumer.of(
                PredicateLib::isRecEntitySame, PlayerItemsAndFunds::onAddInvItems
        ));

        //Remove
        registerListener(EventType.PLAYER_REMOVE_BANKED_TOKENS, BiPredicatedBiConsumer.of(
                PredicateLib::isRecEntitySame, PlayerItemsAndFunds::onRemoveBankedTokens
        ));
        registerListener(EventType.PLAYER_REMOVE_INV_TOKENS, BiPredicatedBiConsumer.of(
                PredicateLib::isRecEntitySame, PlayerItemsAndFunds::onRemoveInvTokens
        ));
        registerListener(EventType.PLAYER_REMOVE_BANKED_ITEMS, BiPredicatedBiConsumer.of(
                PredicateLib::isRecEntitySame, PlayerItemsAndFunds::onRemoveBankedItems
        ));
        registerListener(EventType.PLAYER_REMOVE_INV_ITEMS, BiPredicatedBiConsumer.of(
                PredicateLib::isRecEntitySame, PlayerItemsAndFunds::onRemoveInvItems
        ));

        // Drop items on death
        registerListener(EventType.CHARACTER_DEATH, BiPredicatedBiConsumer.of(
                (PlayerItemsAndFunds self, Event<EventData.CharacterDeath> event) -> event.data().deadEntityId() == entityId(),
                PlayerItemsAndFunds::onPlayerDeath
        ));

    }

    public void onFundAndItemQuery(Event<Integer> event) {
        var data = new EventData.FundsAndItems(
                Collections.unmodifiableMap(bankedTokens),
                Collections.unmodifiableMap(inventoryTokens),
                Collections.unmodifiableMap(bankedItems),
                Collections.unmodifiableMap(inventoryItems)
        );
        emitEvent(Event.responseEvent(this, event, EventType.PLAYER_FUNDS_ITEMS_RESP, data));
    }

    public void onTokensBankedToInv(Event<Map<TokenType, Integer>> event) {
        boolean invalid = event.data().entrySet().stream().anyMatch(entry -> bankedTokens.get(entry.getKey()) < entry.getValue());
        if (invalid) {
            //TODO log this
            return;
        }
        event.data().forEach((key, val) -> {
            bankedTokens.merge(key, -val, (oldVal, newVal) -> oldVal - newVal);
            inventoryTokens.merge(key, val, Integer::sum);
        });
    }

    public void onTokensInvToBanked(Event<Map<TokenType, Integer>> event) {
        boolean invalid = event.data().entrySet().stream().anyMatch(entry -> inventoryTokens.get(entry.getKey()) < entry.getValue());
        if (invalid) {
            //TODO log this
            return;
        }
        event.data().forEach((key, val) -> {
            inventoryTokens.merge(key, -val, (oldVal, newVal) -> oldVal - newVal);
            bankedTokens.merge(key, val, Integer::sum);
        });
    }

    public void onItemsBankedToInv(Event<Map<Long, ItemEntity<?>>> event) {
        boolean invalid = event.data().entrySet().stream().anyMatch(entry -> !bankedItems.containsKey(entry.getKey()));
        if (invalid) {
            //TODO log this
            return;
        }
        event.data().forEach((key, val) -> {
            bankedItems.remove(key);
            inventoryItems.put(key, val);
        });
    }

    public void onItemsInvToBanked(Event<Map<Long, ItemEntity<?>>> event) {
        boolean invalid = event.data().entrySet().stream().anyMatch(entry -> !inventoryItems.containsKey(entry.getKey()));
        if (invalid) {
            //TODO log this
            return;
        }
        event.data().forEach((key, val) -> {
            inventoryItems.remove(key);
            bankedItems.put(key, val);
        });
    }

    public void onPlayerDeath(Event<EventData.CharacterDeath> event) {
        ContainerEntity container = EntityManager.GET().newContainerEntity(ContainerType.BAG, areaId(), playerPositionSupplier.get(),
                        Map.copyOf(inventoryTokens), Map.copyOf(inventoryItems), true)
                .withDestructionTime(60 * 60);

        inventoryTokens.clear();
        inventoryItems.clear();
    }

    public void onAddBankedTokens(Event<Map<TokenType, Integer>> event) {
        event.data().forEach((key, val) -> bankedTokens.merge(key, val, Integer::sum));
    }

    public void onAddInvTokens(Event<Map<TokenType, Integer>> event) {
        event.data().forEach((key, val) -> inventoryTokens.merge(key, val, Integer::sum));
    }

    public void onAddBankedItems(Event<Map<Long, ItemEntity<?>>> event) {
        event.data().values().forEach(item -> item.setOwner(entityId()));
        bankedItems.putAll(event.data());
    }

    public void onAddInvItems(Event<Map<Long, ItemEntity<?>>> event) {
        event.data().values().forEach(item -> item.setOwner(entityId()));
        inventoryItems.putAll(event.data());
    }

    public void onRemoveBankedTokens(Event<Map<TokenType, Integer>> event) {
        event.data().forEach((key, val) -> bankedTokens.merge(key, -val, (oldVal, newVal) -> oldVal - newVal));
    }

    public void onRemoveInvTokens(Event<Map<TokenType, Integer>> event) {
        event.data().forEach((key, val) -> inventoryTokens.merge(key, -val, (oldVal, newVal) -> oldVal - newVal));
    }

    public void onRemoveBankedItems(Event<Map<Long, ItemEntity<?>>> event) {
        event.data().values().forEach(item -> item.setOwner(-1));
        event.data().forEach((key, val) -> bankedItems.remove(key));
    }

    public void onRemoveInvItems(Event<Map<Long, ItemEntity<?>>> event) {
        event.data().values().forEach(item -> item.setOwner(-1));
        event.data().forEach((key, val) -> inventoryItems.remove(key));
    }


}
