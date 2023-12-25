package io.mindspce.outerfieldsserver.systems.event;

import java.util.Set;
import java.util.function.Consumer;


public interface Subscribable<T> {
    void subscribe(EventType eventType, T session);
    void unsubscribe(EventType eventType, T session);
    void broadcast(EventType eventType, Consumer<T> action);
    Set<T> getSubscribers(EventType eventType);
}
