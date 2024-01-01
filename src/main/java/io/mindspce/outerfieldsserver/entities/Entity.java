package io.mindspce.outerfieldsserver.entities;

import io.mindspce.outerfieldsserver.components.Component;
import io.mindspce.outerfieldsserver.enums.AreaId;
import io.mindspce.outerfieldsserver.enums.ComponentType;
import io.mindspce.outerfieldsserver.enums.EntityType;
import io.mindspce.outerfieldsserver.systems.event.CoreSystem;
import io.mindspce.outerfieldsserver.systems.event.EventType;
import io.mindspice.mindlib.data.collections.sets.AtomicBitSet;
import io.mindspice.mindlib.data.geometry.IVector2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * The type Entity.
 */
public abstract class Entity {
    /**
     * The Entity type.
     */
    protected final EntityType entityType;
    /**
     * The entity id.
     */
    protected final int id;
    /**
     * The Area id.
     */
    protected volatile AreaId areaId = null;
    /**
     * The Chunk index.
     */
    protected volatile IVector2 chunkIndex = IVector2.of(-1, -1);
    /**
     * The list of attached component objects.
     */
    protected final List<Component<?>> componentList;
    /**
     * Bitset to query for attached component confirmation.
     */
    protected final AtomicBitSet attachedComponents = new AtomicBitSet(ComponentType.values().length);
    /**
     * Bitset to query for events being listened for.
     */
    protected final AtomicBitSet listeningFor = new AtomicBitSet(EventType.values().length);

    /**
     * Instantiates a new Entity.
     *
     * @param id         the entity id
     * @param entityType the entity type
     * @param areaId     the area id
     * @param components the components
     */
    public Entity(int id, EntityType entityType, AreaId areaId, List<Component<?>> components) {
        this.id = id;
        this.entityType = entityType;
        this.areaId = areaId;
        componentList = new ArrayList<>();
        componentList.addAll(components);

    }

    /**
     * Instantiates a new Entity.
     *
     * @param id         the entity id
     * @param entityType the entity type
     * @param areaId     the area id
     */
    public Entity(int id, EntityType entityType, AreaId areaId) {
        this.id = id;
        this.entityType = entityType;
        this.areaId = areaId;
        componentList = null;
    }

    /**
     * Instantiates a new Entity.
     *
     * @param id         the entity id
     * @param entityType the entity type
     * @param areaId     the area id
     * @param chunkIndex the chunk index
     * @param components the components
     */
    public Entity(int id, EntityType entityType, AreaId areaId, IVector2 chunkIndex, List<Component<?>> components) {
        this.id = id;
        this.entityType = entityType;
        this.areaId = areaId;
        this.chunkIndex = chunkIndex;
        componentList = new ArrayList<>();
        componentList.addAll(components);

    }

    /**
     * Instantiates a new Entity.
     *
     * @param id         the entity id
     * @param entityType the entity type
     * @param areaId     the area id
     * @param chunkIndex the chunk index
     */
    public Entity(int id, EntityType entityType, AreaId areaId, IVector2 chunkIndex) {
        this.id = id;
        this.entityType = entityType;
        this.areaId = areaId;
        this.chunkIndex = chunkIndex;
        componentList = null;
    }

    /**
     * Add component.
     *
     * @param component the component to attach
     */
    public void addComponent(Component<?> component) {
        componentList.add(component);
        attachedComponents.set(component.componentType().ordinal());
        component.getAllListeningFor().forEach(e -> listeningFor.set(e.ordinal()));
    }

    /**
     * Add components.
     *
     * @param components the components to attach
     */
    public void addComponents(List<Component<?>> components) {
        components.forEach(this::addComponent);
    }

    /**
     * Entity id int.
     *
     * @return the int
     */
    public int entityId() {
        return id;
    }

    /**
     * Entity type EntityType.
     *
     * @return the entity type
     */
    public EntityType entityType() {
        return entityType;
    }

    /**
     * Area id area id.
     *
     * @return the area id
     */
    public AreaId areaId() {
        return areaId;
    }

    /**
     * Chunk index vector2.
     *
     * @return the vector2 of the chunk index
     */
    public IVector2 chunkIndex() {
        return chunkIndex;
    }

    /**
     * Is listener for boolean.
     *
     * @param eventType the event type
     * @return is listening for
     */
    public boolean isListenerFor(EventType eventType) {
        return listeningFor.get(eventType.ordinal());
    }

    /**
     * Had attached component boolean.
     *
     * @param componentType the component type
     * @return is attached
     */
    public boolean hasAttachedComponent(ComponentType componentType) {
        return attachedComponents.get(componentType.ordinal());
    }

    /**
     * Gets events being listened for.
     *
     * @return the listening for
     */
    public List<EventType> getListeningFor() {
        return Arrays.stream(EventType.values()).filter(e -> listeningFor.get(e.ordinal())).toList();
    }

    /**
     * Gets attached components.
     *
     * @return the attached components
     */
    public List<ComponentType> getAttachedComponents() {
        return Arrays.stream(ComponentType.values()).filter(e -> attachedComponents.get(e.ordinal())).toList();
    }

    /**
     * Gets a list of components matching passed componentType.
     *
     * @param componentType the component type
     * @return the components found
     */
    public List<Component<?>> getComponent(ComponentType componentType) {
        return componentList.stream().filter(c -> c.componentType() == componentType).toList();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Entity: ");
        sb.append("\n  entityType: ").append(entityType);
        sb.append(",\n  id: ").append(id);
        sb.append("\n");
        return sb.toString();
    }

    public void registerComponents(CoreSystem systemRegistry) {
        systemRegistry.registerComponents(componentList);
    }


}
