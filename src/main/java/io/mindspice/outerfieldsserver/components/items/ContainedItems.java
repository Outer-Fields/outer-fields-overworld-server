package io.mindspice.outerfieldsserver.components.items;

import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;
import io.mindspice.mindlib.functional.consumers.BiPredicatedBiConsumer;
import io.mindspice.outerfieldsserver.components.Component;
import io.mindspice.outerfieldsserver.components.logic.PredicateLib;
import io.mindspice.outerfieldsserver.core.Tick;
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
    public Map<TokenType, Integer> containedTokens;
    public Map<Long, ItemEntity<?>> containedItems;
    public Consumer<ContainedItems> respawnLogic;
    public int tickTime = 100;


    public ContainedItems(Entity parentEntity, Map<TokenType, Integer> tokens, Map<Long, ItemEntity<?>> items) {
        super(parentEntity, ComponentType.CONTAINED_ITEMS, List.of());
        this.containedTokens = new EnumMap<>(tokens);
        this.containedItems = new HashMap<>(items);

        registerListener(EventType.CONTAINER_CONTAINED_ITEMS_QUERY, BiPredicatedBiConsumer.of(
                PredicateLib::isRecEntitySame, ContainedItems::onContainedItemsQuery
        ));
        registerListener(EventType.CONTAINER_ADD_ITEMS, BiPredicatedBiConsumer.of(
                PredicateLib::isRecEntitySame, ContainedItems::onAddItems
        ));
        registerListener(EventType.CONTAINER_REMOVE_ITEMS, BiPredicatedBiConsumer.of(
                PredicateLib::isRecEntitySame, ContainedItems::onRemoveItems
        ));

        setOnTickConsumer(ContainedItems::onTickLogic);
    }

    public ContainedItems withRespawnLogic(Consumer<ContainedItems> respawnLogic, boolean runOnAdd) {
        this.respawnLogic = respawnLogic;
        if (runOnAdd) { respawnLogic.accept(this); }
        return this;
    }



    private void onTickLogic(Tick tick) {
        if (--tickTime <= 0) {
            if (respawnLogic != null) {
                respawnLogic.accept(this);
            }
        }
    }

    public void onContainedItemsQuery(Event<Integer> event) {
        var data = new EventData.TokensAndItems(
                containedTokens == null ? Map.of() : Collections.unmodifiableMap(containedTokens),
                containedItems == null ? Map.of() : Collections.unmodifiableMap(containedItems)
        );
        emitEvent(Event.responseEvent(this, event, EventType.CONTAINER_CONTAINED_ITEMS_RESP, data));
    }

    public void onAddItems(Event<EventData.TokensAndItems> event) {
        if (!event.data().tokens().isEmpty()) {
            if (containedTokens == null) { containedTokens = new EnumMap<>(TokenType.class); }
            event.data().tokens().forEach((key, val) ->
                    containedTokens.merge(key, val, Integer::sum)
            );
        }
        if (!event.data().items().isEmpty()) {
            if (containedItems == null) { containedItems = new HashMap<>(); }
            containedItems.putAll(event.data().items());
        }
    }

    public void onRemoveItems(Event<EventData.TokensAndItems> event) {
        if (containedTokens != null && !event.data().tokens().isEmpty()) {
            event.data().tokens().forEach((key, val) -> {
                if (containedTokens.containsKey(key)) {
                    containedTokens.merge(key, -val, (oldVal, newVal) -> Math.min(oldVal - newVal, 0));
                }
            });
        }
        if (containedItems != null && !event.data().items().isEmpty()) {
            event.data().items().forEach((key, val) -> containedItems.remove(key));
        }
    }


}
