package io.mindspce.outerfieldsserver.components.ai;

import io.mindspce.outerfieldsserver.ai.decisiongraph.DecisionEventGraph;
import io.mindspce.outerfieldsserver.ai.task.Task;
import io.mindspce.outerfieldsserver.components.Component;
import io.mindspce.outerfieldsserver.core.Tick;
import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.enums.ComponentType;
import io.mindspce.outerfieldsserver.systems.event.EventType;
import io.mindspice.mindlib.data.tuples.Pair;
import io.mindspice.mindlib.functional.consumers.TriConsumer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Predicate;


public class ThoughtModule<T extends Enum<T>, V> extends Component<ThoughtModule<T, V>> {
    public Task<T, ?> currentTask;
    public T lastTask;

    public List<Thought<T>> tryingToDo = new LinkedList<>();
    public List<Thought<T>> wantingToDo = new LinkedList<>();
    public List<Thought<T>> randomTasks = new LinkedList<>();
    public PriorityQueue<Task<T, ?>> taskQueue = new PriorityQueue<>(
            (Task<T, ?> o1, Task<T, ?> o2) -> Float.compare(o2.taskWeight, o1.taskWeight));

    public List<T> canDo = new ArrayList<>(6);
    public DecisionEventGraph<V, T> decisionGraph;
    public V graphFocusState;
    public int graphQueryRate;
    private int currQueryTick;

    public boolean suspendThought = false;

    public Consumer<Task<T, ?>> taskConsumer;

    public ThoughtModule(Entity parentEntity, DecisionEventGraph<V, T> decisionGraph, V graphFocusState, int queryRate) {
        super(parentEntity, ComponentType.THOUGHT_MODULE, List.of());
        this.decisionGraph = decisionGraph;
        this.graphFocusState = graphFocusState;
        this.graphQueryRate = queryRate;
        decisionGraph.getActionEvents().forEach(ae -> ae.linkEventConsumer(this::onEventNodeResponse));
        setOnTickConsumer(ThoughtModule::tickProcess);
    }

    public ThoughtModule(Entity parentEntity, DecisionEventGraph<V, T> decisionGraph, V graphFocusState, int queryRate,
            Consumer<Task<T, ?>> taskConsumer) {
        super(parentEntity, ComponentType.THOUGHT_MODULE, List.of());
        this.taskConsumer = taskConsumer;
        this.decisionGraph = decisionGraph;
        this.graphFocusState = graphFocusState;
        this.graphQueryRate = queryRate;
        decisionGraph.getActionEvents().forEach(ae -> ae.linkEventConsumer(this::onEventNodeResponse));
        setOnTickConsumer(ThoughtModule::tickProcess);
    }

    public void addWantingToDo(T type, boolean isOneShot, Predicate<Tick> startPredicate, Task<T, ?> task) {
        task.linkListenerControls(this::registerInputHook, this::clearInputHooksFor);
        var thought = new Thought<>(type, isOneShot, startPredicate, task);
        tryingToDo.add(thought);
    }

    public void addTryingToDo(T type, boolean isOneShot, Predicate<Tick> startPredicate, Task<T, ?> task) {
        task.linkListenerControls(this::registerInputHook, this::clearInputHooksFor);
        var thought = new Thought<>(type, isOneShot, startPredicate, task);
        tryingToDo.add(thought);
    }

    public void addRandomTask(T type, boolean isOneShot, Predicate<Tick> startPredicate, Task<T, ?> task) {
        task.linkListenerControls(this::registerInputHook, this::clearInputHooksFor);
        var thought = new Thought<>(type, isOneShot, startPredicate, task);
        randomTasks.add(thought);
    }

    private void onEventNodeResponse(T eventKey) {
        canDo.add(eventKey);
    }

    public void tickProcess(Tick tick) {
        if (suspendThought) { return; }
        if (--currQueryTick <= 0) {
            canDo.clear();
            decisionGraph.getRoot().travel(graphFocusState);
            currQueryTick = graphQueryRate;
        }

        if (currentTask != null) {
            if (!currentTask.isCompleted() && !currentTask.isSuspendable()) {
                currentTask.onTick(tick);
                return;
            }
            if (currentTask.isCompleted()) {
                var queuedTask = taskQueue.poll();
                if (queuedTask != null) {
                    taskQueue.add(currentTask);
                    currentTask = queuedTask;
                    currentTask.start();
                } else {
                    lastTask = currentTask.taskType;
                    currentTask.reset();
                    currentTask = null;
                }
                return;
            }

            if (currentTask.isSuspendable()) {
                if (calculateSuspendable(tryingToDo, tick)) { return; }
            }
            currentTask.onTick(tick);
        }

        if (!canDo.isEmpty()) {
            if (calculateTask(tryingToDo, tick)) { return; }
            if (calculateTask(wantingToDo, tick)) { return; }
        }
        if (!randomTasks.isEmpty()) {
            var randTasks = randomTasks.stream().filter(t -> canDo.contains(t.type)).toList();
            Thought<T> randThought = randTasks.get(ThreadLocalRandom.current().nextInt(randTasks.size()));
            if (randThought.oneShot) {
                randomTasks.remove(randThought);
            }
        }
    }

    public void queueTask(Task<T, ?> task) {
        taskQueue.add(task);
    }

    public boolean calculateSuspendable(List<Thought<T>> taskList, Tick tick) {
        if (taskList == null) { return false; }

        List<Thought<T>> canDoTasks = taskList.stream()
                .filter(t -> canDo.contains(t.type()))
                .sorted((t1, t2) -> Double.compare(t2.weight(), t1.weight()))
                .toList();

        for (var thought : canDoTasks) {
            if (thought.tickPredicate.test(tick)) {
                if (thought.weight() > currentTask.taskWeight) {
                    taskQueue.add(currentTask);
                    currentTask.suspend();
                    currentTask = thought.task;
                    if (taskConsumer != null) { taskConsumer.accept(thought.task); }
                    currentTask.start();
                    if (thought.oneShot) {
                        taskList.remove(thought);
                    } else {
                        taskList.removeFirst();
                        taskList.add(thought);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public Pair<TriConsumer<EventType, Consumer<?>, Boolean>, Consumer<EventType>> getEventHooks() {
        return Pair.of(this::registerInputHook, this::clearInputHooksFor);
    }

    public boolean calculateTask(List<Thought<T>> taskList, Tick tick) {
        if (taskList == null) { return false; }

        List<Thought<T>> canDoTasks = taskList.stream()
                .filter(t -> canDo.contains(t.type()))
                .sorted((t1, t2) -> Double.compare(t2.weight(), t1.weight()))
                .toList();

        for (var thought : canDoTasks) {
            if (thought.tickPredicate.test(tick)) {
                currentTask = thought.task;
                if (taskConsumer != null) { taskConsumer.accept(thought.task); }
                currentTask.start();
                if (thought.oneShot) {
                    taskList.remove(thought);
                } else {
                    taskList.removeFirst();
                    taskList.add(thought);
                }
                return true;
            }
        }
        return false;
    }

    public record Thought<T extends Enum<T>>(
            T type,
            boolean oneShot,
            Predicate<Tick> tickPredicate,
            Task<T, ?> task
    ) {
        public float weight() {
            return task.taskWeight;
        }
    }

}
