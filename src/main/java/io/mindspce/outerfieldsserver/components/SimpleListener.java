package io.mindspce.outerfieldsserver.components;

import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.enums.ComponentType;
import io.mindspce.outerfieldsserver.enums.QueryType;

import java.util.List;


public class SimpleListener extends Component<SimpleListener>{
    public SimpleListener(Entity parentEntity) {
        super(parentEntity, ComponentType.SIMPLER_LISTENER, List.of());
    }
}
