package io.mindspce.outerfieldsserver.ai.decisiongraph;

import java.util.Stack;


public class DecisionGraphBuilder<T> {
    private final Node<T> rootNode;
    private final Stack<Node<T>> nodeStack = new Stack<>();

    // Init with root node that is returned when built
    public DecisionGraphBuilder(RootNode<T> rootNode) {
        this.rootNode = rootNode;
        nodeStack.push(rootNode);
    }

    // Add a child node and step to that depth
    public DecisionGraphBuilder<T> addChild(Node<T> node) {
        Node<T> current = nodeStack.peek();
        current.addAdjacent(node);
        nodeStack.push(node);
        return this;
    }

    // Add another child to the parent node, sibling to other children of the parent node
    public DecisionGraphBuilder<T> addSibling(Node<T> node) {
        stepBack(); // Step back to the parent node
        return addChild(node); // Add the node as a child to the parent
    }

    // Step back to the parent of the current depth
    public DecisionGraphBuilder<T> stepBack() {
        if (nodeStack.size() > 1) {
            nodeStack.pop();
        }
        return this;
    }

    // Add a terminal leaf to the current node and step back to maintain depth
    // of the current node
    public DecisionGraphBuilder<T> addLeaf(Node<T> node) {
        addChild(node);
        stepBack(); // Step back to the parent of the leaf
        return this;
    }

    // Return the root node
    public RootNode<T> build() {
        return (RootNode<T>) rootNode;
    }
}

