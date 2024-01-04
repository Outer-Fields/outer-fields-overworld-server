package io.mindspce.outerfieldsserver.systems.event;

import io.mindspce.outerfieldsserver.enums.ComponentType;

import java.util.function.Consumer;


public record CallBack<T, U>(
        U data,
        Consumer<T> callback
) { }
