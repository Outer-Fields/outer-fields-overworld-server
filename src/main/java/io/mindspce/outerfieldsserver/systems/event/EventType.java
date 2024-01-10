package io.mindspce.outerfieldsserver.systems.event;

import io.mindspce.outerfieldsserver.core.Tick;
import io.mindspce.outerfieldsserver.core.networking.incoming.NetInPlayerPosition;
import io.mindspce.outerfieldsserver.entities.LocationEntity;
import io.mindspce.outerfieldsserver.enums.AreaId;
import io.mindspce.outerfieldsserver.enums.ClothingItem;
import io.mindspce.outerfieldsserver.systems.EventData;
import io.mindspce.outerfieldsserver.ai.task.Task;
import io.mindspice.mindlib.data.geometry.IRect2;
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
    TICK(Tick.class, x -> x instanceof Tick),
    CALLBACK(Consumer.class, x -> x instanceof Consumer<?>),
    COMPLETABLE_EVENT(EventData.CompletableEvent.class, x -> x instanceof EventData.CompletableEvent<?, ?>),

    //NETWORK IN EVENTS
    NETWORK_IN_PLAYER_POSITION(NetInPlayerPosition.class, x -> x instanceof NetInPlayerPosition),

    //NETWORK OUT EVENTS
    NETWORK_OUT_ENTITY_UPDATE(byte[].class, x -> x instanceof byte[]),

    // ENTITY CHANGE EVENTS
    ENTITY_POSITION_CHANGED(EventData.EntityPositionChanged.class, x -> x instanceof EventData.EntityPositionChanged),
    ENTITY_VIEW_RECT_ENTERED(EventData.AreaEntered.class, x -> x instanceof EventData.AreaEntered),
    NEW_ENTITY(EventData.NewEntity.class, x -> x instanceof EventData.NewEntity),
    ENTITY_VIEW_RECT_CHANGED(IRect2.class, x -> x instanceof IRect2),
    ENTITY_CHUNK_CHANGED(EventData.EntityChunkChanged.class, x -> x instanceof EventData.EntityChunkChanged),
    ENTITY_AREA_CHANGED(EventData.EntityAreaChanged.class, x -> x instanceof EventData.EntityAreaChanged),

    // ENTITY UPDATE EVENTS
    ENTITY_AREA_UPDATE(AreaId.class, x -> x instanceof AreaId),

    // PLAYER SUBSYSTEM EVENTS
    PLAYER_VALID_MOVEMENT(EventData.EntityPositionChanged.class, x -> x instanceof EventData.EntityPositionChanged),
    PLAYER_INVALID_MOVEMENT(EventData.EntityPositionChanged.class, x -> x instanceof EventData.EntityPositionChanged),
    PLAYER_RECONNECT(WebSocketSession.class, x -> x instanceof WebSocketSession),

    // OTHER COMPONENT SPECIFIC EVENTS
    AREA_MONITOR_ENTERED(EventData.AreaEntered.class, x -> x instanceof EventData.AreaEntered),
    TILE_DATA_UPDATE(EventData.TileDataUpdate.class, x -> x instanceof EventData.TileDataUpdate),
    COLLISION_CHANGE(EventData.CollisionData.class, x -> x instanceof EventData.CollisionData),
    COLLISION_UPDATE(EventData.CollisionData.class, x -> x instanceof EventData.CollisionData),
    ENTITY_STATE_CHANGED(List.class, x -> x instanceof List<?>),
    ENTITY_STATE_UPDATE(EventData.EntityStateUpdate.class, x -> x instanceof EventData.EntityStateUpdate),
    ENTITY_NAME_CHANGE(String.class, x -> x instanceof String),
    ENTITY_NAME_UPDATE(String.class, x -> x instanceof String),
    ENTITY_GRID_QUERY(IRect2.class, x -> x instanceof IRect2),
    ENTITY_GRID_RESPONSE(int[].class, x -> x instanceof int[]),
    SERIALIZED_ENTITY_REQUEST(Integer.class, x -> x instanceof int[]),
    SERIALIZED_ENTITY_RESP(byte[].class, x -> x instanceof byte[]),
    SERIALIZED_ENTITIES_REQUEST(Predicate.class, x -> x instanceof Predicate<?>),
    SERIALIZED_ENTITIES_RESP(Pair.class, x -> x instanceof Pair<?, ?>),

    CHARACTER_OUTFIT_CHANGED(ClothingItem.class, x -> x instanceof ClothingItem[]),
    CHARACTER_OUTFIT_UPDATE(byte[].class, x -> x instanceof byte[]),
    NEW_LOCATION(LocationEntity.class, x -> x instanceof LocationEntity),
    REMOVE_LOCATION(Integer.class, x -> x instanceof Integer),
    AREA_MONITOR_QUERY(List.class, x -> x instanceof List), //List<Pair<IVector2,Integer>
    AREA_MONITOR_RESP(List.class, x -> x instanceof List), //List<Integer>
    SYSTEM_ENTITIES_QUERY(Predicate.class, x -> x instanceof Predicate<?>),
    SYSTEM_ENTITIES_RESP(List.class, x -> x instanceof List<?>),
    KEY_VALUE_EVENT(Pair.class, x -> x instanceof Pair),
    DO_TASK(Task.class, x -> x instanceof Task);

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

