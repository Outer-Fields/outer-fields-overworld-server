package io.mindspice.outerfieldsserver.components.player;

import io.mindspice.mindlib.data.collections.lists.primative.IntList;
import io.mindspice.mindlib.data.geometry.IRect2;
import io.mindspice.mindlib.data.geometry.IVector2;
import io.mindspice.mindlib.data.tuples.Pair;
import io.mindspice.mindlib.functional.consumers.BiPredicatedBiConsumer;
import io.mindspice.outerfieldsserver.components.Component;
import io.mindspice.outerfieldsserver.components.items.ContainedItems;
import io.mindspice.outerfieldsserver.components.logic.PredicateLib;
import io.mindspice.outerfieldsserver.components.world.ActiveEntities;
import io.mindspice.outerfieldsserver.components.world.EntityGrid;
import io.mindspice.outerfieldsserver.core.WorldSettings;
import io.mindspice.outerfieldsserver.core.singletons.EntityManager;
import io.mindspice.outerfieldsserver.entities.ContainerEntity;
import io.mindspice.outerfieldsserver.entities.Entity;
import io.mindspice.outerfieldsserver.entities.ItemEntity;
import io.mindspice.outerfieldsserver.enums.*;
import io.mindspice.outerfieldsserver.systems.event.Event;
import io.mindspice.outerfieldsserver.systems.event.EventData;
import io.mindspice.outerfieldsserver.systems.event.EventType;
import io.mindspice.outerfieldsserver.systems.event.TimedEvent;
import io.mindspice.outerfieldsserver.util.Utility;
import jdk.jshell.execution.Util;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;


public class PlayerItemsAndFunds extends Component<PlayerItemsAndFunds> {
    public Map<String, ItemEntity<?>> inventoryItems = new HashMap<>();
    public Map<String, ItemEntity<?>> bankedItems = new HashMap<>();
    public final Supplier<IVector2> playerPositionSupplier;

    public PlayerItemsAndFunds(Entity parentEntity, Supplier<IVector2> positionSupplier) {
        super(parentEntity, ComponentType.PLAYER_ITEMS_AND_FUNDS, List.of(EventType.PLAYER_FUNDS_ITEMS_RESP));
        this.playerPositionSupplier = positionSupplier;

        registerListener(EventType.PLAYER_FUNDS_ITEMS_QUERY, BiPredicatedBiConsumer.of(
                PredicateLib::isRecEntitySame, PlayerItemsAndFunds::onFundAndItemQuery
        ));

        // Deposit/Withdraw
        registerListener(EventType.PLAYER_ITEMS_BANKED_TO_INV, BiPredicatedBiConsumer.of(
                PredicateLib::isRecEntitySame, PlayerItemsAndFunds::onItemsBankedToInv
        ));
        registerListener(EventType.PLAYER_ITEMS_INV_TO_BANKED, BiPredicatedBiConsumer.of(
                PredicateLib::isRecEntitySame, PlayerItemsAndFunds::onItemsInvToBanked
        ));

        //Add
        registerListener(EventType.PLAYER_ADD_BANKED_ITEMS, BiPredicatedBiConsumer.of(
                PredicateLib::isRecEntitySame, PlayerItemsAndFunds::onAddBankedItems
        ));
        registerListener(EventType.PLAYER_ADD_INV_ITEMS, BiPredicatedBiConsumer.of(
                PredicateLib::isRecEntitySame, PlayerItemsAndFunds::onAddInvItems
        ));

        //Remove
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

        // World interactions

        registerListener(EventType.PLAYER_PICKUP_ITEMS, BiPredicatedBiConsumer.of(
                PredicateLib::isRecEntitySame, PlayerItemsAndFunds::onPickupItems
        ));

        registerListener(EventType.PLAYER_DROP_ITEMS, BiPredicatedBiConsumer.of(
                PredicateLib::isRecEntitySame, PlayerItemsAndFunds::onDropItems
        ));

        registerListener(EventType.CONTAINER_ADD_ITEMS, BiPredicatedBiConsumer.of(
                PredicateLib::isRecEntitySame, PlayerItemsAndFunds::onContainerPutItems
        ));

    }

    public void onFundAndItemQuery(Event<Integer> event) {
        var data = new EventData.FundsAndItems(
                Collections.unmodifiableMap(bankedItems),
                Collections.unmodifiableMap(inventoryItems)
        );
        emitEvent(Event.responseEvent(this, event, EventType.PLAYER_FUNDS_ITEMS_RESP, data));
    }

