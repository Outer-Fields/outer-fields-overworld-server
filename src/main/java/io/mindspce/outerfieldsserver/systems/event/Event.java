package io.mindspce.outerfieldsserver.systems.event;

import io.mindspce.outerfieldsserver.core.singletons.EntityManager;
import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.enums.AreaId;
import io.mindspce.outerfieldsserver.enums.EntityType;
import io.mindspce.outerfieldsserver.systems.EventData;
import io.mindspice.mindlib.data.geometry.IVector2;


public class Event<T> {
    private final AreaId areaId;
    private final IVector2 chunkId;
    private final int issuerId;
    private final int recipientId;
    private final EventType eventType;
    private final EntityType entityType;
    private final T data;

    private Event(EventType eventType, Entity entity, T eventData) {
        areaId = entity.areaId();
        chunkId = entity.chunkIndex();
        issuerId = entity.id();
        recipientId = -1;
        this.eventType = eventType;
        this.entityType = entity.entityType();
        this.data = eventData;
    }

    private Event(EventType eventType, Entity entity, T eventData, int recipientId) {
        areaId = entity.areaId();
        chunkId = entity.chunkIndex();
        issuerId = entity.id();
        this.recipientId = -recipientId;
        this.eventType = eventType;
        this.entityType = entity.entityType();
        this.data = eventData;
    }

    public boolean isDirect() {
        return recipientId != 1;
    }

    public AreaId areaId() {
        return areaId;
    }

    public IVector2 chunkId() {
        return chunkId;
    }

    public int issuerId() {
        return issuerId;
    }

    public int recipientId() {
        return recipientId;
    }

    public EventType eventType() {
        return eventType;
    }

    public EntityType entityType() {
        return entityType;
    }

    public T data() {
        return data;
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


    public static class Emit {
        public static void newEntityPosition(Entity entity, EventData.PositionUpdate position) {
            EntityManager.GET().emitEvent(new Event<>(EventType.PLAYER_POSITION, entity, position));
        }

        public static void newMonitoredAreaEntered(Entity entity, EventData.AreaEntered areaEntered) {
            EntityManager.GET().emitEvent(new Event<>(EventType.MONITORED_AREA_ENTERED, entity, areaEntered));
        }

        public static void newEntityAreaUpdate(Entity entity, EventData.AreaUpdate areaUpdate) {
            EntityManager.GET().emitEvent(new Event<>(EventType.AREA_UPDATE, entity, areaUpdate));
        }

        public static void newEntityChunkUpdate(Entity entity, EventData.ChunkUpdate chunkUpdate) {
            EntityManager.GET().emitEvent(new Event<>(EventType.AREA_UPDATE, entity, chunkUpdate));
        }


    }

}
