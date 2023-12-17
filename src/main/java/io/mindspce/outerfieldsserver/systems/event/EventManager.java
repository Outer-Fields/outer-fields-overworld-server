package io.mindspce.outerfieldsserver.systems.event;

import io.mindspce.outerfieldsserver.systems.event.EntityEvent;
import io.mindspice.mindlib.data.tuples.Pair;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;


public class EventManager {

    private final EntityEvent[][] entityEvents;
    private final AtomicInteger entityEventCount = new AtomicInteger(0);
    private volatile int entityMaxEventSize;
    private volatile int activeEntityArray = 0;
    private final Object entityEventLock = new Object();

    public EventManager(int entityEventSize) {
        entityEvents = new EntityEvent[2][entityEventSize];
        entityMaxEventSize = entityEventSize;
    }

    public void pushEntityEvent(EntityEvent event) {
        synchronized (entityEventLock) {
            if (entityEventCount.get() >= entityMaxEventSize) {
                int resizeFactor = (int) (entityMaxEventSize * 1.25);
                resizeBuffers(resizeFactor);
            }
            entityEvents[activeEntityArray][entityEventCount.getAndIncrement()] = event;
        }
    }

    public Pair<EntityEvent[], Integer> getEntityEvents() {
        synchronized (entityEventLock) {
            int currentCount = entityEventCount.getAndSet(0);
            EntityEvent[] events = Arrays.copyOf(entityEvents[activeEntityArray], currentCount);
            activeEntityArray = activeEntityArray == 0 ? 1 : 0;

            // Check if the buffer can be shrunk
            if (currentCount < (int) (entityMaxEventSize * 0.5)) {
                int resizeFactor = (int) (currentCount * 0.75); // Avoid shrinking below initial size
                resizeBuffers(resizeFactor);
            }

            Arrays.fill(entityEvents[activeEntityArray], null); // Clear the newly active buffer
            return new Pair<>(events, currentCount);
        }
    }

    private void resizeBuffers(int newSize) {
        for (int i = 0; i < entityEvents.length; i++) {
            entityEvents[i] = Arrays.copyOf(entityEvents[i], newSize);
        }
        entityMaxEventSize = newSize;
    }
}
