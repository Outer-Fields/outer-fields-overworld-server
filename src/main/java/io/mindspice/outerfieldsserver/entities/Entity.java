package io.mindspice.outerfieldsserver.entities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.mindspice.outerfieldsserver.components.Component;
import io.mindspice.outerfieldsserver.components.serialization.Visibility;
import io.mindspice.outerfieldsserver.enums.AreaId;
import io.mindspice.outerfieldsserver.enums.ComponentType;
import io.mindspice.outerfieldsserver.enums.EntityType;
import io.mindspice.outerfieldsserver.enums.SystemType;
import io.mindspice.outerfieldsserver.systems.event.SystemListener;
import io.mindspice.outerfieldsserver.systems.event.EventType;
import io.mindspice.mindlib.data.collections.sets.AtomicBitSet;
import io.mindspice.mindlib.data.geometry.IVector2;
import io.mindspice.mindlib.data.tuples.Pair;
import io.mindspice.mindlib.util.JsonUtils;

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
    protected SystemListener systemRegistry;

    public Entity(int id, EntityType entityType, AreaId areaId, List<Component<?>> components) {
        this.id = id;
        this.entityType = entityType;
        this.areaId = areaId;
        componentList = new CopyOnWriteArrayList<>(components);

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
        componentList = new CopyOnWriteArrayList<>(components);
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

        componentList.forEach(c -> {
            if (c.componentType == (component.componentType)) {
                throw new IllegalStateException("Attempted to add same component twice on Entity: " + id
                        + " | EntityType: " + entityType + " | Compoent: " + component);
            }
        });

        if (systemRegistry != null) {
            System.out.println("Registered component with system: " + component.componentType);
            systemRegistry.registerComponent(component);
        }
        componentList.add(component);
        attachedComponents.set(component.componentType().ordinal());
        component.getAllListeningFor().forEach(e -> listeningFor.set(e.ordinal()));
    }

    public void addComponents(List<Component<?>> components) {
        components.forEach(this::addComponent);
    }

    public int entityId() {
        return id;
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public SystemType registeredWith() {
        return systemRegistry == null ? SystemType.NONE : systemRegistry.systemType();
    }

    public void registerWithSystem(SystemListener system){
        if (systemRegistry != null) {
            throw new IllegalStateException("Attempted to register entity with system twice");
        }
        this.systemRegistry = system;
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
        return componentList == null ? List.of() :  List.copyOf(componentList);
    }

    public Component<?> getComponent(ComponentType componentType) {
        return componentList.stream().filter(c -> c.componentType() == componentType).findFirst().orElse(null);
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

    public List<EventType> listeningForTypes() {
        List<EventType> listening = new ArrayList<>();
        for (int i = 0; i < listeningFor.size(); ++i) {
            boolean bit = listeningFor.get(i);
            if (bit) { listening.add(EventType.values()[i]); }
        }
        return listening;
    }

    public String toString() {
        JsonNode node = new JsonUtils.ObjectBuilder()
                .put("entityType", entityType)
                .put("entityId", id)
                .put("name", name)
                .put("areaId", areaId)
                .put("chunkIndex", chunkIndex)
                .put("attachComponents", getAttachedComponentTypes())
                .put("listeningFor", listeningForTypes())
                .put("systemRegistry", systemRegistry != null ? systemRegistry.systemType() : null)
                .buildNode();
        try {
            return JsonUtils.writePretty(node);
        } catch (JsonProcessingException e) {
            return "Error serializing to string";
        }
    }




}
