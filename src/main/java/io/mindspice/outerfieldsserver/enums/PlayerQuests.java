package io.mindspice.outerfieldsserver.enums;

public enum PlayerQuests {

    TEST("Test Quest", QuestType.TEST, 42342342L);

    public final String fullName;
    public final QuestType type;
    public final long key;

    PlayerQuests(String fullName, QuestType type, long key) {
        this.fullName = fullName;
        this.type = type;
        this.key = key;
    }
}
