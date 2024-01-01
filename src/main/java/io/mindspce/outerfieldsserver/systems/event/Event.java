package io.mindspce.outerfieldsserver.systems.event;

import io.mindspce.outerfieldsserver.components.Component;
import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.enums.AreaId;
import io.mindspce.outerfieldsserver.enums.EntityType;
import io.mindspce.outerfieldsserver.systems.EventData;
import io.mindspice.mindlib.data.geometry.IRect2;

import java.util.List;


public record Event<T>(
        EventType eventType,
        AreaId eventArea,
        int issuerEntityId,
        long issuerComponentId,
        EntityType issuerEntityType,
        int recipientEntityId,
        long recipientComponentId,
        T data
) {
    public Event(EventType eventType, AreaId eventArea, Component<?> component, T data) {
        this(
                eventType,
                eventArea,
                component.entityId(),
                component.componentId(),
                component.entityType(),
                -1,
                -1,
                data);
    }

    public Event(EventType eventType, AreaId eventArea, Component<?> component, int recipientEntityId, T data) {
        this(
                eventType,
                eventArea,
                component.entityId(),
                component.componentId(),
                component.entityType(),
                recipientEntityId,
                -1,
                data);
    }

    public Event(EventType eventType, AreaId eventArea, Component<?> component, int recipientEntityId, long recipientComponentId, T data) {
        this(
                eventType,
                eventArea,
                component.entityId(),
                component.componentId(),
                component.entityType(),
                recipientEntityId,
                recipientComponentId,
                data);
    }

    public boolean isDirect() {
        return recipientEntityId != -1;
    }

    public boolean isDirectComponent() {
        return recipientComponentId != -1;
    }

    public static class Factory {

        public static Event<EventData.AreaEntered> newAreaEntered(Component<?> component, EventData.AreaEntered data) {
            return new Event<>(EventType.AREA_MONITORED_ENTERED, component.areaId(), component, data);
        }

        public static Event<EventData.AreaEntered> newEntityViewRectEntered(Component<?> component, EventData.AreaEntered data) {
            return new Event<>(EventType.ENTITY_VIEW_RECT_ENTERED, component.areaId(), component, data);
        }

        public static Event<EventData.EntityPositionChanged> newEntityPosition(Component<?> component,
                EventData.EntityPositionChanged position) {
            return new Event<>(EventType.ENTITY_POSITION_CHANGED, component.areaId(), component, position);
        }

        public static List<Event<?>> newEntityAreaChanged(Component<?> component, EventData.EntityAreaChanged data) {
            return List.of(
                    new Event<>(EventType.ENTITY_AREA_CHANGED, data.oldArea(), component, data),
                    new Event<>(EventType.ENTITY_AREA_CHANGED, data.newArea(), component, data));
        }

        public static Event<EventData.EntityChunkChanged> newEntityChunkUpdate(Component<?> component,
                EventData.EntityChunkChanged entityChunkChanged) {
            return new Event<>(EventType.ENTITY_AREA_CHANGED, component.areaId(), component, entityChunkChanged);
        }

        public static Event<EventData.NewEntity> newEntity(Component<?> component, EventData.NewEntity entityData) {
            return new Event<>(EventType.NEW_ENTITY, component.areaId(), component, entityData);
        }

        public static Event<IRect2> newEntityViewRectChanged(Component<?> component, IRect2 viewRect) {
            return new Event<>(EventType.ENTITY_VIEW_RECT_CHANGED, component.areaId(), component, viewRect);
        }

        public static Event<EventData.Query<?, ?, ?>> newQuery(Component<?> component,
                EventData.Query<?, ?, ?> queryCallBack, int entityId, long componentId) {
            return new Event<>(EventType.QUERY, AreaId.GLOBAL, component, entityId, componentId, queryCallBack);
        }

        public static Event<EventData.QueryResponse<?, ?>> newQueryResponse(Component<?> component,
                EventData.QueryResponse<?, ?> response, int entityId, long componentId) {
            return new Event<>(EventType.QUERY_RESPONSE, component.areaId(), component, entityId, componentId, response);
        }

        public static Event<EventData.CollisionData> newCollisionUpdate(AreaId eventArea,
                Component<?> component,
                EventData.CollisionData data) {
            return new Event<>(EventType.COLLISION_UPDATE, eventArea, component, data);
        }

        public static Event<EventData.CollisionData> newCollisionChange(Component<?> component, EventData.CollisionData data) {
            return new Event<>(EventType.COLLISION_CHANGE, component.areaId(), component, data);
        }
    }

}
