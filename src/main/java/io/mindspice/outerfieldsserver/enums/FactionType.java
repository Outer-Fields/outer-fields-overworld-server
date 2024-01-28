package io.mindspice.outerfieldsserver.enums;

public enum FactionType {
    ALL(1),
    PLAYER(2),
    NON_PLAYER(3);

    public final int value;

    FactionType(int value) { this.value = value; }
}
