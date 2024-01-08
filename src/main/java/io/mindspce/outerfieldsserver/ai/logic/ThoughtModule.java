package io.mindspce.outerfieldsserver.ai.logic;

import io.mindspce.outerfieldsserver.components.Component;
import io.mindspce.outerfieldsserver.core.Tick;
import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.enums.ComponentType;
import io.mindspce.outerfieldsserver.systems.Task;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Predicate;

// TODO ad a way to suspend task for another and return with an interuptible flag


public class ThoughtModule<T extends Enum<T>, U extends Task<T, U>> extends Component<ThoughtModule<T, U>> {
    public Task<T, U> currentTask;
    public T lastTask;

    public List<Thought<T, U>> tryingToDo = new ArrayList<>(2);
    public List<Thought<T, U>> wantingToDo = new ArrayList<>(2);
    public List<Thought<T, U>> randomTasks = new ArrayList<>(2);
    public PriorityQueue<Task<T, U>> queuedTasks = new PriorityQueue<>(
            (Task<T, U> o1, Task<T, U> o2) -> Float.compare(o2.taskWeight, o1.taskWeight));
    public List<T> canDo = new ArrayList<>(6);
    public Consumer<Task<T, U>> taskConsumer;

    public ThoughtModule(Entity parentEntity, Consumer<Task<T, U>> taskConsumer) {
        super(parentEntity, ComponentType.THOUGHT_MODULE, List.of());
        this.taskConsumer = taskConsumer;
        setOnTickConsumer(ThoughtModule::tickProcess);
    }

    public ThoughtModule(Entity parentEntity) {
        super(parentEntity, ComponentType.THOUGHT_MODULE, List.of());
        setOnTickConsumer(ThoughtModule::tickProcess);
    }

    public void tickProcess(Tick tick) {
        if (!currentTask.isCompleted() && !currentTask.isSuspendable()) {
            return;
        }

        if (!canDo.isEmpty()) {
            if (calculateTask(tryingToDo, tick)) { return; }
            if (calculateTask(wantingToDo, tick)) { return; }
        }
        if (!randomTasks.isEmpty()) {
            var randTasks = randomTasks.stream().filter(t -> canDo.contains(t.type)).toList();
            Thought<T, U> randThought = randTasks.get(ThreadLocalRandom.current().nextInt(randTasks.size()));
            if (randThought.oneShot) {
                randomTasks.remove(randThought);
            }
        }
    }

    public boolean calculateSuspendable(List<Thought<T, U>> taskList, Tick tick) {
        List<Thought<T, U>> canDoTasks = taskList.stream()
                .filter(t -> canDo.contains(t.type()))
                .sorted((t1, t2) -> Double.compare(t2.weight(), t1.weight()))
                .toList();

        for (var thought : canDoTasks) {
            if (thought.tickPredicate.test(tick)) {
                if (thought.weight() > currentTask.taskWeight) {
                    queuedTasks.add(currentTask);
                    currentTask = thought.task;
                    if (taskConsumer != null) { taskConsumer.accept(thought.task); }
                    if (thought.oneShot) { taskList.remove(thought); }
                    return true;
                }
            }

        }
    }

    public boolean calculateTask(List<Thought<T, U>> taskList, Tick tick) {
        List<Thought<T, U>> canDoTasks = taskList.stream()
                .filter(t -> canDo.contains(t.type()))
                .sorted((t1, t2) -> Double.compare(t2.weight(), t1.weight()))
                .toList();

        for (var thought : canDoTasks) {
            if (thought.tickPredicate.test(tick)) {
                lastTask = currentTask.taskType;
                currentTask = thought.task;
                if (taskConsumer != null) { taskConsumer.accept(thought.task); }
                if (thought.oneShot) { taskList.remove(thought); }
                return true;
            }
        }
        return false;
    }

    public void queueTask(Task<T, U> task) {
        queuedTasks.add(task);
    }

    public record Thought<T extends Enum<T>, U extends Task<T, U>>(
            T type,
            Task<T, U> task,
            boolean oneShot,
            Predicate<Tick> tickPredicate
    ) {

        public float weight() {
            return task.taskWeight;
        }
    }
}
