package io.mindspce.outerfieldsserver.ai.decisiongraph.actions;

public abstract class Action<T> {
    public abstract boolean doAction(T focusState);
}
