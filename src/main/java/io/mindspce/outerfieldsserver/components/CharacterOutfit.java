package io.mindspce.outerfieldsserver.components;

import io.mindspce.outerfieldsserver.components.logic.PredicateLib;
import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.enums.ClothingItem;
import io.mindspce.outerfieldsserver.enums.ComponentType;
import io.mindspce.outerfieldsserver.networking.NetSerializable;

import io.mindspce.outerfieldsserver.systems.event.Event;
import io.mindspce.outerfieldsserver.systems.event.EventType;
import io.mindspice.mindlib.functional.consumers.BiPredicatedBiConsumer;

import java.nio.ByteBuffer;
import java.util.List;


public class CharacterOutfit extends Component<CharacterOutfit> implements NetSerializable {
    public final ClothingItem[] outfit;

    public CharacterOutfit(Entity parentEntity, ClothingItem[] outfit) {
        super(parentEntity, ComponentType.OUTFIT, List.of(EventType.CHARACTER_OUTFIT_CHANGED));//
        this.outfit = outfit != null ? outfit : new ClothingItem[ClothingItem.Slot.values().length];
        registerListener(EventType.CHARACTER_OUTFIT_UPDATE, BiPredicatedBiConsumer.of(PredicateLib::isRecEntitySame,
                CharacterOutfit::onOutfitUpdate

        ));
    }

    // TODO this is going to need to implement authority
    public void onOutfitUpdate(Event<byte[]> outfitChanges) {
        for (int i = 0; i < outfitChanges.data().length; i += 2) {
            outfit[outfitChanges.data()[i]] = ClothingItem.fromValue(outfitChanges.data()[i + 1]);
        }
        emitEvent(Event.characterOutFitChanges(this, outfit));
    }


    public byte[] asByteArray() {
        return new byte[]{1, 2, 3, 4, 5, 6};
    }

    @Override
    public int byteSize() {
        return 0;
    }

    @Override
    public byte[] getBytes() {
        return new byte[0];
    }

    @Override
    public void addBytesToBuffer(ByteBuffer buffer) {

    }
}
