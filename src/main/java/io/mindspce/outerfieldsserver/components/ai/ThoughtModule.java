package io.mindspce.outerfieldsserver.components.ai;

import ch.qos.logback.core.encoder.EchoEncoder;
import io.mindspce.outerfieldsserver.ai.decisiongraph.DecisionEventGraph;
import io.mindspce.outerfieldsserver.ai.decisiongraph.actions.ActionTask;
import io.mindspce.outerfieldsserver.ai.task.Task;
import io.mindspce.outerfieldsserver.components.Component;
import io.mindspce.outerfieldsserver.core.Tick;
import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.enums.ComponentType;
import io.mindspice.mindlib.data.tuples.Pair;
import io.mindspice.mindlib.util.DebugUtils;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Predicate;


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
        actionTask.linkTaskConsumer((task, replace) -> {
            task.linkListenerHooks(this::registerInputHook, this::clearInputHooksFor);
            if (currentTask == null || replace) {
                currentTask = task;
                task.start();
            } else if (currentTask.isSuspendable() && currentTask.taskWeight() > task.taskWeight()) {
                taskQueue.add(currentTask);
                currentTask = task;
                task.start();
            } else {
                taskQueue.add(task);
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
