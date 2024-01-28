package io.mindspice.outerfieldsserver.components.player;

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
    public final ClothingItem[] outfit;

    public CharacterOutfit(Entity parentEntity, ClothingItem[] outfit) {
        super(parentEntity, ComponentType.OUTFIT, List.of(EventType.CHARACTER_OUTFIT_CHANGED));//
        this.outfit = outfit != null ? outfit : new ClothingItem[]{
                ClothingItem.EMPTY, ClothingItem.EMPTY, ClothingItem.EMPTY,
                ClothingItem.EMPTY, ClothingItem.EMPTY, ClothingItem.EMPTY,
        };
        for (int i = 0; i < outfit.length; ++i) { if (outfit[i] == null) { outfit[i] = ClothingItem.EMPTY; } }

        registerListener(EventType.CHARACTER_OUTFIT_UPDATE, BiPredicatedBiConsumer.of(PredicateLib::isRecEntitySame,
                CharacterOutfit::onOutfitUpdate

        ));
    }

    // TODO this is going to need to implement authority
    public void onOutfitUpdate(Event<byte[]> outfitChanges) {
//        for (int i = 0; i < outfitChanges.data().length; i += 2) {
//            outfit[outfitChanges.data()[i]] = ClothingItem.fromValue(outfitChanges.data()[i + 1]);
//        }
//        emitEvent(Event.characterOutFitChanges(this, outfit));
    }

    public byte[] asByteArray() {
        return new byte[]{1, 2, 3, 4, 5, 6};
    }

    public int[] currOutfit() {
        return Arrays.stream(outfit).mapToInt(ClothingItem::value).toArray();
    }


}
