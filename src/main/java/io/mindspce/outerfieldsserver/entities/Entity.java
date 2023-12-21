package io.mindspce.outerfieldsserver.entities;

import io.mindspce.outerfieldsserver.enums.EntityType;
import io.mindspce.outerfieldsserver.enums.State;
import io.mindspice.mindlib.data.geometry.IVector2;
import io.mindspice.mindlib.data.wrappers.LazyFinalValue;

import java.util.*;
import java.util.concurrent.locks.StampedLock;


public abstract class Entity {
    protected final EntityType entityType;
    protected LazyFinalValue<Integer> id = new LazyFinalValue<>();
    protected volatile State[] states = new State[0];
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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Entity: ");
        sb.append("\n  entityType: ").append(entityType);
        sb.append(",\n  id: ").append(id.get());
        sb.append(",\n  states: ").append(Arrays.toString(states));
        sb.append(",\n  name: \"").append(name).append('\"');
        sb.append("\n");
        return sb.toString();
    }
}
