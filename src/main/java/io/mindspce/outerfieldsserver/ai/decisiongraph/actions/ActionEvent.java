package io.mindspce.outerfieldsserver.ai.decisiongraph.actions;

import java.util.function.BiConsumer;
import java.util.function.Consumer;


public class ActionEvent<T, U extends Enum<U>> extends Action<T> {
    private final U eventKey;
    private Consumer<U> eventConsumer;

    public ActionEvent(U eventKey) {
        this.eventKey = eventKey;
    }

    public static <T, U extends Enum<U>> ActionEvent<T, U> of(U eventKey) {
        return new ActionEvent<>(eventKey);
    }

    public void linkEventConsumer(Consumer<U> eventConsumer) {
        this.eventConsumer = eventConsumer;
    }

    public String name() {
        return eventKey.name();
    }

    @Override
    public boolean doAction(T focusState) {
        if (eventConsumer == null) {
            throw new IllegalStateException("No event consumer linked");
        }
        eventConsumer.accept(eventKey);
        return false;
    }

}
