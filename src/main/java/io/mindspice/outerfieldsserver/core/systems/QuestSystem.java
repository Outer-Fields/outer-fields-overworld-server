package io.mindspice.outerfieldsserver.core.systems;

import io.mindspice.outerfieldsserver.components.primatives.SimpleListener;
import io.mindspice.outerfieldsserver.core.singletons.EntityManager;
import io.mindspice.outerfieldsserver.entities.PlayerQuestEntity;
import io.mindspice.outerfieldsserver.entities.WorldQuestEntity;
import io.mindspice.outerfieldsserver.enums.PlayerQuests;
import io.mindspice.outerfieldsserver.enums.SystemType;
import io.mindspice.outerfieldsserver.enums.WorldQuests;
import io.mindspice.outerfieldsserver.systems.event.Event;
import io.mindspice.outerfieldsserver.systems.event.EventType;
import io.mindspice.outerfieldsserver.systems.event.SystemListener;
import io.mindspice.outerfieldsserver.util.Utility;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;


public class QuestSystem extends SystemListener {
    private final EnumMap<PlayerQuests, List<PlayerQuestEntity>> playerQuestsTable;
    private final EnumMap<WorldQuests, WorldQuestEntity> worldQuestTable;

    public QuestSystem(int id) {
        super(id, SystemType.QUEST, true, Utility.msToNano(5));
        playerQuestsTable = new EnumMap<>(PlayerQuests.class);
        worldQuestTable = new EnumMap<>(WorldQuests.class);
        selfListener.registerInputHook(EventType.QUEST_PLAYER_NEW, this::addPlayerQuest, true);
        selfListener.registerInputHook(EventType.QUEST_WORLD_NEW, this::addWorldQuest, true);
        selfListener.registerInputHook(EventType.QUEST_COMPLETED_PLAYER, this::onPlayerQuestCompleted, true);
        selfListener.registerInputHook(EventType.QUEST_COMPLETED_WORLD, this::onWorldQuestCompleted, true);
    }

    private void addPlayerQuest(Event<PlayerQuestEntity> event) {
        PlayerQuestEntity playerQuest = event.data();
        playerQuestsTable.computeIfAbsent(playerQuest.quest(), v -> new ArrayList<>(4)).add(playerQuest);
    }

    private void addWorldQuest(Event<WorldQuestEntity> event) {
        WorldQuestEntity worldQuest = event.data();
        WorldQuestEntity existing = worldQuestTable.putIfAbsent(worldQuest.quest(), worldQuest);
        if (existing != null) {
            //TODO log this
        }
    }

    private void onPlayerQuestCompleted(Event<PlayerQuestEntity> event) {
        playerQuestsTable.get(event.data().quest()).removeIf(
                q -> q.participatingPlayerId() == event.data().participatingPlayerId()
        );
    }

    private void onWorldQuestCompleted(Event<WorldQuestEntity> event) {
        worldQuestTable.remove(event.data().quest());
    }

}
