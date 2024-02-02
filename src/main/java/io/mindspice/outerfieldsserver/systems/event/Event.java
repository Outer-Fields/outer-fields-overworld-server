package io.mindspice.outerfieldsserver.systems.event;

import io.mindspice.outerfieldsserver.combat.schema.websocket.incoming.NetCombatAction;
import io.mindspice.outerfieldsserver.components.Component;
import io.mindspice.outerfieldsserver.core.networking.incoming.NetInPlayerAction;
import io.mindspice.outerfieldsserver.core.networking.incoming.NetInPlayerPosition;
import io.mindspice.outerfieldsserver.core.singletons.EntityManager;
import io.mindspice.outerfieldsserver.entities.*;
import io.mindspice.outerfieldsserver.enums.*;
import io.mindspice.mindlib.data.collections.lists.primative.IntList;
import io.mindspice.mindlib.data.geometry.IRect2;
import io.mindspice.mindlib.data.geometry.IVector2;
import io.mindspice.mindlib.data.tuples.Pair;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntPredicate;


public record Event<T>(
        EventType eventType,
        AreaId eventArea,
        int issuerEntityId,
        long issuerComponentId,
        ComponentType issuerCompType,
        EntityType issuerEntityType,
        int recipientEntityId,
        long recipientComponentId,
        ComponentType recipientCompType,
        T data
) {

    public Event { // TODO FIXME remove this for prod
        if (!eventType.validate(data)) {
            throw new RuntimeException("invalid data, expected: " + eventType.dataClass + " got: " + data.getClass());
        }
    }

    public Event(EventType eventType, AreaId eventArea, Component<?> component, T data) {
        this(
                eventType,
                eventArea,
                component.entityId(),
                component.componentId(),
                component.componentType(),
                component.entityType(),
                -1,
                -1,
                ComponentType.ANY,
                data);
    }

    public Event(EventType eventType, AreaId eventArea, Component<?> component, int recipientEntityId, T data) {
        this(
                eventType,
                eventArea,
                component.entityId(),
                component.componentId(),
                component.componentType(),
                component.entityType(),
                recipientEntityId,
                -1,
                ComponentType.ANY,
                data);
    }

    public Event(EventType eventType, AreaId eventArea, Component<?> component, int recipientEntityId,
            long recipientComponentId, T data) {
        this(
                eventType,
                eventArea,
                component.entityId(),
                component.componentId(),
                component.componentType(),
                component.entityType(),
                recipientEntityId,
                recipientComponentId,
                ComponentType.ANY,
                data);
    }

    public Event(EventType eventType, AreaId eventArea, Component<?> component, int recipientEntityId,
            long recipientComponentId, ComponentType recCompType, T data) {
        this(
                eventType,
                eventArea,
                component.entityId(),
                component.componentId(),
                component.componentType(),
                component.entityType(),
                recipientEntityId,
                recipientComponentId,
                recCompType,
                data);
    }

    public Event(EventType eventType, AreaId eventArea, Component<?> component, ComponentType recCompType, T data) {
        this(
                eventType,
                eventArea,
                component.entityId(),
                component.componentId(),
                component.componentType(),
                component.entityType(),
                -1,
                -1,
                recCompType,
                data);
    }

    public boolean isDirect() {
        return recipientEntityId != -1;
    }

    public boolean isDirectComponent() {
        return recipientComponentId != -1;
    }

    public boolean isDirectComponentType() {
        return recipientCompType != ComponentType.ANY;
    }

    public static <T> Builder<T> builder(EventType eventType, Component<?> issuingComponent) {
        return new Builder<>(eventType, issuingComponent);
    }

    public static <T> Builder<T> builder(EventType eventType) {
        return new Builder<>(eventType);
    }

    public static <U> Event<U> responseEvent(Component<?> component, Event<?> ogEvent, EventType respEventType, U respData) {
        return new Event<>(
                respEventType,
                ogEvent.eventArea,
                component.entityId(),
                component.componentId(),
                component.componentType(),
                component.entityType(),
                ogEvent.issuerEntityId,
                ogEvent.issuerComponentId(),
                ogEvent.issuerCompType(),
                respData
        );
    }

    public static <E, V> Event<EventData.CompletableEvent<E, V>> completableEvent(Component<?> component,
            Event<E> mainEvent, Event<V> completionEvent) {
        return new Event<>(
                EventType.COMPLETABLE_EVENT, AreaId.GLOBAL, component, new EventData.CompletableEvent<>(mainEvent, completionEvent)
        );
    }

    // Simple Static Factory to enforce proper creation parameters are used

    // GENERAL

    public static Event<Boolean> updateListening(int entityId, ComponentType componentType, boolean isEnable) {
        return new Event<>(EventType.UPDATE_LISTENING, AreaId.GLOBAL, -1, -1, ComponentType.ANY,
                EntityType.ANY, entityId, -1, componentType, isEnable);
    }

    public static <T> Event<T> nonComponentEvent(EventType eventType, int entityId, T data) {
        return new Event<>(eventType, AreaId.GLOBAL, -1, -1,
                ComponentType.ANY, EntityType.ANY, entityId, -1, ComponentType.ANY, data);
    }

    public static <T> Event<T> nonComponentEvent(EventType eventType, AreaId area, int entityId, T data) {
        return new Event<>(eventType, area, -1, -1,
                ComponentType.ANY, EntityType.ANY, entityId, -1, ComponentType.ANY, data);
    }

    public static <T> Event<T> fromShell(EventType eventType, ComponentType recipientCompType, int recipId, T data) {
        ShellEntity shell = EntityManager.GET().getShellEntity();
        return new Event<>(eventType, AreaId.GLOBAL, shell.entityId(),
                shell.getComponent(ComponentType.SIMPLER_LISTENER).componentId(),
                ComponentType.ANY, EntityType.ANY, recipId, -1, recipientCompType, data);
    }

    public static <T> Event<T> fromShell(EventType eventType, ComponentType recipientCompType,
            AreaId areaid, int recipId, T data) {
        ShellEntity shell = EntityManager.GET().getShellEntity();
        return new Event<>(eventType, areaid, shell.entityId(),
                shell.getComponent(ComponentType.SIMPLER_LISTENER).componentId(),
                ComponentType.ANY, EntityType.ANY, recipId, -1, recipientCompType, data);

    }

    public static <T> Event<T> toShell(Component<?> component, EventType eventType, T data) {
        ShellEntity shell = EntityManager.GET().getShellEntity();
        return new Event<>(eventType, component.areaId(), component, shell.entityId(),
                shell.getComponent(ComponentType.SIMPLER_LISTENER).componentId(), ComponentType.ANY, data);
    }

    public static Event<EventData.NewPositionalEntity> newPositionalEntity(EventData.NewPositionalEntity newPositionalEntityData) {
        return new Event<>(EventType.NEW_POSITIONAL_ENTITY, newPositionalEntityData.area(), -1, -1, ComponentType.ANY,
                newPositionalEntityData.entity().entityType(), -1, -1, ComponentType.ANY, newPositionalEntityData);
    }

    public static Event<Consumer<?>> directComponentCallback(Component<?> component, AreaId areaId, ComponentType recCompType,
            int recEntId, long recCompId, Consumer<?> callBack) {
        return new Event<>(EventType.CALLBACK, areaId, component, recEntId, recCompId, recCompType, callBack);
    }

    public static Event<Consumer<?>> directEntityCallback(Component<?> component, AreaId areaId, int recEntId,
            ComponentType compType, Consumer<?> Callback) {
        return new Event<>(EventType.CALLBACK, areaId, component, recEntId, -1, compType, Callback);
    }

    public static Event<Consumer<?>> directEntityCallback(AreaId areaId, int recEntId, ComponentType compType, Consumer<?> Callback) {
        return new Event<>(EventType.CALLBACK, areaId, -1, -1,
                ComponentType.ANY, EntityType.ANY, recEntId, -1, compType, Callback);
    }

    public static Event<Consumer<?>> directEntityCallback(int recEntId, ComponentType compType, Consumer<?> Callback) {
        return new Event<>(EventType.CALLBACK, AreaId.GLOBAL, -1, -1,
                ComponentType.ANY, EntityType.ANY, recEntId, -1, compType, Callback);
    }

    public static Event<Entity> systemRegisterEntity(Entity entity) {
        return new Event<>(EventType.SYSTEM_REGISTER_ENTITY, AreaId.NONE, -1, -1, ComponentType.ANY,
                EntityType.ANY, -1, -1, ComponentType.ANY, entity);
    }

    public static void emitAndRegisterPositionalEntity(SystemType systemType, AreaId currArea, IVector2 currPos, Entity entity) {
        EntityManager.GET().emitEvent(Event.newPositionalEntity(new EventData.NewPositionalEntity(currArea, currPos, entity)));
        EntityManager.GET().emitEventToSystem(systemType, Event.systemRegisterEntity(entity));

    }

    // NETWORK

    public static Event<NetInPlayerPosition> netInPlayerPosition(int recipientId, NetInPlayerPosition data) {
        return new Event<>(
                EventType.NETWORK_IN_PLAYER_POSITION, AreaId.GLOBAL, -1, -1, ComponentType.ANY,
                EntityType.ANY, recipientId, -1, ComponentType.NET_PLAYER_POSITION, data);
    }

    public static Event<List<NetInPlayerAction>> netInPlayerAction(int recipientId, List<NetInPlayerAction> data) {
        return new Event<>(
                EventType.NETWORK_IN_PLAYER_ACTION, AreaId.GLOBAL, -1, -1, ComponentType.ANY,
                EntityType.ANY, recipientId, -1, ComponentType.NET_PLAYER_POSITION, data);
    }

    // This uses playerId vs entityId for recipient addressing
    public static Event<NetCombatAction> netInCombatAction(int recipientId, NetCombatAction data) {
        return new Event<>(
                EventType.NETWORK_IN_COMBAT_ACTION, AreaId.GLOBAL, -1, -1, ComponentType.ANY,
                EntityType.ANY, recipientId, -1, ComponentType.NET_PLAYER_POSITION, data);
    }

    // CHARACTER

    public static Event<EventData.CharacterDeath> characterDeath(AreaId area, EventData.CharacterDeath characterDeath) {
        return new Event<>(EventType.CHARACTER_DEATH, area, -1, -1, ComponentType.ANY,
                EntityType.ANY, -1, -1, ComponentType.ANY, characterDeath);
    }

    // SPECIFIC EVENTS

    public static Event<EventData.AreaEntered> areaEntered(Component<?> component, EventData.AreaEntered data) {
        return new Event<>(EventType.AREA_MONITOR_ENTERED, component.areaId(), component, data);
    }

    public static Event<Integer> entityViewRectEntered(Component<?> component, Integer data) {
        return new Event<>(EventType.ENTITY_VIEW_RECT_ENTERED, component.areaId(), component, data);
    }

    public static Event<Integer> entityViewRectExited(Component<?> component, Integer data) {
        return new Event<>(EventType.Entity_VIEW_RECT_EXITED, component.areaId(), component, data);
    }

    public static Event<EventData.EntityPositionChanged> entityPosition(Component<?> component,
            EventData.EntityPositionChanged position) {
        return new Event<>(EventType.ENTITY_POSITION_CHANGED, component.areaId(), component, position);
    }

    public static Event<IVector2> entityPositionUpdate(Component<?> component, int entityId, IVector2 data) {
        return new Event<>(EventType.ENTITY_POSITION_UPDATE, component.areaId(), component, entityId, data);
    }

    public static Event<IVector2> entityPositionUpdate(int entityId, IVector2 data) {
        return new Event<>(EventType.ENTITY_POSITION_UPDATE, AreaId.GLOBAL, -1, -1, ComponentType.ANY,
                EntityType.ANY, entityId, -1, ComponentType.ANY, data);
    }

    public static Event<IVector2> playerValidMovement(Component<?> component, IVector2 position) {
        return new Event<>(EventType.PLAYER_VALID_MOVEMENT, component.areaId(), component, position);
    }

    public static List<Event<?>> entityAreaChanged(Component<?> component, EventData.EntityAreaChanged data) {
        return List.of(
                new Event<>(EventType.ENTITY_AREA_CHANGED, data.oldArea(), component, data),
                new Event<>(EventType.ENTITY_AREA_CHANGED, data.newArea(), component, data));
    }

    public static Event<EventData.EntityChunkChanged> entityChunkUpdate(Component<?> component,
            EventData.EntityChunkChanged entityChunkChanged) {
        return new Event<>(EventType.ENTITY_AREA_CHANGED, component.areaId(), component, entityChunkChanged);
    }

    public static Event<IRect2> entityViewRectChanged(Component<?> component, IRect2 viewRect) {
        return new Event<>(EventType.ENTITY_VIEW_RECT_CHANGED, component.areaId(), component, viewRect);
    }

    public static Event<EventData.CollisionData> collisionUpdate(AreaId eventArea, Component<?> component,
            EventData.CollisionData data) {
        return new Event<>(EventType.COLLISION_UPDATE, eventArea, component, data);
    }

    public static Event<EventData.CollisionData> collisionChange(Component<?> component, EventData.CollisionData data) {
        return new Event<>(EventType.COLLISION_CHANGE, component.areaId(), component, data);
    }

    public static Event<Consumer<?>> globalComponentCallback(Component<?> component, AreaId areaId,
            ComponentType recipientCompType, Consumer<?> callBack) {
        return new Event<>(EventType.CALLBACK, areaId, component, -1, -1, recipientCompType, callBack);
    }

    public static Event<EventData.EntityStateUpdate> entityStateUpdate(Component<?> component, AreaId areaId,
            EventData.EntityStateUpdate data) {
        return new Event<>(EventType.ENTITY_STATE_UPDATE, areaId, component, data);
    }

    public static Event<List<EntityState>> entityStateChanged(Component<?> component, List<EntityState> data) {
        return new Event<>(EventType.ENTITY_STATE_CHANGED, component.areaId(), component, data);
    }

    public static Event<String> entityNameChange(Component<?> component, String name) {
        return new Event<>(EventType.ENTITY_PROPERTY_CHANGE, component.areaId(), component, name);
    }

    public static Event<Integer> serializedEntityRequest(Component<?> component, AreaId areaId, int entityId) {
        return new Event<>(EventType.SERIALIZED_ENTITY_REQUEST, areaId, component, entityId, -1, ComponentType.ANY, entityId);
    }

    public static Event<IRect2> entityGridQuery(Component<?> component, AreaId areaId,
            int entityId, IRect2 queryRect) {
        return new Event<>(EventType.ENTITY_GRID_QUERY, areaId, component, entityId, -1, ComponentType.ACTIVE_ENTITIES, queryRect);
    }

    public static Event<IntPredicate> serializedEntitiesReq(Component<?> component,
            AreaId areaId, IntPredicate predicate) {
        return new Event<>(EventType.SERIALIZED_ENTITIES_REQUEST, areaId, component, predicate);
    }

    public static Event<ClothingItem[]> characterOutFitChanges(Component<?> component, ClothingItem[] data) {
        return new Event<>(EventType.CHARACTER_OUTFIT_CHANGED, component.areaId(), component, data);
    }

    public static Event<List<Pair<IVector2, Integer>>> areaMonitorContainsEntities(Component<?> component, AreaId queryArea,
            List<Pair<IVector2, Integer>> queryData) {
        return new Event<>(EventType.AREA_MONITOR_QUERY, queryArea, component, queryData);
    }

    public static Event<List<Pair<IVector2, Integer>>> areaMonitorContainsEntities(Component<?> component, AreaId queryArea, int entityId,
            List<Pair<IVector2, Integer>> queryData) {
        return new Event<>(EventType.AREA_MONITOR_QUERY, queryArea, component, entityId, queryData);
    }

    public static Event<List<Pair<IVector2, Integer>>> areaMonitorContainsEntities(Component<?> component, AreaId queryArea,
            int entityId, ComponentType componentType, List<Pair<IVector2, Integer>> queryData) {
        return new Event<>(EventType.AREA_MONITOR_QUERY, queryArea, component, entityId, -1, componentType, queryData);
    }

    public static Event<List<Pair<IVector2, Integer>>> areaMonitorContainsEntities(Component<?> component, AreaId queryArea,
            long componentId, ComponentType componentType, List<Pair<IVector2, Integer>> queryData) {
        return new Event<>(EventType.AREA_MONITOR_QUERY, queryArea, component, -1, componentId, componentType, queryData);
    }

    public static Event<Pair<String, String>> entityPropertyUpdate(Component<?> component, AreaId areaId, int entityId,
            Pair<String, String> data) {
        return new Event<>(EventType.ENTITY_PROPERTY_UPDATE, areaId, component, entityId, data);
    }

    public static Event<Pair<String, String>> entityPropertyChanged(Component<?> component, AreaId areaId, Pair<String, String> data) {
        return new Event<>(EventType.ENTITY_PROPERTY_CHANGE, areaId, component, data);
    }

    public static Event<EventData.NPCLocationArrival> npcArrivedAtLocation(Component<?> component, AreaId areaId,
            EventData.NPCLocationArrival data) {
        return new Event<>(EventType.NPC_ARRIVED_AT_LOC, areaId, component, data);
    }

    public static Event<EventData.NPCTravelTo> npcTravelTo(Component<?> component, AreaId areaId,
            EventData.NPCTravelTo data) {
        return new Event<>(EventType.NPC_TRAVEL_TO, areaId, component, data);
    }

    public static Event<PlayerQuestEntity> newPlayerQuest(PlayerQuestEntity entity) {
        return new Event<>(EventType.QUEST_PLAYER_NEW, AreaId.NONE, -1, -1, ComponentType.ANY,
                EntityType.ANY, -1, -1, ComponentType.ANY, entity);
    }

    public static Event<WorldQuestEntity> newWorldQuest(WorldQuestEntity entity) {
        return new Event<>(EventType.QUEST_WORLD_NEW, AreaId.NONE, -1, -1, ComponentType.ANY,
                EntityType.ANY, -1, -1, ComponentType.ANY, entity);
    }

    public static Event<PlayerQuestEntity> questCompletedPlayer(PlayerQuestEntity quest) {
        return new Event<>(EventType.QUEST_COMPLETED_PLAYER, AreaId.NONE, -1, -1, ComponentType.QUEST_MODULE,
                EntityType.ANY, -1, -1, ComponentType.ANY, quest);
    }

    public static Event<WorldQuestEntity> questCompletedWorld(WorldQuestEntity quest) {
        return new Event<>(EventType.QUEST_COMPLETED_WORLD, AreaId.NONE, -1, -1, ComponentType.QUEST_MODULE,
                EntityType.ANY, -1, -1, ComponentType.ANY, quest);
    }

    public static Event<IntList> tileDataQuery(Component<?> component, AreaId area, IntList query) {
        return new Event<>(EventType.TILE_DATA_QUERY, area, component, query);
    }

    public static Event<EventData.CombatInit> combatInit(Component<?> component, AreaId areaId, EventData.CombatInit data, boolean isPVP) {
        return new Event<>(isPVP ? EventType.COMBAT_INIT_PVP : EventType.COMBAT_INIT_NPC, areaId,
                component, -1, -1, ComponentType.ANY, data);
    }

    public static Event<Boolean> entitySetActive(Component<?> component, AreaId areaId, int recipEntityId, boolean isActive) {
        return new Event<>(EventType.ENTITY_SET_ACTIVE, areaId, component, recipEntityId, isActive);
    }

    public static Event<Integer> entityVisibilityQuery(Component<?> component, AreaId areaId, int recipEntityId) {
        return new Event<>(EventType.ENTITY_VISIBILITY_QUERY, areaId, component, recipEntityId, recipEntityId);
    }

    public static Event<Integer> entityIsActiveQuery(Component<?> component, AreaId areaId, int recipEntityId) {
        return new Event<>(EventType.ENTITY_IS_ACTIVE_QUERY, areaId, component, recipEntityId, recipEntityId);
    }

    public static Event<Pair<Integer, Boolean>> entityIsActiveChanged(Component<?> component, AreaId areaId, Pair<Integer, Boolean> data) {
        return new Event<>(EventType.ENTITY_IS_ACTIVE_CHANGED, areaId, component, data);
    }

    public static Event<EventData.EntityVisibility> entityVisibilityChange(Component<?> comp, AreaId area,
            EventData.EntityVisibility data) {
        return new Event<>(EventType.ENTITY_VISIBILITY_CHANGED, area, comp, data);
    }

    public static Event<EventData.VisibilityUpdate> entityVisibilityUpdate(Component<?> comp, AreaId area,
            int recipEntityId, EventData.VisibilityUpdate data) {
        return new Event<>(EventType.ENTITY_VISIBILITY_CHANGED, area, comp, recipEntityId, data);
    }

    public static Event<Entity> destroyEntity(Entity entity){
        return new Event<>(EventType.ENTITY_DESTROY, entity.areaId(), -1, -1,
                ComponentType.ANY, EntityType.ANY, entity.entityId(), -1, ComponentType.ANY, entity);
    }

    // BUILDER


    public static class Builder<T> {
        EventType eventType;
        AreaId eventArea;
        int issuerEntityId = -1;
        long issuerComponentId = -1;
        ComponentType issuerCompType = ComponentType.ANY;
        EntityType issuerEntityType = EntityType.ANY;
        int recipientEntityId = -1;
        long recipientComponentId = -1;
        ComponentType recipientCompType = ComponentType.ANY;
        T data;

        public Builder(EventType eventType, Component<?> issuingComponent) {
            this.eventType = eventType;
            this.issuerEntityId = issuingComponent.entityId();
            this.issuerComponentId = issuingComponent.componentId();
            this.issuerCompType = issuingComponent.componentType();
        }

        public Builder(EventType eventType) {
            this.eventType = eventType;
        }

        public Builder<T> setEventArea(AreaId eventArea) {
            this.eventArea = eventArea;
            return this;
        }

        public Builder<T> setIssuerEntityId(int issuerEntityId) {
            this.issuerEntityId = issuerEntityId;
            return this;
        }

        public Builder<T> setIssuerComponentId(long issuerComponentId) {
            this.issuerComponentId = issuerComponentId;
            return this;
        }

        public Builder<T> setIssuerCompType(ComponentType issuerCompType) {
            this.issuerCompType = issuerCompType;
            return this;
        }

        public Builder<T> setIssuerEntityType(EntityType issuerEntityType) {
            this.issuerEntityType = issuerEntityType;
            return this;
        }

        public Builder<T> setRecipientEntityId(int recipientEntityId) {
            this.recipientEntityId = recipientEntityId;
            return this;
        }

        public Builder<T> setRecipientComponentId(long recipientComponentId) {
            this.recipientComponentId = recipientComponentId;
            return this;
        }

        public Builder<T> setRecipientCompType(ComponentType recipientCompType) {
            this.recipientCompType = recipientCompType;
            return this;
        }

        public Builder<T> setData(T data) {
            this.data = data;
            return this;
        }

        public Event<T> build() {
            if (data == null) {
                throw new IllegalStateException("Data must be set");
            }

            if (eventArea == null) {
                throw new IllegalStateException("Event area must be set");
            }

            if (eventType == EventType.CALLBACK) {
                if (recipientCompType == null) {
                    throw new IllegalStateException(("ComponentType must be set for callbacks"));
                }
            }

            if (!eventType.validate(data)) {
                throw new IllegalStateException("Data must match EventType. Expected: "
                        + eventType.dataClass + " Received: " + data.getClass());
            }

            return new Event<>(eventType, eventArea, issuerEntityId, issuerComponentId, issuerCompType, issuerEntityType,
                    recipientEntityId, recipientComponentId, recipientCompType, data);
        }
    }


}
