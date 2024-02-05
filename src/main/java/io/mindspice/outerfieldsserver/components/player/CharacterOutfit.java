package io.mindspice.outerfieldsserver.components.player;

import io.mindspice.mindlib.data.tuples.Pair;
import io.mindspice.outerfieldsserver.components.Component;
import io.mindspice.outerfieldsserver.components.logic.PredicateLib;
import io.mindspice.outerfieldsserver.entities.Entity;
import io.mindspice.outerfieldsserver.enums.ClothingItem;
import io.mindspice.outerfieldsserver.enums.ComponentType;

import io.mindspice.outerfieldsserver.systems.event.Event;
import io.mindspice.outerfieldsserver.systems.event.EventType;
import io.mindspice.mindlib.functional.consumers.BiPredicatedBiConsumer;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;


public class CharacterOutfit extends Component<CharacterOutfit> {
    public final ClothingItem[] outfit = new ClothingItem[6];

    public CharacterOutfit(Entity parentEntity, List<ClothingItem> initOutfit) {
        super(parentEntity, ComponentType.OUTFIT, List.of(EventType.CHARACTER_OUTFIT_CHANGED));//

        initOutfit.forEach(item -> outfit[item.slot.value] = item);

        registerListener(EventType.CHARACTER_OUTFIT_UPDATE, BiPredicatedBiConsumer.of(
                PredicateLib::isRecEntitySame, CharacterOutfit::onOutfitUpdate
        ));
        padNull();
    }

    public void padNull() {
        for (int i = 0; i < outfit.length; i++) {
            if (outfit[i] == null) {
                outfit[i] = ClothingItem.EMPTY;
            }
        }
    }

    public void onOutfitUpdate(Event<List<ClothingItem>> event) {
        PlayerItemsAndFunds playerItems = ComponentType.PLAYER_ITEMS_AND_FUNDS.castOrNull(
                parentEntity.getComponent(ComponentType.PLAYER_ITEMS_AND_FUNDS)
        );
        if (playerItems == null) {
            // TODO log this and send response, this should never happen
            return;
        }
        List<ClothingItem> validItems = event.data().stream()
                .filter(cItem -> playerItems.inventoryItems.values()
                        .stream().anyMatch(invItem -> invItem.item().equals(cItem))
                ).toList();

        validItems.forEach(item -> outfit[item.slot.value] = item);
    }

    public int[] currOutfit() {
        return Arrays.stream(outfit).mapToInt(ClothingItem::value).toArray();
    }


}
