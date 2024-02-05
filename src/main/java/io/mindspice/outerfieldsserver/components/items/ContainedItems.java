package io.mindspice.outerfieldsserver.components.items;

import io.mindspice.mindlib.functional.consumers.BiPredicatedBiConsumer;
import io.mindspice.outerfieldsserver.components.Component;
import io.mindspice.outerfieldsserver.components.logic.PredicateLib;
import io.mindspice.outerfieldsserver.core.Tick;
import io.mindspice.outerfieldsserver.core.singletons.EntityManager;
import io.mindspice.outerfieldsserver.entities.Entity;
import io.mindspice.outerfieldsserver.entities.ItemEntity;
import io.mindspice.outerfieldsserver.enums.ComponentType;
import io.mindspice.outerfieldsserver.enums.TokenType;
import io.mindspice.outerfieldsserver.systems.event.Event;
import io.mindspice.outerfieldsserver.systems.event.EventData;
import io.mindspice.outerfieldsserver.systems.event.EventType;

import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;


public class ContainedItems extends Component<ContainedItems> {
    public Map<String, ItemEntity<?>> containedItems;
    public Consumer<ContainedItems> respawnLogic;
    public int tickTime = 100;

    public ContainedItems(Entity parentEntity, Map<String, ItemEntity<?>> items) {
        super(parentEntity, ComponentType.CONTAINED_ITEMS, List.of());
        this.containedItems = new HashMap<>(items);

        registerListener(EventType.CONTAINER_CONTAINED_ITEMS_QUERY, BiPredicatedBiConsumer.of(
                PredicateLib::isRecEntitySame, ContainedItems::onContainedItemsQuery
        ));
        registerListener(EventType.CONTAINER_ADD_ITEMS, BiPredicatedBiConsumer.of(
                PredicateLib::isRecEntitySame, ContainedItems::onAddItems
        ));
//        registerListener(EventType.CONTAINER_REMOVE_ITEMS, BiPredicatedBiConsumer.of(
//                PredicateLib::isRecEntitySame, ContainedItems::onRemoveItems
//        ));

        setOnTickConsumer(ContainedItems::onTickLogic);
    }

    public ContainedItems withRespawnLogic(Consumer<ContainedItems> respawnLogic, boolean runOnAdd) {
        this.respawnLogic = respawnLogic;
        if (runOnAdd) { respawnLogic.accept(this); }
        return this;
    }

    private void onTickLogic(Tick tick) {
        if (respawnLogic != null) {
            if (--tickTime <= 0) {
                respawnLogic.accept(this);
                tickTime = 100;
            }
        }
    }

    public void onContainedItemsQuery(Event<Integer> event) {
        emitEvent(Event.responseEvent(
                this, event, EventType.CONTAINER_CONTAINED_ITEMS_RESP, Collections.unmodifiableMap(containedItems))
        );
    }

    public void onAddItems(Event<Map<String, Integer>> event) {
        event.data().forEach((key, val) -> {
            ItemEntity<?> existingItem = containedItems.get(key);
            if (existingItem == null) {
                containedItems.put(key, EntityManager.GET().newItemEntity(key, val));
            } else {
                existingItem.setAmount(existingItem.amount() + val);
            }
        });
    }


    public void onRemoveItems(Event<Map<String, Integer>> event) {
        event.data().forEach((key, val) -> {
            ItemEntity<?> existingItem = containedItems.get(key);
            if (existingItem == null || existingItem.amount() < val) {
                // TODO LOG THIS AUTH ERROR and emit to player
                return;
            }
            int newVal = existingItem.amount() - val;
            if (newVal <= 0) {
                containedItems.remove(key);
                emitEvent(Event.destroyEntity(existingItem));
            } else {
                existingItem.setAmount(existingItem.amount() - val);
            }
        });
    }


}
