package io.mindspce.outerfieldsserver.ai.decisiongraph.actions;

import java.util.function.BiConsumer;
import java.util.function.Consumer;


public class BiConsumerAction<U, T> extends Action<T> {
    public U focusObj;
    private final BiConsumer<U, T> actionConsumer;

    public BiConsumerAction(U focusObj, BiConsumer<U, T> actionConsumer) {
        this.actionConsumer = actionConsumer;
    }

    @Override
    public boolean doAction(T focusState) {
        actionConsumer.accept(focusObj, focusState);
        return true;
    }

}
