package io.mindspice.outerfieldsserver.components.primatives;

import io.mindspice.outerfieldsserver.components.Component;
import io.mindspice.outerfieldsserver.entities.Entity;
import io.mindspice.outerfieldsserver.enums.ComponentType;

import java.util.List;


public class SimpleListener extends Component<SimpleListener> {
    public SimpleListener(Entity parentEntity) {
        super(parentEntity, ComponentType.SIMPLER_LISTENER, List.of());
    }
}
