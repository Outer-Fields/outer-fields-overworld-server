package io.mindspce.outerfieldsserver.ai.decisiongraph;

import static io.mindspce.outerfieldsserver.ai.decisiongraph.Node.NodeType.ROOT;


public class RootNode<T> extends Node<T> {

    public RootNode() {
        super(ROOT, "Root");
    }

    @Override
    public boolean travel(T focusState) {
        for (Node<T> child : adjacentNodes) {
            if (child.travel(focusState)) { return true; }
        }
        //TODO logic for if all decisions fail
        return false;
    }
}
