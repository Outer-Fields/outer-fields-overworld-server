package io.mindspce.outerfieldsserver.components;

import io.mindspce.outerfieldsserver.systems.event.*;


public abstract class Component implements TickListener {
    private final int ownerId;

    protected Component(int ownerId) {
        this.ownerId = ownerId;
    }
}