package io.mindspce.outerfieldsserver.systems.event;

import io.mindspce.outerfieldsserver.core.Tick;
import io.mindspce.outerfieldsserver.core.networking.incoming.NetInPlayerPosition;
import io.mindspce.outerfieldsserver.core.networking.proto.EntityProto;
import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.entities.LocationEntity;
import io.mindspce.outerfieldsserver.entities.PlayerQuestEntity;
import io.mindspce.outerfieldsserver.entities.WorldQuestEntity;
import io.mindspce.outerfieldsserver.enums.AreaId;
import io.mindspce.outerfieldsserver.enums.ClothingItem;
import io.mindspce.outerfieldsserver.systems.EventData;
import io.mindspce.outerfieldsserver.ai.task.Task;
import io.mindspice.mindlib.data.geometry.IRect2;
import io.mindspice.mindlib.data.geometry.IVector2;
import io.mindspice.mindlib.data.tuples.Pair;
import jakarta.annotation.Nullable;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;


public enum EventType {
    // TESTING
    PING(Object.class, Objects::nonNull),
    PONG(Object.class, Objects::nonNull),
    // GENERAL EVENTS
    ANY(Object.class, Objects::nonNull),
    TICK(Tick.class, x -> x instanceof Tick),
    CALLBACK(Consumer.class, x -> x instanceof Consumer<?>),
    COMPLETABLE_EVENT(EventData.CompletableEvent.class, x -> x instanceof EventData.CompletableEvent<?, ?>),

    //NETWORK IN EVENTS
    NETWORK_IN_PLAYER_POSITION(NetInPlayerPosition.class, x -> x instanceof NetInPlayerPosition),

    //NETWORK OUT EVENTS
    NETWORK_OUT_ENTITY_UPDATE(byte[].class, x -> x instanceof byte[]),
    NETWORK_PLAYER_RECONNECT(WebSocketSession.class, x -> x instanceof WebSocketSession),

    // ENTITY CHANGE EVENTS
    ENTITY_POSITION_CHANGED(EventData.EntityPositionChanged.class, x -> x instanceof EventData.EntityPositionChanged),
    ENTITY_VIEW_RECT_ENTERED(Integer.class, x -> x instanceof Integer),
    Entity_VIEW_RECT_EXITED(Integer.class, x -> x instanceof Integer),
    NEW_ENTITY(EventData.NewEntity.class, x -> x instanceof EventData.NewEntity),
    ENTITY_VIEW_RECT_CHANGED(IRect2.class, x -> x instanceof IRect2),
    ENTITY_CHUNK_CHANGED(EventData.EntityChunkChanged.class, x -> x instanceof EventData.EntityChunkChanged),
    ENTITY_AREA_CHANGED(EventData.EntityAreaChanged.class, x -> x instanceof EventData.EntityAreaChanged),
    ENTITY_STATE_CHANGED(List.class, x -> x instanceof List<?>),
    ENTITY_PROPERTY_CHANGE(Pair.class, x -> x instanceof Pair),
    ENTITY_PROPERTY_UPDATE(Pair.class, x -> x instanceof Pair),

    // ENTITY UPDATE EVENTS
    ENTITY_AREA_UPDATE(AreaId.class, x -> x instanceof AreaId),
    ENTITY_NAME_UPDATE(String.class, x -> x instanceof String),
    ENTITY_STATE_UPDATE(EventData.EntityStateUpdate.class, x -> x instanceof EventData.EntityStateUpdate),
    ENTITY_POSITION_UPDATE(IVector2.class, x -> x instanceof IVector2),

    // PLAYER SUBSYSTEM EVENTS
    PLAYER_VALID_MOVEMENT(IVector2.class, x -> x instanceof IVector2),
    PLAYER_INVALID_MOVEMENT(EventData.EntityPositionChanged.class, x -> x instanceof EventData.EntityPositionChanged),

    // OTHER COMPONENT SPECIFIC EVENTS

