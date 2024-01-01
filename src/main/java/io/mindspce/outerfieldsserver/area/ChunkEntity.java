package io.mindspce.outerfieldsserver.area;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import io.mindspce.outerfieldsserver.components.ComponentFactory;
import io.mindspce.outerfieldsserver.components.SimpleListener;
import io.mindspce.outerfieldsserver.components.SimpleObject;
import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.enums.AreaId;
import io.mindspce.outerfieldsserver.enums.EntityType;
import io.mindspce.outerfieldsserver.systems.EventData;
import io.mindspce.outerfieldsserver.systems.event.EventType;
import io.mindspce.outerfieldsserver.util.GridUtils;
import io.mindspice.mindlib.data.geometry.*;
import io.mindspice.mindlib.data.tuples.Pair;
import jakarta.annotation.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class ChunkEntity extends Entity {
    // direct reference to the map stored in the component
    final Map<IVector2, TileData> activeTiles;
    final TIntSet activePlayers = new TIntHashSet();

    public ChunkEntity(int entityId, AreaId areaId, IVector2 index, ConcurrentHashMap<IVector2,
            TileData> activeTiles) {
        super(entityId, EntityType.CHUNK_ENTITY, areaId, index);
        this.activeTiles = activeTiles;
    }

    public ChunkEntity(int entityId, AreaId areaId, IVector2 index, ChunkJson chunkJson) {
        super(entityId, EntityType.CHUNK_ENTITY, areaId, index);
        activeTiles = loadFromJson(chunkJson);
    }

    public void updateChunkData(EventData.TileDataUpdate data) {
        if (data.isRemoval()) {
            data.tileData().forEach(e -> activeTiles.remove(e.first()));
        } else {
            data.tileData().forEach(e -> activeTiles.put(e.first(), e.second()));
        }
    }

    public TIntSet getActivePlayers() {
        return new TIntHashSet(activePlayers);
    }

    public void addActivePlayer(int entityId) {
        activePlayers.remove(entityId);
    }

    public void removeActivePlayer(int entityId) {
        activePlayers.remove(entityId);
    }

    @Nullable
    public TileData getTileByGlobalPos(IVector2 pos) {
        return activeTiles.get(GridUtils.globalToLocalTile(chunkIndex, pos));
    }

    @Nullable
    public TileData getTileByIndex(IVector2 index) {
        return activeTiles.get(index);
    }

    private Map<IVector2, TileData> loadFromJson(ChunkJson json) {
        ConcurrentHashMap<IVector2, TileData> tileData = new ConcurrentHashMap<>(50);

        for (var collision : json.collisionMask()) {
            tileData.put(collision, new TileData(collision, null, true, false));
        }
        for (var area : json.areaMask()) {
            if (tileData.containsKey(area)) {
                tileData.get(area).withLocationChange(true);
            }
            tileData.put(area, new TileData(area, null, false, true));
        }

        for (var navigation : json.navMask()) {
            if (tileData.containsKey(navigation)) {
                tileData.get(navigation).withNavChange(new NavData());
            }
            tileData.put(navigation, new TileData(navigation, new NavData(), false, false));
        }
        return tileData;
    }
}
