package io.mindspce.outerfieldsserver.enums;

public enum FactionType {
    PLAYER(1),
    NON_PLAYER(2);

    public final int value;

    FactionType(int value) { this.value = value; }
}
