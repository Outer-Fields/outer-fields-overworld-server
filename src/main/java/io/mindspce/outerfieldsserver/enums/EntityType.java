package io.mindspce.outerfieldsserver.enums;

public enum EntityType {
    PLAYER(0),
    NPC(1),
    ENEMY(3),
    ITEM(4),
    LOCATION(5);

    public final int value;

    EntityType(int value) { this.value = value; }
}
