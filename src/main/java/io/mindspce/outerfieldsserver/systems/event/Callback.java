package io.mindspce.outerfieldsserver.systems.event;

import java.util.function.Consumer;


public record Callback<T extends Callback<T>>(
        int entityId,
        Consumer<T> callback
) { }
