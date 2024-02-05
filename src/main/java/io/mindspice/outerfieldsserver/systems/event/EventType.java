package io.mindspice.outerfieldsserver.systems.event;

import io.mindspice.outerfieldsserver.combat.schema.websocket.incoming.NetCombatAction;
import io.mindspice.outerfieldsserver.core.Tick;
import io.mindspice.outerfieldsserver.core.networking.incoming.NetInPlayerPosition;
import io.mindspice.outerfieldsserver.core.networking.proto.EntityProto;
import io.mindspice.outerfieldsserver.entities.*;
import io.mindspice.outerfieldsserver.enums.AreaId;
import io.mindspice.outerfieldsserver.enums.ClothingItem;
import io.mindspice.outerfieldsserver.ai.task.Task;
import io.mindspice.mindlib.data.geometry.IRect2;
import io.mindspice.mindlib.data.geometry.IVector2;
import io.mindspice.mindlib.data.tuples.Pair;
import io.mindspice.outerfieldsserver.enums.SeedType;
import jakarta.annotation.Nullable;
import org.springframework.security.core.parameters.P;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;


public enum EventType {
    // TESTING
    PING(Object.class, Objects::nonNull),
    PONG(Object.class, Objects::nonNull),

    // GENERAL EVENTS
    OBJECT(Object.class, Objects::nonNull),
    TICK(Tick.class, x -> x instanceof Tick),
    CALLBACK(Consumer.class, x -> x instanceof Consumer<?>),
    COMPLETABLE_EVENT(EventData.CompletableEvent.class, x -> x instanceof EventData.CompletableEvent<?, ?>),

    //NETWORK IN EVENTS
    NETWORK_IN_PLAYER_POSITION(NetInPlayerPosition.class, x -> x instanceof NetInPlayerPosition),

    //NETWORK OUT EVENTS
    NETWORK_OUT_ENTITY_UPDATE(byte[].class, x -> x instanceof byte[]),
    NETWORK_PLAYER_RECONNECT(WebSocketSession.class, x -> x instanceof WebSocketSession),
    NETWORK_IN_PLAYER_ACTION(Map.class, x -> x instanceof Map), // Map<NetPlayerAction, List<NetPlayerActionMsg>>
    NETWORK_IN_COMBAT_ACTION(NetCombatAction.class, x -> x instanceof NetCombatAction),

    // ENTITY EVENT
    NEW_POSITIONAL_ENTITY(EventData.NewPositionalEntity.class, x -> x instanceof EventData.NewPositionalEntity),
    ENTITY_POSITION_CHANGED(EventData.EntityPositionChanged.class, x -> x instanceof EventData.EntityPositionChanged),
    ENTITY_VIEW_RECT_ENTERED(Integer.class, x -> x instanceof Integer),
    Entity_VIEW_RECT_EXITED(Integer.class, x -> x instanceof Integer),
    ENTITY_VIEW_RECT_CHANGED(IRect2.class, x -> x instanceof IRect2),
    ENTITY_CHUNK_CHANGED(EventData.EntityChunkChanged.class, x -> x instanceof EventData.EntityChunkChanged),
    ENTITY_AREA_CHANGED(EventData.EntityAreaChanged.class, x -> x instanceof EventData.EntityAreaChanged),
    ENTITY_STATE_CHANGED(List.class, x -> x instanceof List<?>),
    ENTITY_PROPERTY_CHANGE(Pair.class, x -> x instanceof Pair),
    ENTITY_IS_ACTIVE_CHANGED(Pair.class, x -> x instanceof Pair<?, ?>), //Pair<Integer, Boolean>
    ENTITY_IS_ACTIVE_QUERY(Integer.class, x -> x instanceof Integer),
    ENTITY_IS_ACTIVE_RESP(Boolean.class, x -> x instanceof Boolean), //Pair<Integer, Boolean>
    ENTITY_SET_ACTIVE(Boolean.class, x -> x instanceof Boolean),
    ENTITY_VISIBILITY_QUERY(Integer.class, x -> x instanceof Integer),
    ENTITY_VISIBILITY_RESP(EventData.EntityVisibility.class, x -> x instanceof EventData.EntityVisibility),
    ENTITY_VISIBILITY_CHANGED(EventData.EntityVisibility.class, x -> x instanceof EventData.EntityVisibility),
    ENTITY_VISIBILITY_UPDATE(EventData.VisibilityUpdate.class, x -> x instanceof EventData.VisibilityUpdate),
    ENTITY_VISIBLE_TO_QUERY(Integer.class, x -> x instanceof Integer),
    ENTITY_VISIBLE_TO_RESPONSE(Boolean.class, x -> x instanceof Boolean),

