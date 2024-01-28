package io.mindspice.outerfieldsserver.components.ai;

import io.mindspice.outerfieldsserver.ai.decisiongraph.RootNode;
import io.mindspice.outerfieldsserver.components.Component;
import io.mindspice.outerfieldsserver.entities.Entity;
import io.mindspice.outerfieldsserver.enums.ComponentType;
import io.mindspice.outerfieldsserver.systems.event.Event;

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
