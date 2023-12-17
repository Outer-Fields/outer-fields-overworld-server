package io.mindspce.outerfieldsserver.entities;

import io.mindspce.outerfieldsserver.enums.EntityType;
import io.mindspce.outerfieldsserver.enums.State;
import io.mindspice.mindlib.data.geometry.IVector2;
import io.mindspice.mindlib.data.wrappers.LazyFinalValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.StampedLock;


public abstract class Entity {
    protected final EntityType entityType;
    protected LazyFinalValue<Integer> id;
    protected volatile State[] states = new State[1];
    protected volatile String name = "";

    public Entity(EntityType entityType) {
        this.entityType = entityType;
    }

    public void setId(int id) { this.id.set(id); }

    public State[] states() { return states; }

    public void setStates(State[] states) { this.states = states; }

    public String name() { return name; }

    public void addState() { }

    public int id() { return id.getOrThrow(); }

    public EntityType entityType() { return entityType; }

    public int entityTypeValue() { return entityType.value; }

    public abstract IVector2 globalPosition();

    public abstract IVector2 priorPosition();

}