    ENTITY_AREA_UPDATE(AreaId.class, x -> x instanceof AreaId),
    ENTITY_NAME_UPDATE(String.class, x -> x instanceof String),
    ENTITY_STATE_UPDATE(EventData.EntityStateUpdate.class, x -> x instanceof EventData.EntityStateUpdate),
    ENTITY_POSITION_UPDATE(IVector2.class, x -> x instanceof IVector2),
    ENTITY_PROPERTY_UPDATE(Pair.class, x -> x instanceof Pair),
    ENTITY_DESTROY(Entity.class, x -> x instanceof Entity),

    // PLAYER SUBSYSTEM EVENTS
    PLAYER_VALID_MOVEMENT(IVector2.class, x -> x instanceof IVector2),
    PLAYER_INVALID_MOVEMENT(EventData.EntityPositionChanged.class, x -> x instanceof EventData.EntityPositionChanged),

    PLAYER_FUNDS_ITEMS_QUERY(Integer.class, x -> x instanceof Integer),
    PLAYER_FUNDS_ITEMS_RESP(EventData.FundsAndItems.class, x -> x instanceof EventData.FundsAndItems),


    PLAYER_ITEMS_BANKED_TO_INV(Map.class, x -> x instanceof Map), // Map<Integer(itemId), Integer>
    PLAYER_ITEMS_INV_TO_BANKED(Map.class, x -> x instanceof Map), // Map<Integer(itemId), Integer>
    PLAYER_ADD_BANKED_ITEMS(Map.class, x -> x instanceof Map), // Map<String(key), ItemEntity>
    PLAYER_ADD_INV_ITEMS(Map.class, x -> x instanceof Map), // Map<String(key), ItemEntity>
    PLAYER_REMOVE_BANKED_ITEMS(Map.class, x -> x instanceof Map), // Map<String(key), Integer>
    PLAYER_REMOVE_INV_ITEMS(Map.class, x -> x instanceof Map), // Map<String(key), Integer>

    // Player Actions
    PLAYER_DROP_ITEMS(Map.class, x -> x instanceof Map), // Map<Integer(itemId), Integer(amount>
    PLAYER_PICKUP_ITEMS(Pair.class, x -> x instanceof Pair), // Pair<Integer(containerId), Map<Integer(itemId), Integer(amount)>>
    PLAYER_PUT_CONTAINER(Pair.class, x -> x instanceof Pair), //Pair<Integer(containerId), Map<Integer(itemId), Integer(amount)>>

    // Serialization
    SERIALIZED_ENTITY_REQUEST(Integer.class, x -> x instanceof Integer),
    SERIALIZED_ENTITIES_REQUEST(Predicate.class, x -> x instanceof Predicate<?>),
    SERIALIZED_CHARACTER_RESP(EntityProto.CharacterEntity.class, x -> x instanceof EntityProto.CharacterEntity),
    SERIALIZED_LOC_ITEM_RESP(EntityProto.LocationItemEntity.class, x -> x instanceof EntityProto.LocationItemEntity),

    // CHARACTER
    CHARACTER_OUTFIT_CHANGED(ClothingItem[].class, x -> x instanceof ClothingItem[]),
    CHARACTER_OUTFIT_UPDATE(List.class, x -> x instanceof List), // List<ClothingItem>
    CHARACTER_DEATH(EventData.CharacterDeath.class, x -> x instanceof EventData.CharacterDeath),
    CHARACTER_NEW_SPAWN(IVector2.class, x -> x instanceof IVector2),
    CHARACTER_RESPAWN(Object.class, Objects::nonNull), // TODO add respawn dataclass

