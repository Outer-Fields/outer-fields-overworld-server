package io.mindspce.outerfieldsserver.enums;

public enum PlayerQuests {



;

 public final String fullName;
 public final QuestType type;
 public final long key;

    PlayerQuests(String fullName, QuestType type, long key) {
        this.fullName = fullName;
        this.type = type;
        this.key = key;
    }
}
