package io.mindspice.outerfieldsserver.core;

public record Tick(
        long tickTime,
        double deltaTime,
        long blockHeight
) {
}