    TILE_DATA_UPDATE(EventData.TileDataUpdate.class, x -> x instanceof EventData.TileDataUpdate),
    COLLISION_CHANGE(EventData.CollisionData.class, x -> x instanceof EventData.CollisionData),
    COLLISION_UPDATE(EventData.CollisionData.class, x -> x instanceof EventData.CollisionData),

    ENTITY_GRID_QUERY(IRect2.class, x -> x instanceof IRect2),
    ENTITY_GRID_RESPONSE(int[].class, x -> x instanceof int[]),

    // Serialization
    SERIALIZED_ENTITY_REQUEST(Integer.class, x -> x instanceof Integer),
    SERIALIZED_ENTITY_RESP(byte[].class, x -> x instanceof byte[]),
    SERIALIZED_ENTITIES_REQUEST(Predicate.class, x -> x instanceof Predicate<?>),
    SERIALIZED_ENTITIES_RESP(Pair.class, x -> x instanceof Pair<?, ?>),
    SERIALIZED_CHARACTER_RESP(EntityProto.CharacterEntity.class, x -> x instanceof EntityProto.CharacterEntity),
    SERIALIZED_LOC_ITEM_RESP(EntityProto.LocationItemEntity.class, x -> x instanceof EntityProto.LocationItemEntity),

    // CHARACTER
    CHARACTER_OUTFIT_CHANGED(ClothingItem.class, x -> x instanceof ClothingItem[]),
    CHARACTER_OUTFIT_UPDATE(byte[].class, x -> x instanceof byte[]),

    // AREA
    AREA_MONITOR_QUERY(List.class, x -> x instanceof List), //List<Pair<IVector2,Integer>
    AREA_MONITOR_RESP(List.class, x -> x instanceof List), //List<Integer>
    AREA_MONITOR_ENTERED(EventData.AreaEntered.class, x -> x instanceof EventData.AreaEntered),
    AREA_ENTITIES_QUERY(AreaId.class, x -> x instanceof AreaId),
    AREA_ENTITIES_RESPONSE(List.class, x -> x instanceof List<?>),

    // LOCATION
    LOCATION_NEW(LocationEntity.class, x -> x instanceof LocationEntity),
    LOCATION_REMOVE(Integer.class, x -> x instanceof Integer),

    //SYSTEM
    SYSTEM_REGISTER_ENTITY(Entity.class, x -> x instanceof Entity),
    SYSTEM_ENTITIES_QUERY(Predicate.class, x -> x instanceof Predicate<?>),
    SYSTEM_ENTITIES_RESP(List.class, x -> x instanceof List<?>),

    // RANDOM
    KEY_VALUE_EVENT(Pair.class, x -> x instanceof Pair),
    DO_TASK(Task.class, x -> x instanceof Task),

    // NPC
    NPC_ARRIVED_AT_LOC(EventData.NpcLocationArrival.class, x -> x instanceof EventData.NpcLocationArrival),
    NPC_TRAVEL_TO(EventData.NPCTravelTo.class, x -> x instanceof EventData.NPCTravelTo),

    // Quest
    QUEST_PLAYER_NEW(PlayerQuestEntity.class, x -> x instanceof PlayerQuestEntity),
    QUEST_WORLD_NEW(WorldQuestEntity.class, x -> x instanceof WorldQuestEntity),
    QUEST_COMPLETED_PLAYER(PlayerQuestEntity.class, x -> x instanceof PlayerQuestEntity),
    QUEST_COMPLETED_WORLD(WorldQuestEntity.class, x -> x instanceof WorldQuestEntity);

    public final Class<?> dataClass;
    private final Predicate<Object> validator;

    EventType(Class<?> dataClass, Predicate<Object> validator) {
        this.dataClass = dataClass;
        this.validator = validator;
    }

    public boolean validate(Object dataObj) {
        return validator.test(dataObj);
    }

    @Nullable
    public <T> T castOrNull(Object dataObj) {
        if (validate(dataObj)) {
            @SuppressWarnings("unchecked")
            T casted = (T) dataObj;
            return casted;
        }
        return null;
    }

}

