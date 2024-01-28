package io.mindspice.outerfieldsserver.combat.enums;

public enum EffectLength {
    SHORT(0),
    AVERAGE(1),
    LONG(2);

    public final int amount;

    EffectLength(int amount) {
        this.amount = amount;
    }
}
