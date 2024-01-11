package io.mindspce.outerfieldsserver.components.primatives;

import io.mindspce.outerfieldsserver.components.Component;
import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.enums.ComponentType;

import java.util.List;


public class SimpleListener extends Component<SimpleListener> {
    public SimpleListener(Entity parentEntity) {
        super(parentEntity, ComponentType.SIMPLER_LISTENER, List.of());
    }
}
