package io.mindspce.outerfieldsserver.entities;

import io.mindspce.outerfieldsserver.enums.AreaId;
import io.mindspce.outerfieldsserver.enums.EntityType;
import io.mindspce.outerfieldsserver.enums.State;
import io.mindspice.mindlib.data.geometry.IVector2;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;


public abstract class Entity {
    protected final EntityType entityType;
    protected volatile int entityId = -1;
    protected volatile boolean idSet;
    protected final List<State> states = new CopyOnWriteArrayList<>();
    protected volatile String name = "";

    public Entity(EntityType entityType) {
        this.entityType = entityType;
    }

    public void setEntityId(int entityId) {
        this.entityId = entityId;
        idSet = true;
    }

    public List<State> states() { return states; }

    public void setStates(List<State> states) { this.states.addAll(states); }

    public void setName(String name) { this.name = name; }

    public String name() { return name; }

    public void addState(State state) { states.add(state); }

    public void clearState(State state) { states.clear(); }

    public int id() { return entityId; }

    public EntityType entityType() { return entityType; }

    public int entityTypeValue() { return entityType.value; }

    // Implementation Specific, these need to return thread-safe data
    // Some implementing classes already hold this data so overridden
    // "handles" to it are used

    public abstract IVector2 globalPosition();

    public abstract IVector2 priorPosition();

    public abstract AreaId currentArea();

    public abstract IVector2 chunkIndex();

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Entity: ");
        sb.append("\n  entityType: ").append(entityType);
        sb.append(",\n  id: ").append(entityId);
        sb.append(",\n  states: ").append(states);
        sb.append(",\n  name: \"").append(name).append('\"');
        sb.append("\n");
        return sb.toString();
    }


}
