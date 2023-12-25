package io.mindspce.outerfieldsserver.components.subcomponents;

import io.mindspce.outerfieldsserver.components.Component;
import io.mindspce.outerfieldsserver.systems.event.Event;
import io.mindspce.outerfieldsserver.systems.event.EventListener;
import io.mindspce.outerfieldsserver.systems.event.EventType;
import io.mindspce.outerfieldsserver.systems.event.ListenerCache;
import io.mindspice.mindlib.data.geometry.IRect2;

import java.util.Set;
import java.util.function.BiConsumer;


public class AreaMonitor extends Component implements EventListener {
    private final ListenerCache<AreaMonitor> listenerCache = new ListenerCache<>();
    private final IRect2 monitoredArea;
    private final BiConsumer<AreaMonitor, Event> onEvent;

    public AreaMonitor(int ownerId, IRect2 monitoredArea, BiConsumer<AreaMonitor, Event> onEvent) {
        super(ownerId);

        this.monitoredArea = monitoredArea;
        this.onEvent = onEvent;
    }

    @Override
    public void onTick(long tickTime, double delta) {

    }

    @Override
    public void onEvent(Event event) {
        listenerCache.handleEvent(this, event);
    }

    @Override
    public void onDirect(Event event) {

    }

    @Override
    public boolean isListenerFor(EventType eventType) {
        return false;
    }
}
