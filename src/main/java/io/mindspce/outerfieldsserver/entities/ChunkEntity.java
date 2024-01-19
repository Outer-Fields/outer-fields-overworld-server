package io.mindspce.outerfieldsserver.entities;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import io.mindspce.outerfieldsserver.area.ChunkJson;
import io.mindspce.outerfieldsserver.area.TileData;
import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.enums.AreaId;
import io.mindspce.outerfieldsserver.enums.EntityType;
import io.mindspce.outerfieldsserver.systems.EventData;
import io.mindspce.outerfieldsserver.util.GridUtils;
import io.mindspice.mindlib.data.geometry.*;
import jakarta.annotation.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class ChunkEntity extends Entity {
    // direct reference to the map stored in the component
    private final IVector2 chunkIndex;
    volatile Map<IVector2, TileData> activeTiles;
    //  final TIntSet activePlayers = new TIntHashSet();

    public ChunkEntity(int entityId, AreaId areaId, IVector2 index, Map<IVector2,
            TileData> activeTiles) {
        super(entityId, EntityType.CHUNK, areaId, index);
        this.activeTiles = activeTiles;
        this.chunkIndex = index;
    }

    public ChunkEntity(int entityId, AreaId areaId, IVector2 index, ChunkJson chunkJson) {
        super(entityId, EntityType.CHUNK, areaId, index);
        activeTiles = loadFromJson(chunkJson);
        this.chunkIndex = index;
    }

    public void updateChunkData(EventData.TileDataUpdate data) {
        if (data.isRemoval()) {
            data.tileData().forEach(e -> activeTiles.remove(e.first()));
            activeTiles = Map.copyOf(activeTiles);
        } else {
            data.tileData().forEach(e -> activeTiles.put(e.first(), e.second()));
            activeTiles = Map.copyOf(activeTiles);
        }
    }

//    public TIntSet getActivePlayers() {
//        return new TIntHashSet(activePlayers);
//    }
//
//    public void addActivePlayer(int entityId) {
//        activePlayers.remove(entityId);
//    }
//
//    public void removeActivePlayer(int entityId) {
//        activePlayers.remove(entityId);
//    }

    @Nullable
    public TileData getTileByGlobalPos(IVector2 pos) {
        return activeTiles.get(GridUtils.globalToLocalTile(chunkIndex, pos));
    }

    @Nullable
    public TileData getTileByIndex(IVector2 index) {
        return activeTiles.get(index);
    }

    public static Map<IVector2, TileData> loadFromJson(ChunkJson json) {
        ConcurrentHashMap<IVector2, TileData> tileData = new ConcurrentHashMap<>(50);

        for (var collision : json.collisionMask()) {
            tileData.put(collision, new TileData(collision, false, true, -1, -1, -1));
        }
        for (var area : json.areaMask()) {
            if (tileData.containsKey(area)) {
                tileData.get(area).withLocationChange(1); //TODO fix this
            }
            tileData.put(area, new TileData(area, false, false, -1, -1, -1));
        }

        for (var navigation : json.navMask()) {
            if (tileData.containsKey(navigation)) {
                tileData.get(navigation).withNavChange(true);
            }
            tileData.put(navigation, new TileData(navigation, true, false, 1, -1, -1));
        }
        return tileData;
    }
}
