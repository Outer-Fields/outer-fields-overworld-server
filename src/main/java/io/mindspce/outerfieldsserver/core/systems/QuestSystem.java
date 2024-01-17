package io.mindspce.outerfieldsserver.core.systems;

import io.mindspce.outerfieldsserver.components.primatives.SimpleListener;
import io.mindspce.outerfieldsserver.core.singletons.EntityManager;
import io.mindspce.outerfieldsserver.entities.PlayerQuestEntity;
import io.mindspce.outerfieldsserver.entities.WorldQuestEntity;
import io.mindspce.outerfieldsserver.enums.PlayerQuests;
import io.mindspce.outerfieldsserver.enums.SystemType;
import io.mindspce.outerfieldsserver.enums.WorldQuests;
import io.mindspce.outerfieldsserver.systems.event.Event;
import io.mindspce.outerfieldsserver.systems.event.EventType;
import io.mindspce.outerfieldsserver.systems.event.SystemListener;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;


public class QuestSystem extends SystemListener {
    private final EnumMap<PlayerQuests, List<PlayerQuestEntity>> playerQuestsTable;
    private final EnumMap<WorldQuests, WorldQuestEntity> worldQuestTable;
    private final SimpleListener newQuestListener;

    public QuestSystem(boolean doStart) {
        super(SystemType.QUEST, doStart, 2000000);
        EntityManager.GET().registerSystem(this);
        playerQuestsTable = new EnumMap<>(PlayerQuests.class);
        worldQuestTable = new EnumMap<>(WorldQuests.class);
        newQuestListener = new SimpleListener(EntityManager.GET().newSystemEntity(SystemType.QUEST));
        newQuestListener.registerInputHook(EventType.QUEST_PLAYER_NEW, this::addPlayerQuest, true);
        newQuestListener.registerInputHook(EventType.QUEST_WORLD_NEW, this::addWorldQuest, true);
        registerComponent(newQuestListener);
    }

    private void addPlayerQuest(Event<PlayerQuestEntity> event) {
        PlayerQuestEntity playerQuest = event.data();
        playerQuestsTable.computeIfAbsent(playerQuest.quest(), v -> new ArrayList<>(4)).add(playerQuest);
    }

    private void addWorldQuest(Event<WorldQuestEntity> event) {
        WorldQuestEntity worldQuest = event.data();
        worldQuestTable.putIfAbsent(worldQuest.quest(), worldQuest);
    }

}