    public void onItemsBankedToInv(Event<Map<Integer, Integer>> event) {

        List<ItemEntity<?>> itemEntities = Utility.mapEntities(
                EntityManager.GET().multipleEntitiesById(event.data().keySet()),
                EntityType.ITEM
        );
        Map<String, Integer> mappedEntities = itemEntities.stream().collect(Collectors.groupingBy(
                ItemEntity::key,
                Collectors.summingInt(ItemEntity::amount)
        ));


        boolean invalid = mappedEntities.entrySet().stream().anyMatch(entry -> {
            ItemEntity<?> item = bankedItems.get(entry.getKey());
            return item == null || item.amount() < entry.getValue();
        });

        if (invalid) {
            //TODO log this and send auth
            return;
        }
        mappedEntities.forEach((key, amount) -> {
            inventoryItems.compute(key, (k, existingItem) -> {
                if (existingItem == null) {
                    return EntityManager.GET().newItemEntity(key, amount);
                } else {
                    existingItem.setAmount(existingItem.amount() + amount);
                    return existingItem;
                }
            });
        });

        mappedEntities.forEach((key, amount) -> {
            bankedItems.computeIfPresent(key, (k, existingItem) -> {
                int newAmount = existingItem.amount() - amount;
                if (newAmount <= 0) {
                    emitEvent(Event.destroyEntity(existingItem));
                    return null;
                } else {
                    existingItem.setAmount(newAmount);
                    return existingItem;
                }
            });
        });

    }

    public void onItemsInvToBanked(Event<Map<Integer, Integer>> event) {

        List<ItemEntity<?>> itemEntities = Utility.mapEntities(
                EntityManager.GET().multipleEntitiesById(event.data().keySet()),
                EntityType.ITEM
        );
        Map<String, Integer> mappedEntities = itemEntities.stream().collect(Collectors.groupingBy(
                ItemEntity::key,
                Collectors.summingInt(ItemEntity::amount)
        ));

        boolean invalid = mappedEntities.entrySet().stream().anyMatch(entry -> {
            ItemEntity<?> item = inventoryItems.get(entry.getKey());
            return item == null || item.amount() < entry.getValue();
        });

        if (invalid) {
            //TODO log this and send auth
            return;
        }
        mappedEntities.forEach((key, amount) -> {
            bankedItems.compute(key, (k, existingItem) -> {
                if (existingItem == null) {
                    return EntityManager.GET().newItemEntity(key, amount);
                } else {
                    existingItem.setAmount(existingItem.amount() + amount);
                    return existingItem;
                }
            });
        });

        mappedEntities.forEach((key, amount) -> {
            inventoryItems.computeIfPresent(key, (k, existingItem) -> {
                int newAmount = existingItem.amount() - amount;
                if (newAmount <= 0) {
                    emitEvent(Event.destroyEntity(existingItem));
                    return null;
                } else {
                    existingItem.setAmount(newAmount);
                    return existingItem;
                }
            });
        });

    }

    public void onPlayerDeath(Event<EventData.CharacterDeath> event) {
        ContainerEntity container = EntityManager.GET().newContainerEntity(ContainerType.BAG, areaId(),
                playerPositionSupplier.get(), Map.copyOf(inventoryItems), true);

        EntityManager.GET().submitTimedEvent(TimedEvent.ofOffsetMinutes(
                60 * 6, Event.destroyEntity(container)
        ));

        inventoryItems.clear();
    }

    public void onAddBankedItems(Event<Map<String, Integer>> event) {
        event.data().forEach((key, amount) -> {
            bankedItems.compute(key, (k, existingItem) -> {
                if (existingItem == null) {
                    return EntityManager.GET().newItemEntity(key, amount);
                } else {
                    existingItem.setAmount(existingItem.amount() + amount);
                    return existingItem;
                }
            });
        });
    }

    public void onAddInvItems(Event<Map<String, Integer>> event) {
        event.data().forEach((key, amount) -> {
            inventoryItems.compute(key, (k, existingItem) -> {
                if (existingItem == null) {
                    return EntityManager.GET().newItemEntity(key, amount);
                } else {
                    existingItem.setAmount(existingItem.amount() + amount);
                    return existingItem;
                }
            });
        });
    }

    public void onRemoveBankedItems(Event<Map<String, Integer>> event) {
        event.data().forEach((key, amount) -> {
            bankedItems.computeIfPresent(key, (k, existingItem) -> {
                int newAmount = existingItem.amount() - amount;
                if (newAmount <= 0) {
                    emitEvent(Event.destroyEntity(existingItem));
                    return null;
                } else {
                    existingItem.setAmount(newAmount);
                    return existingItem;
                }
            });
        });
    }

    public void onRemoveInvItems(Event<Map<String, Integer>> event) {
        event.data().forEach((key, amount) -> {
            inventoryItems.computeIfPresent(key, (k, existingItem) -> {
                int newAmount = existingItem.amount() - amount;
                if (newAmount <= 0) {
                    emitEvent(Event.destroyEntity(existingItem));
                    return null;
                } else {
                    existingItem.setAmount(newAmount);
                    return existingItem;
                }
            });
        });
    }

