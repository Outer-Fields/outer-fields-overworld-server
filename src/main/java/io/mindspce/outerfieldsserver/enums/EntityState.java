package io.mindspce.outerfieldsserver.enums;

public enum EntityState {
    TEST(-1);
    public final int value;

    EntityState(int value) { this.value = value; }

    public int value() {
        return value;
    }
}
