package io.mindspice.outerfieldsserver.systems.event;

import java.util.function.Consumer;


public record CallBack<T, U>(
        U data,
        Consumer<T> callback
) { }
