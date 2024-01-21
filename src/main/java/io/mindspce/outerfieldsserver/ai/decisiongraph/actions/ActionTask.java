package io.mindspce.outerfieldsserver.ai.decisiongraph.actions;

import io.mindspce.outerfieldsserver.ai.task.Task;
import io.mindspice.mindlib.data.tuples.Pair;

import java.util.function.BiConsumer;
import java.util.function.Function;


public class ActionTask<T, U> extends Action<T> {
    private final String name;
    private final Function<T, Pair<Task<U>, Boolean>> taskProducer;
    private BiConsumer<Task<U>, Boolean> taskConsumer;

    public ActionTask(String name, Function<T, Pair<Task<U>, Boolean>> taskProducer) {
        this.name = name;
        this.taskProducer = taskProducer;

    }

    public static <T, U> ActionTask<T, U> of(String name, Function<T, Pair<Task<U>, Boolean>> taskProducer) {
        return new ActionTask<>(name, taskProducer);
    }

    public void linkTaskConsumer(BiConsumer<Task<U>, Boolean> eventConsumer) {
        this.taskConsumer = eventConsumer;
    }

    public String name() {
        return name;
    }

    @Override
    public boolean doAction(T focusState) {
        if (taskConsumer == null) {
            throw new IllegalStateException("No event consumer linked");
        }
        Pair<Task<U>, Boolean> task = taskProducer.apply(focusState);
        taskConsumer.accept(task.first(), task.second());
        return true;
    }

}
