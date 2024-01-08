package io.mindspce.outerfieldsserver.ai.decisiongraph.actions;

import io.mindspce.outerfieldsserver.ai.decisiongraph.Node;

import static io.mindspce.outerfieldsserver.ai.decisiongraph.Node.NodeType.ACTION;


public class ActionNode<T> extends Node<T> {
    private final Action<T> action;

    public ActionNode(Action<T> action, String name) {
        super(ACTION, name);
        this.action = action;
    }

    @Override
    public boolean travel(T focusState) {
        return action.doAction(focusState);
    }
}
