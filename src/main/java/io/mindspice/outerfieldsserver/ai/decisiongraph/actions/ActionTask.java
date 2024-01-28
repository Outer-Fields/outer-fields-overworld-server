package io.mindspice.outerfieldsserver.ai.decisiongraph.actions;

import io.mindspice.outerfieldsserver.ai.decisiongraph.NewActionTask;

import java.util.function.Consumer;
import java.util.function.Function;


public class ActionTask<T, U> extends Action<T> {
    private final String name;
    private final Function<T, NewActionTask<U>> taskProducer;
    private Consumer<NewActionTask<U>> taskConsumer;

    public ActionTask(String name, Function<T, NewActionTask<U>> taskProducer) {
        this.name = name;
        this.taskProducer = taskProducer;

    }

    public static <T, U> ActionTask<T, U> of(String name,  Function<T, NewActionTask<U>> taskProducer) {
        return new ActionTask<>(name, taskProducer);
    }

    public void linkTaskConsumer(Consumer<NewActionTask<U>> eventConsumer) {
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
        NewActionTask<U> task = taskProducer.apply(focusState);
        taskConsumer.accept(task);
        return true;
    }

}
