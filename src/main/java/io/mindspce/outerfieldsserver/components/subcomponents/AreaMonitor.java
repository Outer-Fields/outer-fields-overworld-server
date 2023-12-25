package io.mindspce.outerfieldsserver.components.subcomponents;

import io.mindspce.outerfieldsserver.components.Component;
import io.mindspce.outerfieldsserver.core.singletons.EntityManager;
import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.enums.EntityType;
import io.mindspce.outerfieldsserver.systems.event.*;
import io.mindspice.mindlib.data.geometry.IRect2;
import io.mindspice.mindlib.data.geometry.IVector2;

import java.util.function.BiConsumer;
import java.util.function.Consumer;


public class AreaMonitor extends Component<AreaMonitor> implements EventListener<AreaMonitor> {
    private final IRect2 monitoredArea;

    public AreaMonitor(Entity parent, IRect2 monitoredArea, BiConsumer<AreaMonitor, Event<?>> onEvent) {
        super(parent);
        this.monitoredArea = monitoredArea;
        listenerCache.addListener(EventType.PLAYER_POSITION, AreaMonitor::enteredAreaCheck);
    }

    private void enteredAreaCheck(Event<IVector2> event) {
        if (monitoredArea.contains(event.entity().globalPosition())) {
            event.data();
        }
        EntityManager.GET().emitEvent(Event.of(parentEntity.id(), EventType.AREA_ENTERED, EntityType.LOCATION));
    }
}

@Override
public void onTick(long tickTime, double delta) {

}

@Override
public void onEvent(Event<?> event) {
    listenerCache.handleEvent(this, event);
}

@Override
public void onCallBack(Consumer<AreaMonitor> consumer) {
    consumer.accept(this);
}

@Override
public boolean isListenerFor(EventType eventType) {
    return false;
}

private record AreaMonitorData(

)
