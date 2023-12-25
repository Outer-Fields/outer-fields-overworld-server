package io.mindspce.outerfieldsserver.core.statemanagers;

import io.mindspce.outerfieldsserver.components.subcomponents.AreaMonitor;
import io.mindspce.outerfieldsserver.core.singletons.EntityManager;
import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.systems.event.EventDomain;
import io.mindspce.outerfieldsserver.systems.event.EventType;
import io.mindspce.outerfieldsserver.systems.event.Event;
import io.mindspice.mindlib.data.geometry.IRect2;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


public class NpcStateManager {
    private static final ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
    private final Set<EventType> entityEventSubscriptions = new CopyOnWriteArraySet<>();

    public NpcStateManager() {
        EntityManager.GET().registerEventListener(EventDomain.CHARACTER, this::entityEventHandler);
        // entityEventSubscriptions.addAll(List.of(EntityEventType.NEW_POSITION));
    }

    public void entityEventHandler(Event event) {
        if (entityEventSubscriptions.contains(event.type())) {
            //loop over lists of EventListeners check if they monitor for that message and issue;
        }
    }

    public void onTick(Event event) {

        AreaMonitor mon = new AreaMonitor(IRect2.of(12, 12, 12, 12), (areaMonitor, event1) -> {
            if (areaMonitor.monitoredArea().contains(event1.entityData().globalPosition())) {
                EntityManager.GET().emitEvent(new Event<>(EventType.POSITION, null));
            }
        });
    }

    public Runnable handleCharPositionUpdate(Event<Entity> entityEvent) {
        return () -> {
            try {
                Entity entity = EntityManager.GET().getEntityById(entityEvent.entityData().id());
                entity.globalPosition();
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
    }

    public interface Task {
        void execute();
    }
}
