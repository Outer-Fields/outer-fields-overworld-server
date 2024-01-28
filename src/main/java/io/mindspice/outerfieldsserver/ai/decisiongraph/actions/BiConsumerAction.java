package io.mindspice.outerfieldsserver.ai.decisiongraph.actions;

import java.util.function.BiConsumer;
import java.util.function.Supplier;


public class BiConsumerAction<T, U> extends Action<T> {
    public final Supplier<U> otherData;
    private final BiConsumer<U, T> actionConsumer;

    public BiConsumerAction(Supplier<U> otherData, BiConsumer<U, T> actionConsumer) {
        this.otherData = otherData;
        this.actionConsumer = actionConsumer;
    }

    public static <T, U> BiConsumerAction<T, U> of(Supplier<U> otherData, BiConsumer<U, T> actionConsumer) {
        return new BiConsumerAction<>(otherData, actionConsumer);
    }

    @Override
    public boolean doAction(T focusState) {
        actionConsumer.accept(otherData.get(), focusState);
        return true;
    }

}
