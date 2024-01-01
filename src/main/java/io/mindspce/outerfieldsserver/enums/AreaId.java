package io.mindspce.outerfieldsserver.enums;

public enum AreaId {
    NONE(-2),
    GLOBAL(-1),
    TEST(0);

    public final int value;

    AreaId(int value) { this.value = value; }
}
