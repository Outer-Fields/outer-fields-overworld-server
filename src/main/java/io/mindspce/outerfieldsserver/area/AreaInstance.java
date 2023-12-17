package io.mindspce.outerfieldsserver.area;

import io.mindspce.outerfieldsserver.core.GameSettings;
import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.entities.player.PlayerState;
import io.mindspce.outerfieldsserver.systems.event.EventManager;
import io.mindspce.outerfieldsserver.data.wrappers.ActiveEntityUpdate;
import io.mindspce.outerfieldsserver.enums.AreaId;
import io.mindspce.outerfieldsserver.enums.EventType;
import io.mindspice.mindlib.data.geometry.IVector2;
import jakarta.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;


public class AreaInstance {
    private final AreaId arenaName;
    private final ChunkData[][] chunkMap;
    private final IVector2 areaSize;
    private final List<PlayerState> activePlayers = new ArrayList<>(20);
    private final Map<IVector2, AtomicInteger> activeChunks = new ConcurrentHashMap<>(100);

    private final ConcurrentLinkedDeque<ActiveEntityUpdate> entityUpdateQueue = new ConcurrentLinkedDeque<>();


    public AreaInstance(AreaId arenaName, ChunkData[][] chunkMap) {
        this.arenaName = arenaName;
        this.chunkMap = chunkMap;
        areaSize = IVector2.of(chunkMap.length, chunkMap[0].length);
    }

    public void submitEntityUpdate(ActiveEntityUpdate entityUpdate) {
        if (entityUpdate == null) { return; }
        entityUpdateQueue.add(entityUpdate);
    }

//    public void boad() {
//        int queueSize = entityUpdateQueue.size();
//        for (int i = 0; i < queueSize; ++i) {
//            ActiveEntityUpdate entityUpdate = entityUpdateQueue.poll();
//
//        }
//    }



    public AreaId getId() {
        return arenaName;
    }

    public IVector2 getAreaSize() {
        return areaSize;
    }

    public ChunkData[][] getChunkMap() {
        return chunkMap;
    }

    public ChunkData getChunkByGlobalPos(IVector2 pos) {
        int x = pos.x() / GameSettings.GET().chunkSize().x();
        int y = pos.y() / GameSettings.GET().chunkSize().y();
        if (x > chunkMap.length || y > chunkMap[0].length) {
            throw new IndexOutOfBoundsException("Position out of chunk bounds");
        }
        return chunkMap[x][y];
    }

    public ChunkData getChunkByGlobalPos(int posX, int posY) {
        int x = posX / GameSettings.GET().chunkSize().x();
        int y = posY / GameSettings.GET().chunkSize().y();
        if (x > chunkMap.length || y > chunkMap[0].length) {
            throw new IndexOutOfBoundsException("Position out of chunk bounds");
        }
        return chunkMap[x][y];
    }

    @Nullable
    public ChunkData getChunkByIndex(IVector2 index) {
        if (index.x() > chunkMap.length || index.y() > chunkMap[0].length) {
            return null;
        }
        if (index.x() < 0 || index.y() < 0) {
            return null;
        }
        return chunkMap[index.x()][index.y()];
    }

    @Nullable
    public ChunkData getChunkByIndex(int x, int y) {
        if (x > chunkMap.length || y > chunkMap[0].length) {
            return null;
        }
        if (x < 0 || y < 0) {
            return null;
        }
        return chunkMap[x][y];
    }

    public IVector2[][] getVectorMap() {
        IVector2[][] vecMap = new IVector2[chunkMap.length][chunkMap[0].length];
        for (int i = 0; i < chunkMap.length; ++i) {
            for (int j = 0; j < chunkMap[0].length; ++j) {
                vecMap[i][j] = chunkMap[i][j].getIndex();
            }
        }
        return vecMap;
    }

    public List<PlayerState> getActivePlayers() {
        return activePlayers;
    }

    public void subscribeToChunk(IVector2 chunkIndex, EventType eventType, PlayerState playerState) {
        ChunkData chunk = getChunkByIndex(chunkIndex);
        if (chunk != null) {
            chunk.subscribe(eventType, playerState);
        }
    }

    public void unSubscribeToChunk(IVector2 chunkIndex, EventType eventType, PlayerState playerState) {
        ChunkData chunk = getChunkByIndex(chunkIndex);
        if (chunk != null) {
            chunk.unsubscribe(eventType, playerState);
        }
    }

}

