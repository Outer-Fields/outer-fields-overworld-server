package io.mindspce.outerfieldsserver.components.ai;

import io.mindspce.outerfieldsserver.ai.decisiongraph.RootNode;
import io.mindspce.outerfieldsserver.components.Component;
import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.enums.ComponentType;
import io.mindspce.outerfieldsserver.systems.event.Event;
import io.mindspce.outerfieldsserver.systems.event.EventType;

import java.util.List;


public class BehaviorModule<T> extends Component<BehaviorModule<T>> {
    private final RootNode<T> decisionGraph;

    public BehaviorModule(Entity parentEntity, RootNode<T> decisionGraph) {
        super(parentEntity, ComponentType.BEHAVIOR_MODULE, List.of());
        this.decisionGraph = decisionGraph;
    }



}
