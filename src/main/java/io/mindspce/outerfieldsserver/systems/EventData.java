package io.mindspce.outerfieldsserver.systems;

import io.mindspce.outerfieldsserver.area.TileData;
import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.enums.AreaId;
import io.mindspce.outerfieldsserver.enums.EntityState;
import io.mindspce.outerfieldsserver.enums.QueryType;
import io.mindspce.outerfieldsserver.systems.event.Event;
import io.mindspce.outerfieldsserver.systems.event.EventType;
import io.mindspice.mindlib.data.geometry.IPolygon2;
import io.mindspice.mindlib.data.geometry.IRect2;
import io.mindspice.mindlib.data.geometry.IVector2;
import io.mindspice.mindlib.data.tuples.Pair;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;


public class EventData {

    public record EntityAreaChanged(boolean isPlayer, AreaId oldArea, AreaId newArea, IVector2 position) { }


    public record EntityChunkChanged(boolean isPlayer, IVector2 oldChunk, IVector2 newChunk) { }


    public record AreaEntered(boolean isPlayer, int enteredEntity) { }


    public record EntityPositionChanged(boolean isPlayer, IVector2 oldPosition, IVector2 newPosition) { }


    public record EntityStateUpdate(boolean clearExisting, List<EntityState> stateAdditions, List<EntityState> statesRemovals) { }


    public record ViewRectUpdate(boolean isPlayer, IRect2 viewRect) { }


    public record LocalAreaChanged(boolean isPlayer, Set<IVector2> localChunks) { }


    public record NewEntity(boolean isPlayer, AreaId area, IVector2 chunkIndex, IVector2 position, Entity entity) { }


    public record TileDataUpdate(AreaId areaId, IVector2 chunkIndex, List<Pair<IVector2, TileData>> tileData, boolean isRemoval) { }


    public record CollisionData(boolean isRemoved, IPolygon2 poly) { }


    public record CompletableEvent<T, U>(Event<T> mainEvent, Event<U> completionEvent) { }


    public record EntitySerialization(int[] ids, ByteBuffer buffer) { }

}
