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
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Predicate;


public class ThoughtModule<T extends Enum<T>, U extends Task<T, U>, V> extends Component<ThoughtModule<T, U, V>> {
    public Task<T, U> currentTask;
    public T lastTask;

    public List<Thought<T, U>> tryingToDo = new ArrayList<>(2);
    public List<Thought<T, U>> wantingToDo = new ArrayList<>(2);
    public List<Thought<T, U>> randomTasks = new ArrayList<>(2);
    public PriorityQueue<Task<T, U>> queuedTasks = new PriorityQueue<>(
            (Task<T, U> o1, Task<T, U> o2) -> Float.compare(o2.taskWeight, o1.taskWeight));

    public List<T> canDo = new ArrayList<>(6);
    public DecisionEventGraph<V, T> decisionGraph;
    public V graphFocusState;
    public int graphQueryRate;
    private int currQueryTick;

    public Consumer<Task<T, U>> taskConsumer;

    public ThoughtModule(Entity parentEntity, DecisionEventGraph<V, T> decisionGraph, V graphFocusState, int queryRate) {
        super(parentEntity, ComponentType.THOUGHT_MODULE, List.of());
        this.decisionGraph = decisionGraph;
        this.graphFocusState = graphFocusState;
        this.graphQueryRate = queryRate;
        decisionGraph.getActionEvents().forEach(ae -> ae.linkEventConsumer(this::onEventNodeResponse));
        setOnTickConsumer(ThoughtModule::tickProcess);
    }

    public ThoughtModule(Entity parentEntity, DecisionEventGraph<V, T> decisionGraph, V graphFocusState, int queryRate,
            Consumer<Task<T, U>> taskConsumer) {
        super(parentEntity, ComponentType.THOUGHT_MODULE, List.of());
        this.taskConsumer = taskConsumer;
        this.decisionGraph = decisionGraph;
        this.graphFocusState = graphFocusState;
        this.graphQueryRate = queryRate;
        decisionGraph.getActionEvents().forEach(ae -> ae.linkEventConsumer(this::onEventNodeResponse));
        setOnTickConsumer(ThoughtModule::tickProcess);
    }

    public ThoughtModule(Entity parentEntity) {
        super(parentEntity, ComponentType.THOUGHT_MODULE, List.of());
        setOnTickConsumer(ThoughtModule::tickProcess);
    }

    private void onEventNodeResponse(T eventKey) {
        canDo.add(eventKey);
    }

    public void tickProcess(Tick tick) {
        if (--currQueryTick == 0) {
            canDo.clear();
            decisionGraph.getRoot().travel(graphFocusState);
            currQueryTick = graphQueryRate;
        }

        if (!currentTask.isCompleted() && !currentTask.isSuspendable()) {
            return;
        }

        if (currentTask.isCompleted()) {
            var queuedTask = queuedTasks.poll();
            if (queuedTask != null) {
                currentTask = queuedTask;
                currentTask.resume();
                return;
            }
        }

        if (currentTask.isSuspendable()) {
            if (calculateSuspendable(tryingToDo, tick)) { return; }
            if (calculateSuspendable(wantingToDo, tick)) { return; }
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

    public void queueTask(Task<T, U> task) {
        queuedTasks.add(task);
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
                    currentTask.suspend();
                    currentTask = thought.task;
                    if (taskConsumer != null) { taskConsumer.accept(thought.task); }
                    if (thought.oneShot) { taskList.remove(thought); }
                    return true;
                }
            }
        }
        return false;
    }

    public Pair<TriConsumer<EventType, Consumer<?>, Boolean>, Consumer<EventType>> getEventHooks() {
        return Pair.of(this::registerInputHook, this::clearInputHooksFor);
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
