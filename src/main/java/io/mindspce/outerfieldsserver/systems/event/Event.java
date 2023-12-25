package io.mindspce.outerfieldsserver.systems.event;

import io.mindspce.outerfieldsserver.enums.EntityType;


public class Event {
    int issuerId = -1;
    int recipientId = -1;
    EventDomain domain;
    EventType type;
    EntityType entityType;

    private Event(EventDomain domain, EventType type, EntityType entityType, int issuerId, int recipientId) {
        this.domain = domain;
        this.type = type;
        this.entityType = entityType;
        this.issuerId = issuerId;
        this.recipientId = recipientId;
    }

    public EventDomain domain() {
        return domain;
    }

    public EventType type() {
        return type;
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

    public static Event toRecipient(int recipientId, int issuerId, EventType eventType, EntityType entityType) {
        return new Event(EventDomain.DIRECT, eventType, entityType, issuerId, recipientId);
    }

    public static Event toGlobal(int issuerId, EventType eventType, EntityType entityType) {
        return new Event(EventDomain.GLOBAL, eventType, entityType, issuerId, -1);
    }

    public static Event toSelf(int issuerId, EventType eventType) {
        return new Event(EventDomain.SELF, eventType, null, issuerId, issuerId);
    }

    public static Event of(int issuerId, EventType eventType, EntityType entityType) {
        return new Event(eventType.domain, eventType, entityType, issuerId, -1);
    }

}
