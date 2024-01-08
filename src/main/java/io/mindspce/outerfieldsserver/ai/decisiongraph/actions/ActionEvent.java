package io.mindspce.outerfieldsserver.ai.decisiongraph.actions;


import java.util.function.BiConsumer;


public class ActionEvent<U, T> extends Action<T> {
    final U eventKey;
    private final BiConsumer<U, T> eventConsumer;

    public ActionEvent(BiConsumer<U, T> eventConsumer, U eventKey) {
        this.eventKey = eventKey;
        this.eventConsumer = eventConsumer;
    }

    @Override
    public boolean doAction(T focusState) {
        eventConsumer.accept(eventKey, focusState);
        return false;
    }

}
