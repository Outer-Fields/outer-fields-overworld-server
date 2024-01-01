package io.mindspce.outerfieldsserver.core.statemanagers;

import io.mindspce.outerfieldsserver.core.singletons.EntityManager;
import io.mindspce.outerfieldsserver.systems.event.*;
import io.mindspce.outerfieldsserver.systems.event.EventListener;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


public class NpcStateManager implements SystemListener {
    private static final ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
    private final Map<EventType, List<EventListener<?>>> registeredListeners = new HashMap<>();

    public NpcStateManager() {

        EntityManager.GET().registerEventListener(this::onEvent);
        // entityEventSubscriptions.addAll(List.of(EntityEventType.NEW_POSITION));
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
            if (event.isDirect()) {
                for (int i = 0; i < listeners.size(); ++i) {
                    if (listeners.get(i).isListenerFor(event.eventType())
                            && listeners.get(i).entityId() == event.recipientEntityId()) {
                        listeners.get(i).onEvent(event);
                        return;
                    }
                }
                return;
            }

            for (int i = 0; i < listeners.size(); ++i) {
                if (listeners.get(i).isListenerFor(event.eventType())) {
                    if (listeners.get(i).entityId() == event.recipientEntityId()) {
                        listeners.get(i).onEvent(event);
                    }
                }
            }
        });
    }

    @Override
    public void onTick(long tickTime, double deltaTime) {

    }

}
