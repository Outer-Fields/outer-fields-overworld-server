package io.mindspce.outerfieldsserver.systems.event;

import java.util.function.Consumer;


public interface EventListener<T extends EventListener<T>> {
    void onEvent(Event<?> event);

    void onCallBack(Consumer<T> consumer);

    boolean isListenerFor(EventType eventType);

}
