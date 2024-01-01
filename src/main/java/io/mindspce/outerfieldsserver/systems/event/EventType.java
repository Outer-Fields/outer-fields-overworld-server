package io.mindspce.outerfieldsserver.systems.event;

import io.mindspce.outerfieldsserver.core.Tick;
import io.mindspce.outerfieldsserver.core.networking.incoming.NetPlayerMovement;
import io.mindspce.outerfieldsserver.systems.EventData;
import io.mindspice.mindlib.data.geometry.IRect2;

import javax.management.Query;
import java.util.ArrayList;


public enum EventType {
    TICK(Tick.class),
    CALLBACK(CallBack.class),
    QUERY(Query.class),
    ENTITY_POSITION_CHANGED(EventData.EntityPositionChanged.class),
    PLAYER_VALID_MOVEMENT(EventData.EntityPositionChanged.class),
    PLAYER_INVALID_MOVEMENT(EventData.EntityPositionChanged.class),
    ENTITY_VIEW_RECT_ENTERED(EventData.AreaEntered.class),
    ENTITY_VIEW_RECT_CHANGED(IRect2.class),
    ENTITY_CHUNK_CHANGED(EventData.EntityChunkChanged.class),
    ENTITY_AREA_CHANGED(EventData.EntityAreaChanged.class),
    ENTITY_AREA_UPDATE(EventData.EntityAreaChanged.class),
    NEW_ENTITY(EventData.NewEntity.class),
    AREA_MONITORED_ENTERED(EventData.AreaEntered.class),
    NETWORK_IN_PLAYER_MOVEMENT(NetPlayerMovement.class),
    QUAD_TREE_QUERY_REQ(IRect2.class),
    QUAD_TREE_QUERY_RESP(ArrayList.class),
    ACTIVE_PLAYERS_QUERY(Query.class),
    TILE_DATA_UPDATE(EventData.TileDataUpdate.class),
    QUERY_RESPONSE(), COLLISION_CHANGE(EventData.CollisionData.class),
    COLLISION_UPDATE(EventData.CollisionData.class);

    private final Class<?> dataClass;

    EventType(Class<?> dataClass) {
        this.dataClass = dataClass;
    }

}

