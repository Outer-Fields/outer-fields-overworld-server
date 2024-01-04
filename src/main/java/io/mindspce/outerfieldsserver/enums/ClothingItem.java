package io.mindspce.outerfieldsserver.enums;

public enum ClothingItem {
    ;

    public final byte value;
    public final Slot slot;

    ClothingItem(byte value, Slot slot) {
        this.value = value;
        this.slot = slot;

    }

    public static ClothingItem fromValue(byte value) {
        for (ClothingItem clothingItem : ClothingItem.values()) {
            if (clothingItem.value == value) {
                return clothingItem;
            }
        }
        return null;

    }

    public enum Slot {
        HEAD((byte) 0),
        FACE((byte) 1),
        BODY((byte) 2),
        LEGS((byte) 3),
        HANDS((byte) 4),
        FEET((byte) 5);

        public final byte value;

        Slot(byte value) { this.value = value; }


    }
}
