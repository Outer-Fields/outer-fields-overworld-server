package io.mindspice.outerfieldsserver.enums;

public enum WorldQuests {
;

 public final String fullName;
 public final QuestType type;
 public final long key;

    WorldQuests(String fullName, QuestType type, long key) {
        this.fullName = fullName;
        this.type = type;
        this.key = key;
    }
}
