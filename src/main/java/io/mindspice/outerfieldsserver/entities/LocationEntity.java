package io.mindspice.outerfieldsserver.entities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.mindspice.outerfieldsserver.components.monitors.AreaMonitor;
import io.mindspice.outerfieldsserver.enums.AreaId;
import io.mindspice.outerfieldsserver.enums.EntityType;
import io.mindspice.mindlib.data.geometry.IRect2;
import io.mindspice.mindlib.data.geometry.IVector2;
import io.mindspice.mindlib.util.JsonUtils;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;


public class LocationEntity extends PositionalEntity {
    private final long key;
    private final boolean enterable;
    private volatile boolean accessible;
    private final Set<Integer> authorizedEntities;
    private final AreaId enterableAreaId;
    private final long enterableBuildingKey;
    private final IRect2 enterTriggerRect;

    public LocationEntity(int id, AreaId areaId, IVector2 position, IRect2 enterTriggerRect, int key) {
        super(id, EntityType.LOCATION, areaId, position);
        this.key = key;
        this.enterable = false;
        this.accessible = false;
        this.enterableAreaId = AreaId.NONE;
        this.enterableBuildingKey = -1;
        authorizedEntities = null;
        this.enterTriggerRect = enterTriggerRect;

    }

    public LocationEntity(int id, AreaId areaId, IVector2 position, IRect2 enterTriggerRect, int key, boolean accessible,
            List<Integer> authorizedEntities, AreaId enterableAreaId, long enterableBuildingKey) {
        super(id, EntityType.LOCATION, areaId, position);
        this.key = key;
        this.enterable = true;
        this.accessible = accessible;
        this.authorizedEntities = new CopyOnWriteArraySet<>(authorizedEntities);

        if (enterableBuildingKey > 0) {
            this.enterableAreaId = AreaId.AREA_KEY;
            this.enterableBuildingKey = enterableBuildingKey;
        } else {
            this.enterableAreaId = enterableAreaId;
            this.enterableBuildingKey = -1;
        }
        this.enterTriggerRect = enterTriggerRect;

    }

    public long key() { return key; }

    public boolean enterable() { return enterable; }

    public boolean accessible() { return accessible; }

    public Set<Integer> authorizedEntities() { return Collections.unmodifiableSet(authorizedEntities); }

    public AreaId enterableAreaId() { return enterableAreaId; }

    public long enterableBuildingKey() { return enterableBuildingKey; }

    public boolean isEnterableBuilding() {
        return enterableBuildingKey > 0;
    }

    public boolean isEnterableArea() {
        return enterableAreaId != AreaId.NONE;
    }

    public void addAuthorizedEntity(int id) {
        authorizedEntities.add(id);
    }

    public void addAuthorizedEntities(List<Integer> ids) {
        this.authorizedEntities.addAll(ids);
    }

    public void removeAuthorizedEntity(int id) {
        authorizedEntities.remove(id);
    }

    public void removeAuthorizedEntities(List<Integer> entities) {
        entities.forEach(authorizedEntities::remove);
    }

    public void setAccessible(boolean accessible) {
        this.accessible = accessible;
    }

    public IRect2 enterTriggerRect() {
        return enterTriggerRect;
    }

    public boolean isInEnterTrigger(IVector2 position) {
        return enterTriggerRect.contains(position);
    }

    public boolean canEntityEnter(int entityId, IVector2 entityPosition) {
        if (authorizedEntities != null && !authorizedEntities.isEmpty()) {
            if (!authorizedEntities.contains(entityId)) {
                return false;
            }
        }
        return enterTriggerRect.contains(entityPosition);
    }

    public String toString() {
        JsonNode node = new JsonUtils.ObjectBuilder()
                .put("entityType", entityType)
                .put("entityId", id)
                .put("locationKey", key)
                .put("enterable", enterable)
                .put("accessible", accessible)
                .put("authorizedEntities", authorizedEntities)
                .put("enterableAreaId", enterableAreaId)
                .put("enterableAreaKey", enterableBuildingKey)
                .put("name", name)
                .put("areaId", areaId)
                .put("chunkIndex", chunkIndex)
                .put("attachComponents", getAttachedComponentTypes())
                .put("listeningFor", listeningForTypes())
                .put("systemRegistry", systemRegistry.systemType())
                .buildNode();
        try {
            return JsonUtils.writePretty(node);
        } catch (JsonProcessingException e) {
            return "Error serializing to string";
        }
    }

}
