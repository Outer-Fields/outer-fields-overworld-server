package io.mindspice.outerfieldsserver.components.quest;

import io.mindspice.outerfieldsserver.ai.task.SubTask;
import io.mindspice.outerfieldsserver.components.Component;
import io.mindspice.outerfieldsserver.core.Tick;
import io.mindspice.outerfieldsserver.entities.Entity;
import io.mindspice.outerfieldsserver.entities.PlayerQuestEntity;
import io.mindspice.outerfieldsserver.entities.WorldQuestEntity;
import io.mindspice.outerfieldsserver.enums.ComponentType;
import io.mindspice.outerfieldsserver.systems.event.Event;
import io.mindspice.outerfieldsserver.systems.event.EventType;
import io.mindspice.mindlib.data.wrappers.LazyFinalValue;
import io.mindspice.mindlib.util.MUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;


public class QuestModule<T> extends Component<QuestModule<T>> {
    public final T questData;
    public final List<SubTask<T>> taskList = new ArrayList<>();
    public final boolean concurrentSubTasks;
    public final BiConsumer<QuestModule<T>, T> completionConsumer;
    public final boolean isWorld;
    public final LazyFinalValue<Boolean> isCompleted = new LazyFinalValue<>(false);
    private boolean started = false;

    public QuestModule(Entity parentEntity, List<EventType> emittedEvents, T questData,
            boolean concurrentSubTasks, BiConsumer<QuestModule<T>, T> completionConsumer, boolean isWorld) {
        super(
                parentEntity,
                ComponentType.QUEST_MODULE,
                MUtils.mergeToNewList(emittedEvents, EventType.QUEST_COMPLETED_WORLD, EventType.QUEST_COMPLETED_PLAYER)
        );

        this.questData = questData;
        this.concurrentSubTasks = concurrentSubTasks;
        this.completionConsumer = completionConsumer;
        this.isWorld = isWorld;

        setOnTickConsumer(QuestModule::onTickConsumer);
    }

    public void start() {
        if (taskList.isEmpty()) { return; }
        if (concurrentSubTasks) {
            taskList.forEach(SubTask::onStart);
        } else {
            taskList.getFirst().onStart();
        }
        started = true;
    }

    private void onTickConsumer(Tick tick) {
        if (!started) { start(); }
        if (isCompleted.get()) { return; }
        completionCheck();
        if (isCompleted.get()) { return; }
        if (concurrentSubTasks) {
            taskList.forEach(t -> t.onTick(tick));
        } else {
            if (taskList.isEmpty()) { return; }
            taskList.getFirst().onTick(tick);
        }
    }

    public boolean completionCheck() {
        boolean completed = false;
        if (concurrentSubTasks) {
            if (taskList.stream().allMatch(SubTask::completed)) { completed = true; }
        } else if (taskList.getFirst().completed()) {
            taskList.removeFirst();
            if (taskList.isEmpty()) {
                completed = true;
            } else {
                taskList.getFirst().onStart();
            }
        }
        if (completed) {
            isCompleted.setOrThrow(true);
            if (completionConsumer != null) { completionConsumer.accept(this, questData); }
            emitEvent(isWorld
                    ? Event.questCompletedWorld((WorldQuestEntity) parentEntity)
                    : Event.questCompletedPlayer((PlayerQuestEntity) parentEntity)
            );
        }
        return completed;
    }

    // TODO register listeners
    public void addSubEventTask(EventType eventType, BiPredicate<T, Event<?>> completionEventPredicate,
            Consumer<T> onStartConsumer, boolean suspendable) {
        SubTask<T> subTask = SubTask.ofOnEvent(eventType, questData, onStartConsumer, completionEventPredicate, suspendable);
        subTask.linkListenerHooks(this::registerInputHook, this::clearInputHooksFor);
        taskList.add(subTask);
    }

    public void addSubTickTask(BiPredicate<T, Tick> completionTickPredicate, Consumer<T> onStartConsumer,
            boolean suspendable) {
        taskList.add(SubTask.ofOnTick(questData, onStartConsumer, completionTickPredicate,
                suspendable));
    }

    public void addSubEventTickTask(EventType eventType, BiPredicate<T, Event<?>> completionEventPredicate,
            BiPredicate<T, Tick> completionTickPredicate, Consumer<T> onStartConsumer, boolean suspendable) {
        SubTask<T> subTask = SubTask.ofEventAndTick(eventType, questData, onStartConsumer, completionEventPredicate,
                completionTickPredicate, suspendable);
        subTask.linkListenerHooks(this::registerInputHook, this::clearInputHooksFor);
        taskList.add(subTask);
    }

}
