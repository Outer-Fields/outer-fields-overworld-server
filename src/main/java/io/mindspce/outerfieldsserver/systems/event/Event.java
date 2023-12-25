package io.mindspce.outerfieldsserver.systems.event;

import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.enums.AreaId;
import io.mindspce.outerfieldsserver.enums.EntityType;
import io.mindspice.mindlib.data.geometry.IVector2;


public class Event<T> {
    private final AreaId areaId;
    private final IVector2 chunkId;
    private final int issuerId;
    private final int recipientId;
    private final EventType eventType;
    private final EntityType entityType;
    private final Entity entity;
    private final T data;

    public Event(EventType eventType, EntityType entityType, Entity entity, T eventData) {
        areaId = entity.currentArea();
        chunkId = IVector2.of(entity.chunkIndex());
        issuerId = entity.id();
        recipientId = -1;
        this.eventType = eventType;
        this.entityType = entityType;
        this.entity = entity;
        this.data = eventData;

    }

    public T data() {
        return data;
    }

    public EventType eventType() {
        return eventType;
    }

    public EntityType entityType() {
        return entityType;
    }

    public int issuerId() {
        return issuerId;
    }

    public int recipientId() {
        return recipientId;
    }

    public AreaId areaId() {
        return areaId;
    }

    public IVector2 chunkId() {
        return chunkId;
    }

    public Entity entity() {
        return entity;
    }

//    public static Event toRecipient(int recipientId, int issuerId, EventType eventType, EntityType entityType) {
//        return new Event(EventDomain.DIRECT, eventType, entityType, issuerId, recipientId);
//    }
//
//    public static Event toGlobal(int issuerId, EventType eventType, EntityType entityType) {
//        return new Event(EventDomain.GLOBAL, eventType, entityType, issuerId, -1);
//    }
//
//    public static Event toSelf(int issuerId, EventType eventType) {
//        return new Event(EventDomain.SELF, eventType, null, issuerId, issuerId);
//    }
//
//    public static Event of(int issuerId, EventType eventType, EntityType entityType) {
//        return new Event(eventType.domain, eventType, entityType, issuerId, -1);
//    }

}
