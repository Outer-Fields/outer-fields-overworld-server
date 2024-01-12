package io.mindspce.outerfieldsserver.entities;

import io.mindspce.outerfieldsserver.area.AreaEntity;
import io.mindspce.outerfieldsserver.components.Component;
import io.mindspce.outerfieldsserver.core.singletons.EntityManager;
import io.mindspce.outerfieldsserver.enums.AreaId;
import io.mindspce.outerfieldsserver.enums.ComponentType;
import io.mindspce.outerfieldsserver.enums.EntityType;
import io.mindspce.outerfieldsserver.enums.SystemType;
import io.mindspce.outerfieldsserver.systems.event.SystemListener;
import io.mindspce.outerfieldsserver.systems.event.EventType;
import io.mindspice.mindlib.data.collections.sets.AtomicBitSet;
import io.mindspice.mindlib.data.geometry.IVector2;
import io.mindspice.mindlib.data.tuples.Pair;
import io.mindspice.mindlib.data.wrappers.LazyFinalValue;
import io.mindspice.mindlib.util.DebugUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * The type Entity.
 */
public abstract class Entity {

    protected final EntityType entityType;
    protected final int id;
    protected volatile String name = "";
    protected volatile AreaId areaId = null;
    protected volatile IVector2 chunkIndex = IVector2.of(-1, -1);
    private List<Component<?>> componentList;
    protected final AtomicBitSet attachedComponents = new AtomicBitSet(ComponentType.values().length);
    protected final AtomicBitSet listeningFor = new AtomicBitSet(EventType.values().length);

    public Entity(int id, EntityType entityType, AreaId areaId, List<Component<?>> components) {
        this.id = id;
        this.entityType = entityType;
        this.areaId = areaId;
        componentList = new CopyOnWriteArrayList<>();
        componentList.addAll(components);
    }

    public Entity(int id, EntityType entityType, AreaId areaId) {
        this.id = id;
        this.entityType = entityType;
        this.areaId = areaId;
        componentList = null;
    }

    public Entity(int id, EntityType entityType, AreaId areaId, IVector2 chunkIndex, List<Component<?>> components) {
        this.id = id;
        this.entityType = entityType;
        this.areaId = areaId;
        this.chunkIndex = chunkIndex;
        componentList = new CopyOnWriteArrayList<>();
        componentList.addAll(components);
    }

    public Entity(int id, EntityType entityType, AreaId areaId, IVector2 chunkIndex) {
        this.id = id;
        this.entityType = entityType;
        this.areaId = areaId;
        this.chunkIndex = chunkIndex;
        componentList = null;
    }

    public void addComponent(Component<?> component) {

        if (componentList == null) {
            componentList = new CopyOnWriteArrayList<>();
        }
        componentList.add(component);
        attachedComponents.set(component.componentType().ordinal());
        component.getAllListeningFor().forEach(e -> listeningFor.set(e.ordinal()));
    }

    public void addComponents(List<Component<?>> components) {
        if (componentList == null) {
            componentList = new CopyOnWriteArrayList<>();
        }
        components.forEach(this::addComponent);
    }

    public int entityId() {
        return id;
    }

    public String name() {
        return name;
    }

    public EntityType entityType() {
        return entityType;
    }

    public AreaId areaId() {
        return areaId;
    }

    public IVector2 chunkIndex() {
        return chunkIndex;
    }

    public boolean isListenerFor(EventType eventType) {
        return listeningFor.get(eventType.ordinal());
    }

    public boolean hasAttachedComponent(ComponentType componentType) {
        return attachedComponents.get(componentType.ordinal());
    }

    public List<EventType> getListeningFor() {
        return Arrays.stream(EventType.values()).filter(e -> listeningFor.get(e.ordinal())).toList();
    }

    public List<ComponentType> getAttachedComponentTypes() {
        return Arrays.stream(ComponentType.values()).filter(e -> attachedComponents.get(e.ordinal())).toList();
    }

    public List<Component<?>> getAttachedComponents() {
        return List.copyOf(componentList);
    }

    public List<Component<?>> getComponent(ComponentType componentType) {
        return componentList.stream().filter(c -> c.componentType() == componentType).toList();
    }

    public long[] getComponentTypeIds(ComponentType componentType) {
        return componentList.stream()
                .filter(c -> c.componentType() == componentType)
                .mapToLong(Component::componentId)
                .toArray();
    }

    public List<Pair<ComponentType, Long>> getAllComponentIds() {
        return componentList.stream().map(c -> Pair.of(c.componentType(), c.componentId())).toList();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Entity: ");
        sb.append("\n  entityType: ").append(entityType);
        sb.append(",\n  id: ").append(id);
        sb.append(",\n  name: \"").append(name).append('\"');
        sb.append(",\n  areaId: ").append(areaId);
        sb.append(",\n  chunkIndex: ").append(chunkIndex);
        sb.append(",\n  componentList: ").append(componentList);
        sb.append(",\n  attachedComponents: ").append(attachedComponents);
        sb.append(",\n  listeningFor: ").append(listeningFor);
        sb.append("\n");
        return sb.toString();
    }

    public void registerComponents(SystemListener systemRegistry) {
        systemRegistry.registerComponents(componentList);
    }


}
