package io.mindspce.outerfieldsserver.components.world;

import com.google.protobuf.Internal;
import io.mindspce.outerfieldsserver.area.TileData;
import io.mindspce.outerfieldsserver.entities.ChunkEntity;
import io.mindspce.outerfieldsserver.components.Component;
import io.mindspce.outerfieldsserver.components.logic.PredicateLib;
import io.mindspce.outerfieldsserver.core.GameSettings;
import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.enums.ComponentType;
import io.mindspce.outerfieldsserver.systems.EventData;
import io.mindspce.outerfieldsserver.systems.event.Event;
import io.mindspce.outerfieldsserver.systems.event.EventType;
import io.mindspce.outerfieldsserver.util.GridUtils;
import io.mindspice.mindlib.data.collections.lists.primative.IntList;
import io.mindspice.mindlib.data.geometry.IVector2;
import io.mindspice.mindlib.functional.consumers.BiPredicatedBiConsumer;
import jakarta.annotation.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;


public class ChunkMap extends Component<ChunkMap> {
    public final ChunkEntity[][] chunkMap;

    public ChunkMap(Entity parent, ChunkEntity[][] chunkMap) {
        super(parent, ComponentType.CHUNK_MAP, List.of());
        this.chunkMap = chunkMap;
//        registerListener(EventType.ENTITY_CHUNK_CHANGED, BiPredicatedBiConsumer.of(PredicateLib::isSameAreaEvent, ChunkMap::onEntityChunkChanged));
//        registerListener(EventType.ENTITY_AREA_CHANGED, ChunkMap::onEntityAreaChange);
//        registerListener(EventType.NEW_ENTITY, BiPredicatedBiConsumer.of(PredicateLib::isSameAreaEvent, ChunkMap::onNewEntityEnteredChunk));
        registerListener(EventType.TILE_DATA_UPDATE, BiPredicatedBiConsumer.of(PredicateLib::isSameAreaEvent, ChunkMap::onTileDataUpdate));
        registerListener(EventType.TILE_DATA_QUERY, BiPredicatedBiConsumer.of(PredicateLib::isSameAreaEvent, ChunkMap::onTileDataQuery));
    }

    private void onTileDataUpdate(Event<EventData.TileDataUpdate> event) {
        EventData.TileDataUpdate data = event.data();
        ChunkEntity tileData = getChunkByIndex(data.chunkIndex());
        if (tileData == null) {
            //TODO log this
            return;
        }
        tileData.updateChunkData(event.data());
    }

    public void onTileDataQuery(Event<List<IVector2>> event) {
        List<TileData> respList = event.data().stream().map(this::getTileByIndex).toList();
        emitEvent(Event.responseEvent(this, event, EventType.TILE_DATA_RESPONSE, respList));
    }

//    private void onEntityAreaChange(Event<EventData.EntityAreaChanged> event) {
//        if (areaId() == event.data().oldArea()) {
//
//        }
//    }
//
//    private void onEntityChunkChanged(Event<EventData.EntityChunkChanged> event) {
//        ChunkEntity oldChunk = getChunkByIndex(event.data().oldChunk());
//        ChunkEntity newChunk = getChunkByIndex(event.data().newChunk());
//        if (oldChunk == null) {
//            //todo logs this
//        } else {
//            oldChunk.removeActivePlayer(event.issuerEntityId());
//        }
//        if (newChunk == null) {
//            // TODO log this
//        } else {
//            newChunk.addActivePlayer(event.issuerEntityId());
//        }
//    }
//
//    private void onNewEntityEnteredChunk(Event<EventData.NewEntity> event) {
//        ChunkEntity chunk = getChunkByIndex(GridUtils.globalToChunk(event.data().position()));
//        if (chunk == null) {
//            // TODO log this
//            return;
//        }
//        chunk.addActivePlayer(event.issuerEntityId());
//    }

    @Nullable
    public ChunkEntity getChunkByGlobalPos(IVector2 pos) {
        int x = pos.x() / GameSettings.GET().chunkSize().x();
        int y = pos.y() / GameSettings.GET().chunkSize().y();
        if (x < 0 || y < 0 || x >= chunkMap.length || y >= chunkMap[0].length) {
            return null;
        }
        return chunkMap[x][y];
    }

    @Nullable
    public ChunkEntity getChunkByGlobalPos(int posX, int posY) {
        int x = posX / GameSettings.GET().chunkSize().x();
        int y = posY / GameSettings.GET().chunkSize().y();
        if (x < 0 || y < 0 || x >= chunkMap.length || y >= chunkMap[0].length) {
            return null; // return null, this is acceptable and should be handled by caller
        }
        return chunkMap[x][y];
    }

    @Nullable
    public ChunkEntity getChunkByIndex(IVector2 index) {
        if (index.x() < 0 || index.y() < 0 || index.x() >= chunkMap.length || index.y() >= chunkMap[0].length) {
            return null;
        }
        return chunkMap[index.x()][index.y()];
    }

    @Nullable
    public ChunkEntity getChunkByIndex(int x, int y) {
        if (x < 0 || y < 0 || x >= chunkMap.length || y >= chunkMap[0].length) {
            return null;
        }
        return chunkMap[x][y];
    }

    @Nullable
    TileData getTileByIndex(IVector2 tileIndex) {
        IVector2 chunkIndex = GridUtils.tileIndexToChunk(tileIndex);
        ChunkEntity chunk = chunkMap[chunkIndex.x()][chunkIndex.y()];
        if (chunk == null) { return null; }
        return chunk.getTileByIndex(tileIndex);
    }
}
