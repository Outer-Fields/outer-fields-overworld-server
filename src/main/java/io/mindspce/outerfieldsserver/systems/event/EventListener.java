package io.mindspce.outerfieldsserver.systems.event;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;


public interface EventListener<T extends EventListener<T>> {
    void onEvent(Event event);

    void onDirect(Event event);

    void onCallBack(Callback<T> consumer);

    boolean isListenerFor(EventType eventType);

}
