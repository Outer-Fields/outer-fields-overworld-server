package io.mindspce.outerfieldsserver.ai.decisiongraph.actions;

import java.util.function.Consumer;


public class ConsumerAction<T> extends Action<T> {
    private final Consumer<T> actionConsumer;

    public ConsumerAction(Consumer<T> actionConsumer) {
        this.actionConsumer = actionConsumer;
    }

    public static <T> ConsumerAction<T> of(Consumer<T> actionConsumer) {
        return new ConsumerAction<>(actionConsumer);
    }

    @Override
    public boolean doAction(T focusState) {
        actionConsumer.accept(focusState);
        return true;
    }

}
