package io.mindspice.outerfieldsserver.components.ai;

import io.mindspice.outerfieldsserver.ai.decisiongraph.DecisionEventGraph;
import io.mindspice.outerfieldsserver.ai.decisiongraph.actions.ActionTask;
import io.mindspice.outerfieldsserver.ai.task.Task;
import io.mindspice.outerfieldsserver.components.Component;
import io.mindspice.outerfieldsserver.core.Tick;
import io.mindspice.outerfieldsserver.entities.Entity;
import io.mindspice.outerfieldsserver.enums.ComponentType;

import java.util.*;
import java.util.function.Consumer;


public class ThoughtModule<T> extends Component<ThoughtModule<T>> {
    public Task<?> currentTask;
    public String lastTask;
    public PriorityQueue<Task<?>> taskQueue = new PriorityQueue<>(
            (Task<?> o1, Task<?> o2) -> Float.compare(o2.taskWeight(), o1.taskWeight()));

    public DecisionEventGraph<T> decisionGraph;
    public T graphFocusState;
    public int graphQueryRate;
    private int currQueryTick;

    public Consumer<Task<?>> taskConsumer;

    public ThoughtModule(Entity parentEntity, DecisionEventGraph<T> decisionGraph, T graphFocusState, int queryTickInterval) {
        super(parentEntity, ComponentType.THOUGHT_MODULE, List.of());
        this.decisionGraph = decisionGraph;
        this.graphFocusState = graphFocusState;
        this.graphQueryRate = queryTickInterval;
        decisionGraph.getActionEvents().forEach(this::linkTaskConsumer);
        setOnTickConsumer(ThoughtModule::tickProcess);
    }

    public ThoughtModule(Entity parentEntity, int queryTickInterval) {
        super(parentEntity, ComponentType.THOUGHT_MODULE, List.of());

        this.graphQueryRate = queryTickInterval;
        setOnTickConsumer(ThoughtModule::tickProcess);
    }

    public void addDecisionGraph(DecisionEventGraph<T> decisionGraph, T graphFocusState) {
        this.decisionGraph = decisionGraph;
        this.graphFocusState = graphFocusState;
        decisionGraph.getActionEvents().forEach(this::linkTaskConsumer);
    }

    private void linkTaskConsumer(ActionTask<T, ?> actionTask) {
        actionTask.linkTaskConsumer((newActionTask) -> {
            newActionTask.task().linkListenerHooks(this::registerInputHook, this::clearInputHooksFor);
            if (currentTask == null || newActionTask.interruptExisting()) {
                if (currentTask != null) { currentTask.cancel(); }
                currentTask = newActionTask.task();
                currentTask.start();
            } else if (currentTask.isSuspendable() && currentTask.taskWeight() > newActionTask.task().taskWeight()) {
                taskQueue.add(currentTask);
                currentTask = newActionTask.task();
                currentTask.start();
            } else {
                taskQueue.add(newActionTask.task());
            }

        });
    }

    public ThoughtModule(Entity parentEntity, DecisionEventGraph<T> decisionGraph, T graphFocusState, int queryTickInterval,
            Consumer<Task<?>> taskConsumer) {
        super(parentEntity, ComponentType.THOUGHT_MODULE, List.of());
        this.taskConsumer = taskConsumer;
        this.decisionGraph = decisionGraph;
        this.graphFocusState = graphFocusState;
        this.graphQueryRate = queryTickInterval;
        decisionGraph.getActionEvents().forEach(this::linkTaskConsumer);
        setOnTickConsumer(ThoughtModule::tickProcess);
    }

    public void tickProcess(Tick tick) {
        long t = System.nanoTime();
        if (currentTask != null) {
            currentTask.onTick(tick);
            if (currentTask.isCompleted()) {
                lastTask = currentTask.taskName();
                currentTask = taskQueue.isEmpty() ? null : taskQueue.poll();
            }
        }

        if (currentTask != null && !currentTask.isSuspendable()) {

            return;
        }

        if (graphQueryRate == -1 && currentTask != null) {

            return;
        }

        if (graphQueryRate != -1 && --currQueryTick > 0) {

            return;
        }

        currQueryTick = graphQueryRate;
        decisionGraph.getRoot().travel(graphFocusState);
    }

    public void queueTask(Task<?> task) {
        taskQueue.add(task);
    }

}
