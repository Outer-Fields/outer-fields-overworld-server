package io.mindspce.outerfieldsserver.ai.decisiongraph.decisions;

import io.mindspce.outerfieldsserver.ai.decisiongraph.Node;

import java.util.List;

import static io.mindspce.outerfieldsserver.ai.decisiongraph.Node.NodeType.DECISION;


public class DecisionNode<T> extends Node<T> {
    private final List<Decision<T>> decisions;

    public DecisionNode(String name, List<Decision<T>> decisions) {
        super(DECISION, name);
        this.decisions = decisions;
    }

    public static <T> DecisionNode<T> of(String name, List<Decision<T>> decisions) {
        return new DecisionNode<>(name, decisions);
    }

    @Override
    public boolean travel(T focusState) {
        for (Decision<T> decision : decisions) {
            var rtn = decision.getDecision(focusState);
            if (!rtn) { return false; }
        }

        for (Node<T> node : adjacentNodes) {
            if (node.travel(focusState)) { return true; }
        }
        return false;
    }
}
