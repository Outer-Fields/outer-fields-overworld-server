package io.mindspice.outerfieldsserver.entities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.mindspice.outerfieldsserver.area.ChunkJson;
import io.mindspice.outerfieldsserver.area.TileData;
import io.mindspice.outerfieldsserver.enums.AreaId;
import io.mindspice.outerfieldsserver.enums.EntityType;
import io.mindspice.outerfieldsserver.systems.event.EventData;
import io.mindspice.outerfieldsserver.util.GridUtils;
import io.mindspice.mindlib.data.geometry.*;
import io.mindspice.mindlib.util.JsonUtils;
import jakarta.annotation.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class ChunkEntity extends Entity {
    // direct reference to the map stored in the component
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

    public String toString() {
        JsonNode node = new JsonUtils.ObjectBuilder()
                .put("entityType", entityType)
                .put("entityId", id)
                .put("activeTiles", activeTiles.size())
                .put("name", name)
                .put("areaId", areaId)
                .put("chunkIndex", chunkIndex)
                .put("attachComponents", getAttachedComponentTypes())
                .put("listeningFor", listeningForTypes())
                .put("systemRegistry", systemRegistry != null ?  systemRegistry.systemType() : null)
                .buildNode();
        try {
            return JsonUtils.writePretty(node);
        } catch (JsonProcessingException e) {
            return "Error serializing to string";
        }
    }
}
