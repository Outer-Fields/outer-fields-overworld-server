package io.mindspce.outerfieldsserver.components;

import io.mindspce.outerfieldsserver.ai.decisiongraph.RootNode;
import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.enums.ComponentType;
import io.mindspce.outerfieldsserver.systems.event.Event;

import java.util.List;


public class DecisionTree<T> extends Component<DecisionTree<T>> {
    public final RootNode<T> rootNode;
    public DecisionTree(Entity parentEntity, RootNode<T> rootNode) {
        super(parentEntity, ComponentType.DECISION_TREE, List.of());
        this.rootNode = rootNode;
    }

    public void onDecisionGraphQuery(Event<T> event) {
        rootNode.travel(event.data());
    }


}
