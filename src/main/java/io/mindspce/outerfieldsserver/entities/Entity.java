package io.mindspce.outerfieldsserver.entities;

import io.mindspce.outerfieldsserver.components.Component;
import io.mindspce.outerfieldsserver.enums.AreaId;
import io.mindspce.outerfieldsserver.enums.ComponentType;
import io.mindspce.outerfieldsserver.enums.EntityType;
import io.mindspce.outerfieldsserver.enums.State;
import io.mindspice.mindlib.data.geometry.IVector2;

import java.util.BitSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public abstract class Entity {
    protected final EntityType entityType;
    protected final int entityId;

    protected volatile AreaId areaId = null;
    protected volatile IVector2 chunkIndex = IVector2.of(-1, -1);

    public Entity(int id, EntityType entityType, AreaId areaId) {
        this.entityId = id;
        this.entityType = entityType;
        this.areaId = areaId;
    }

    public Entity(int id, EntityType entityType, AreaId areaId, IVector2 chunkIndex) {
        this.entityId = id;
        this.entityType = entityType;
        this.areaId = areaId;
        this.chunkIndex = chunkIndex;
    }

    public Entity(int id, EntityType entityType, List<Component<?>> components) {
        this.entityId = id;
        this.entityType = entityType;
        components.forEach(c -> attachedComponents.set(c.componentType().ordinal()));
        this.componentList.addAll(components);
    }

    public void addComponents(List<Component<?>> components) {
        components.forEach(c -> attachedComponents.set(c.componentType().ordinal()));
        componentList.addAll(components);
    }

    public void addComponent(Component<?> component) {
        attachedComponents.set(component.componentType().ordinal());
        componentList.add(component);

    }

    public int id() {
        return entityId;
    }

    public EntityType entityType() {
        return entityType;
    }

    public int entityTypeValue() {
        return entityType.value;
    }

    public int entityId() {
        return entityId;
    }

    public List<Component<?>> componentList() {
        return componentList;
    }

    public BitSet attachedComponents() {
        return attachedComponents;
    }

    public AreaId areaId() {
        return areaId;
    }

    public IVector2 chunkIndex() {
        return chunkIndex;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Entity: ");
        sb.append("\n  entityType: ").append(entityType);
        sb.append(",\n  id: ").append(entityId);
        sb.append("\n");
        return sb.toString();
    }


}
