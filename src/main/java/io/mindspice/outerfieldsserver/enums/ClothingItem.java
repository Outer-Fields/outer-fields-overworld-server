package io.mindspice.outerfieldsserver.enums;

public enum ClothingItem {
    EMPTY(Slot.ALL, 0);

    public final Slot slot;
    public final int value;

    ClothingItem(Slot slot, int value) {
        this.slot = slot;

        this.value = value;
    }

    public int value() {
        return value;
    }

    public enum Slot {
        ALL(-1),
        HEAD(0),
        FACE(1),
        BODY(2),
        LEGS(3),
        HANDS(4),
        FEET(5);

        public final int value;

        Slot(int value) { this.value = value; }


    }
}
