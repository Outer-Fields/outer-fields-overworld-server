package io.mindspce.outerfieldsserver.core.statemanagers;

import io.mindspce.outerfieldsserver.components.subcomponents.AreaMonitor;
import io.mindspce.outerfieldsserver.core.singletons.EntityManager;
import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.systems.event.*;
import io.mindspce.outerfieldsserver.systems.event.EventListener;
import io.mindspice.mindlib.data.geometry.IRect2;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


public class NpcStateManager implements SystemListener {
    private static final ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
    private final Map<EventType, List<EventListener<?>>> registeredListeners = new HashMap<>();

    public NpcStateManager() {

        EntityManager.GET().registerEventListener(this::eventHandler);
        // entityEventSubscriptions.addAll(List.of(EntityEventType.NEW_POSITION));
    }

    public void eventHandler(Event<?> event) {

    }

    public void registerListener(EventType eventType, EventListener<?> listener) {
        var listenerList = registeredListeners.get(eventType);
        if (listenerList == null) { listenerList = new ArrayList<>(); }
        listenerList.add(listener);
    }

    @Override
    public void onEvent(Event<?> event) {
        exec.submit(() -> {
            List<EventListener<?>> listeners = registeredListeners.get(event.eventType());
            for (int i = 0; i < listeners.size(); ++i) {
                if (listeners.get(i).isListenerFor(event.eventType())) {
                    listeners.get(i).onEvent(event);
                }
            }
        });
    }

//    @Override
//    public void onCallback(Callback<?> callback) {
//        exec.submit(() -> {
//            List<EventListener<?>> listeners = registeredListeners.get(event.eventType());
//            for (int i = 0; i < listeners.size(); ++i) {
//                if (listeners.get(i).isListenerFor(event.eventType())) {
//                    listeners.get(i).onEvent(event);
//                }
//            }
//        });
    }
//
//    public void onTick(Event event) {
//
//        AreaMonitor mon = new AreaMonitor(IRect2.of(12, 12, 12, 12), (areaMonitor, event1) -> {
//            if (areaMonitor.monitoredArea().contains(event1.entityData().globalPosition())) {
//                EntityManager.GET().emitEvent(new Event<>(EventType.POSITION, null));
//            }
//        });
//    }
//
//    public Runnable handleCharPositionUpdate(Event<Entity> entityEvent) {
//        return () -> {
//            try {
//                Entity entity = EntityManager.GET().getEntityById(entityEvent.entityData().id());
//                entity.globalPosition();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        };
//    }
//
//    public interface Task {
//        void execute();
//    }
}
