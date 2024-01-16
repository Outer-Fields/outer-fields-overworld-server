package io.mindspce.outerfieldsserver.components.quest;

import io.mindspce.outerfieldsserver.ai.task.SubTask;
import io.mindspce.outerfieldsserver.components.Component;
import io.mindspce.outerfieldsserver.core.Tick;
import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.enums.ComponentType;
import io.mindspce.outerfieldsserver.systems.event.EventType;
import io.mindspice.mindlib.data.wrappers.LazyFinalValue;

import java.util.List;
import java.util.function.BiConsumer;


public class PlayerQuestModule<T> extends Component<PlayerQuestModule<T>> {
    public final T questData;
    public final List<SubTask<T>> taskList;
    public final boolean concurrentSubTasks;
    public final BiConsumer<PlayerQuestModule<T>, T> completionConsumer;
    public final LazyFinalValue<Boolean> isCompleted = new LazyFinalValue<>(false);

    public PlayerQuestModule(Entity parentEntity, List<EventType> emittedEvents, T questData, List<SubTask<T>> taskList, boolean concurrentSubTasks,
            BiConsumer<PlayerQuestModule<T>, T> completionConsumer) {
        super(parentEntity, ComponentType.PLAYER_QUEST, emittedEvents);
        this.questData = questData;
        this.taskList = taskList;
        this.concurrentSubTasks = concurrentSubTasks;
        this.completionConsumer = completionConsumer;

        setOnTickConsumer(PlayerQuestModule::onTickConsumer);
    }

    public void start() {
        if (taskList.isEmpty()) { return; }
        if (concurrentSubTasks) {
            taskList.forEach(SubTask::onStart);
        } else {
            taskList.getFirst().onStart();
        }
    }

    private void onTickConsumer(Tick tick) {
        if (isCompleted.get()) { return; }
        if (concurrentSubTasks) {
            taskList.forEach(t -> t.onTick(tick));
        } else {
            if (taskList.isEmpty()) { return; }
            taskList.getFirst().onTick(tick);
        }
    }

    public boolean completionCheck() {
        if (concurrentSubTasks) {
            if (taskList.stream().allMatch(SubTask::completed)) {
                isCompleted.setOrThrow(true);
                if (completionConsumer != null) { completionConsumer.accept(this, questData); }
                return true;
            }
        } else if (taskList.getFirst().completed()) {
            taskList.removeFirst();
            if (taskList.isEmpty()) {
                if (completionConsumer != null) { completionConsumer.accept(this, questData); }
                return true;
            }
            taskList.getFirst().onStart();
        }
        return false;
    }
}
