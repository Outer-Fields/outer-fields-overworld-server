package io.mindspce.outerfieldsserver.entities;

import io.mindspce.outerfieldsserver.components.monitors.AreaMonitor;
import io.mindspce.outerfieldsserver.enums.AreaId;
import io.mindspce.outerfieldsserver.enums.EntityType;
import io.mindspice.mindlib.data.geometry.IRect2;
import io.mindspice.mindlib.data.geometry.IVector2;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class LocationEntity extends PositionalEntity {
    private final long key;
    private final boolean enterable;
    private volatile boolean accessible;
    private final List<Integer> authorizedEntities;
    private final AreaId enterableAreaId;
    private final long enterableAreaKey;

    public LocationEntity(int id, AreaId areaId, IVector2 position, int key) {
        super(id, EntityType.LOCATION, areaId, position);
        this.key = key;
        this.enterable = false;
        this.accessible = false;
        this.enterableAreaId = AreaId.NONE;
        this.enterableAreaKey = -1;
        authorizedEntities = null;
    }

    public LocationEntity(int id, AreaId areaId, IVector2 position, int key, boolean accessible,
            List<Integer> authorizedEntities, AreaId enterableAreaId, long enterableAreaKey) {
        super(id, EntityType.LOCATION, areaId, position);
        this.key = key;
        this.enterable = true;
        this.accessible = accessible;
        this.authorizedEntities = new CopyOnWriteArrayList<>(authorizedEntities);

        if (enterableAreaKey > 0) {
            this.enterableAreaId = AreaId.AREA_KEY;
            this.enterableAreaKey = enterableAreaKey;
        } else {
            this.enterableAreaId = enterableAreaId;
            this.enterableAreaKey = -1;
        }

    }

    public LocationEntity withAreaMonitor(IRect2 monitorArea) {
        AreaMonitor areaMonitor = new AreaMonitor(this, monitorArea);
        addComponent(areaMonitor);
        return this;
    }

    public long key() { return key; }

    public boolean enterable() { return enterable; }

    public boolean accessible() { return accessible; }

    public List<Integer> authorizedEntities() { return Collections.unmodifiableList(authorizedEntities); }

    public AreaId enterableAreaId() { return enterableAreaId; }

    public long enterableAreaKey() { return enterableAreaKey; }

    public boolean isEnterableKey() {
        return enterableAreaKey > 0;
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
        authorizedEntities.removeAll(entities);
    }

    public void setAccessible(boolean accessible) {
        this.accessible = accessible;
    }

}
