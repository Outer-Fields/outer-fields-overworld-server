package io.mindspice.outerfieldsserver.ai.decisiongraph.decisions;

import io.mindspice.outerfieldsserver.ai.decisiongraph.Node;

import java.util.List;
import java.util.function.Predicate;

import static io.mindspice.outerfieldsserver.ai.decisiongraph.Node.NodeType.DECISION;


public class PredicateNode<T> extends Node<T> {
    private final List<Predicate<T>> decisions;

    public PredicateNode(String name, List<Predicate<T>> decisions) {
        super(DECISION, name);
        this.decisions = decisions;
    }

    public static <T> PredicateNode<T> of(String name, List<Predicate<T>> predicates) {
        return new PredicateNode<>(name, predicates);
    }

    public static <T> PredicateNode<T> of(String name, Predicate<T> predicate) {
        return new PredicateNode<>(name, List.of(predicate));
    }

    @Override
    public boolean travel(T focusState) {
        for (Predicate<T> decision : decisions) {
            var rtn = decision.test(focusState);
            if (!rtn) { return false; }
        }

        for (Node<T> node : adjacentNodes) {
            if (node.travel(focusState)) { return true; }
        }
        return false;
    }
}