    // CONTAINER
    CONTAINER_CONTAINED_ITEMS_QUERY(Integer.class, x -> x instanceof Integer),
    CONTAINER_CONTAINED_ITEMS_RESP(Map.class, x -> x instanceof Map), // Map<String, ItemEntity<?>>
    CONTAINER_REMOVE_ITEMS(Map.class, x -> x instanceof Map), // Map<String, Integer>
    CONTAINER_ADD_ITEMS(Map.class, x -> x instanceof Map), // Map<String, Integer>

    // AREA
    AREA_MONITOR_QUERY(List.class, x -> x instanceof List), //List<Pair<IVector2,Integer>
    AREA_MONITOR_RESP(List.class, x -> x instanceof List), //List<Integer>
    AREA_MONITOR_ENTERED(EventData.AreaEntered.class, x -> x instanceof EventData.AreaEntered),
    AREA_ENTITIES_QUERY(AreaId.class, x -> x instanceof AreaId),
    AREA_ENTITIES_RESPONSE(int[].class, x -> x instanceof int[]),

    // OTHER AREA/TILE SPECIFIC EVENTS
    TILE_DATA_UPDATE(EventData.TileDataUpdate.class, x -> x instanceof EventData.TileDataUpdate),
    TILE_DATA_QUERY(List.class, x -> x instanceof List), // List<IVector2> chunkIndex
    TILE_DATA_RESPONSE(List.class, x -> x instanceof List<?>),
    COLLISION_CHANGE(EventData.CollisionData.class, x -> x instanceof EventData.CollisionData),
    COLLISION_UPDATE(EventData.CollisionData.class, x -> x instanceof EventData.CollisionData),
    ENTITY_GRID_QUERY(IRect2.class, x -> x instanceof IRect2),
    ENTITY_GRID_RESPONSE(int[].class, x -> x instanceof int[]),

    // LOCATION
    LOCATION_NEW(LocationEntity.class, x -> x instanceof LocationEntity),
    LOCATION_REMOVE(Integer.class, x -> x instanceof Integer),

    // FARMING
    FARM_PLANT_PLOT(SeedType.class, x -> x instanceof SeedType),
    FARM_HARVEST_PLOT(Integer.class, x -> x instanceof Integer),
    FARM_UPDATE_PLOT_OWNER(Integer.class, x -> x instanceof Integer),

    //SYSTEM
    SYSTEM_REGISTER_ENTITY(Entity.class, x -> x instanceof Entity),
    SYSTEM_ENTITIES_QUERY(Predicate.class, x -> x instanceof Predicate<?>),
    SYSTEM_ENTITIES_RESP(List.class, x -> x instanceof List<?>),

    // RANDOM
    KEY_VALUE_EVENT(Pair.class, x -> x instanceof Pair),
    DO_TASK(Task.class, x -> x instanceof Task),
    UPDATE_LISTENING(Boolean.class, x -> x instanceof Boolean),

    // NPC
    NPC_ARRIVED_AT_LOC(EventData.NPCLocationArrival.class, x -> x instanceof EventData.NPCLocationArrival),
    NPC_TRAVEL_TO(EventData.NPCTravelTo.class, x -> x instanceof EventData.NPCTravelTo),

    // QUEST
    QUEST_PLAYER_NEW(PlayerQuestEntity.class, x -> x instanceof PlayerQuestEntity),
    QUEST_WORLD_NEW(WorldQuestEntity.class, x -> x instanceof WorldQuestEntity),
    QUEST_COMPLETED_PLAYER(PlayerQuestEntity.class, x -> x instanceof PlayerQuestEntity),
    QUEST_COMPLETED_WORLD(WorldQuestEntity.class, x -> x instanceof WorldQuestEntity),

    // COMBAT
    COMBAT_INIT_NPC(EventData.CombatInit.class, x -> x instanceof EventData.CombatInit),
    COMBAT_INIT_PVP(EventData.CombatInit.class, x -> x instanceof EventData.CombatInit);

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