    public void onDropItems(Event<Map<Integer, Integer>> event) {
        Map<String, ItemEntity<?>> dropItems = new HashMap<>(event.data().size());
        event.data().forEach((key, val) -> {
            ItemEntity<?> item = EntityType.ITEM.castOrNull(EntityManager.GET().entityById(key));
            if (item == null) {
                // TODO log and auth response
                return;
            }
            ItemEntity<?> invItem = inventoryItems.get(item.key());
            if (invItem == null || invItem.entityId() != key) {
                // TODO log and auth response
                return;
            }
            if (invItem.amount() < val) {
                // TODO and auth response
                return;
            }
            dropItems.put(invItem.key(), EntityManager.GET().newItemEntity(invItem.key(), val));
            int newAmount = invItem.amount() - val;
            if (newAmount <= 0) {
                inventoryItems.remove(invItem.key());
            } else {
                invItem.setAmount(invItem.amount() - val);
            }
        });
        final IVector2 position = IVector2.of(playerPositionSupplier.get()); // Need to get and clone this for other thread

        emitEvent(Event.directEntityCallback(areaId().entityId, ComponentType.ENTITY_GRID, (EntityGrid grid) -> {
            IRect2 searchRect = IRect2.fromCenter(position, WorldSettings.GET().interactionRadius());
            var existing = grid.entityGrid.query(searchRect)
                    .stream()
                    .filter(e -> e.item().entityType() == EntityType.CONTAINER)
                    .findFirst().orElse(null);

            if (existing != null) { // remap and add to existing if found
                Map<String, Integer> updateItems = dropItems.entrySet()
                        .stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().amount()));
                emitEvent(Event.containerAddItems(this, areaId(), existing.item().entityId(), updateItems));
            } else {// else create new and broadcast = true
                ContainerEntity cont = EntityManager.GET().newContainerEntity(ContainerType.BAG, areaId(), position, dropItems, true);
                EntityManager.GET().submitTimedEvent(TimedEvent.ofOffsetHours(6, Event.destroyEntity(cont)));
            }
        }));
    }

    public void onPickupItems(Event<Pair<Integer, Map<Integer, Integer>>> event) {
        int containerId = event.data().first();
        IVector2 position = playerPositionSupplier.get();
        IRect2 areaRect = IRect2.fromCenter(position, WorldSettings.GET().interactionRadius());

        ContainerEntity container = EntityType.CONTAINER.castOrNull(EntityManager.GET().entityById(containerId));
        if (container == null) {
            //TODO log and auth response
            return;
        }
        if (!areaRect.contains(container.position())) {
            // TODO log and auth
            return;
        }

        List<ItemEntity<?>> itemEntities = Utility.mapEntities(
                EntityManager.GET().multipleEntitiesById(event.data().second().keySet()),
                EntityType.ITEM
        );
        Map<String, Integer> mappedEntities = itemEntities.stream().collect(Collectors.groupingBy(
                ItemEntity::key,
                Collectors.summingInt(ItemEntity::amount)
        ));

        emitEvent(Event.directEntityCallback(containerId, ComponentType.CONTAINED_ITEMS, (ContainedItems contItems) -> {
            mappedEntities.forEach((key, value) -> {
                ItemEntity<?> item = contItems.containedItems.get(key);
                if (item == null || item.amount() < value) {
                    mappedEntities.remove(key);
                    // TODO log this
                }
            });
            emitEvent(Event.playerAddInvItems(contItems, areaId(), entityId(), mappedEntities));
            contItems.onRemoveItems(Event.containerRemoveItems(contItems, areaId(), contItems.entityId(), mappedEntities));
        }));
    }

    public void onContainerPutItems(Event<Pair<Integer, Map<Integer, Integer>>> event) {
        int containerId = event.data().first();
        IVector2 position = playerPositionSupplier.get();
        IRect2 areaRect = IRect2.fromCenter(position, WorldSettings.GET().interactionRadius());

        ContainerEntity container = EntityType.CONTAINER.castOrNull(EntityManager.GET().entityById(containerId));
        if (container == null) {
            //TODO log and auth response
            return;
        }
        if (!areaRect.contains(container.position())) {
            // TODO log and auth
            return;
        }
        List<ItemEntity<?>> itemEntities = Utility.mapEntities(
                EntityManager.GET().multipleEntitiesById(event.data().second().keySet()),
                EntityType.ITEM
        );
        Map<String, Integer> mappedEntities = itemEntities.stream().collect(Collectors.groupingBy(
                ItemEntity::key,
                Collectors.summingInt(ItemEntity::amount)
        ));

        boolean valid = mappedEntities.entrySet().stream().allMatch(entry -> {
            ItemEntity<?> item = inventoryItems.get(entry.getKey());
            return (item != null && item.amount() >= entry.getValue());
        });

        onRemoveInvItems(Event.containerRemoveItems(this, areaId(), entityId(), mappedEntities));
        emitEvent(Event.containerAddItems(this, areaId(), containerId, mappedEntities));
    }


}
