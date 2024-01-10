package io.mindspce.outerfieldsserver.ai.decisiongraph;

import io.mindspce.outerfieldsserver.ai.decisiongraph.actions.ActionEvent;
import io.mindspce.outerfieldsserver.ai.decisiongraph.actions.ActionNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;


public class DecisionEventGraph<T, U extends Enum<U>> {
    private final Node<T> rootNode;
    private final Stack<Node<T>> nodeStack = new Stack<>();
    private final List<ActionEvent<T, U>> eventNodes = new ArrayList<>();

    // Init with root node that is returned when built
    public DecisionEventGraph(RootNode<T> rootNode) {
        this.rootNode = rootNode;
        nodeStack.push(rootNode);
    }

    // Add a child node and step to that depth
    public DecisionEventGraph<T, U> addChild(Node<T> node) {
        Node<T> current = nodeStack.peek();
        current.addAdjacent(node);
        nodeStack.push(node);
        return this;
    }

    // Add another child to the parent node, sibling to other children of the parent node
    public DecisionEventGraph<T, U> addSibling(Node<T> node) {
        stepBack(); // Step back to the parent node
        return addChild(node); // Add the node as a child to the parent
    }

    // Step back to the parent of the current depth
    public DecisionEventGraph<T, U> stepBack() {
        if (nodeStack.size() > 1) {
            nodeStack.pop();
        }
        return this;
    }

    // Add a terminal leaf to the current node and step back to maintain depth
    // of the current node
    public DecisionEventGraph<T, U> addLeaf(ActionEvent<T, U> event) {
        eventNodes.add(event);
        addChild(new ActionNode<>(event, event.name()));
        stepBack(); // Step back to the parent of the leaf
        return this;
    }

    public List<ActionEvent<T, U>> getActionEvents() {
        return eventNodes;
    }

    // Return the root node
    public RootNode<T> getRoot() {
        return (RootNode<T>) rootNode;
    }
}

