package io.mindspice.outerfieldsserver.ai.decisiongraph;


import java.util.ArrayList;
import java.util.List;


public abstract class Node<T> {
    protected final String name;
    protected final NodeType nodeType;
    protected final List<Node<T>> adjacentNodes = new ArrayList<>(10);

    public Node(NodeType nodeType, String name) {
        this.nodeType = NodeType.ROOT;
        this.name = name;
    }

    public abstract boolean travel(T focusState);

    public NodeType getType() {
        return nodeType;
    }

    public List<Node<T>> getAdjacentNodes() {
        return adjacentNodes;
    }

    public String getName() {
        return name;
    }

    public void addAdjacent(Node<T> node) {
        if (nodeType == NodeType.ACTION) { throw new IllegalStateException("Can't add adjacent nodes to action nodes"); }
        if (adjacentNodes.contains(node)) {
            // TODO log this
            System.out.println("Attempt to add same node twice ignored");
            return;
        } // Keep from adding the same node twice
        adjacentNodes.add(node);
    }

    @Override
    public String toString() {
        return name;
    }

    public boolean printGraph(int depth) {
        printNode(depth);
        if (this.nodeType == NodeType.ACTION) { return false; }
        for (Node adjacentNode : getAdjacentNodes()) {
            if (!adjacentNode.printGraph(depth + 1)) { return false; }
        }
        return true;
    }

    protected void printNode(int depth) {
        for (int i = 0; i < depth; i++) { System.out.print("\t- "); }
        System.out.println(this);
    }

    public enum NodeType {
        ROOT,
        DECISION,
        ACTION
    }
}
