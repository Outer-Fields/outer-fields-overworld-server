package io.mindspce.outerfieldsserver.systems;

import io.mindspce.outerfieldsserver.area.TileData;
import io.mindspce.outerfieldsserver.enums.AreaId;
import io.mindspce.outerfieldsserver.enums.QueryType;
import io.mindspice.mindlib.data.geometry.IPolygon2;
import io.mindspice.mindlib.data.geometry.IRect2;
import io.mindspice.mindlib.data.geometry.IVector2;
import io.mindspice.mindlib.data.tuples.Pair;

import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;


public class EventData {

    public record EntityAreaChanged(boolean isPlayer, AreaId oldArea, AreaId newArea, IVector2 position) { }


    public record EntityChunkChanged(boolean isPlayer, IVector2 oldChunk, IVector2 newChunk) { }


    public record AreaEntered(boolean isPlayer, int enteredEntity) { }


    public record EntityPositionChanged(boolean isPlayer, IVector2 oldPosition, IVector2 newPosition) { }


    public record ViewRectUpdate(boolean isPlayer, IRect2 viewRect) { }


    public record LocalAreaChanged(boolean isPlayer, Set<IVector2> localChunks) { }


    public record NewEntity(boolean isPlayer, AreaId area, IVector2 chunkIndex, IVector2 position) { }


    public record TileDataUpdate(AreaId areaId, IVector2 chunkIndex, List<Pair<IVector2, TileData>> tileData, boolean isRemoval) { }


    public record Query<T, U, V>(QueryType queryType, V queryData, BiConsumer<T, U> queryCallBack) { }


    public record QueryResponse<T, U>(U data, BiConsumer<T, U> responseCallBack) { }


    public record CollisionData(boolean isRemoved, IPolygon2 poly) {}

}
