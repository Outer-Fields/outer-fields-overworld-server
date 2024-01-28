package io.mindspice.outerfieldsserver.ai.decisiongraph.actions;

import io.mindspice.outerfieldsserver.ai.decisiongraph.Node;

import static io.mindspice.outerfieldsserver.ai.decisiongraph.Node.NodeType.ACTION;


public class ActionNode<T> extends Node<T> {
    private final Action<T> action;

    public ActionNode(String name, Action<T> action) {
        super(ACTION, name);
        this.action = action;
    }

    public static <T> ActionNode<T> of(String name, Action<T> action) {
        return new ActionNode<>(name, action);
    }

    @Override
    public boolean travel(T focusState) {
        return action.doAction(focusState);
    }
}
