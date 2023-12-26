package io.mindspce.outerfieldsserver.components;

import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.enums.ComponentType;
import io.mindspce.outerfieldsserver.systems.event.ListenerCache;

import java.util.BitSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;


public class ComponentContainer<T extends Entity> {
    protected final List<Component<?>> componentList = new CopyOnWriteArrayList<>();
    protected final BitSet attachedComponents = new BitSet(ComponentType.values().length);
    protected final ListenerCache<>

}
