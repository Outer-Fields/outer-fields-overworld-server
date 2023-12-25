package io.mindspce.outerfieldsserver.systems.event;

import java.util.function.Consumer;


public record Callback<T>(
        int entityId,
        Class<T> classType,
        Consumer<T> callback
) {

}
