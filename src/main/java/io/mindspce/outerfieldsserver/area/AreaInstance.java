package io.mindspce.outerfieldsserver.area;

import io.mindspce.outerfieldsserver.core.GameSettings;
import io.mindspce.outerfieldsserver.datacontainers.ActiveChunkUpdate;
import io.mindspice.mindlib.data.geometry.IVector2;
import jakarta.annotation.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;


public class AreaInstance implements Runnable {
    private final String arenaName;
    private final ChunkData[][] chunkMap;
    private final IVector2 areaSize;
    private final Map<IVector2, AtomicInteger> activeChunks = new ConcurrentHashMap<>(100);
    private final ConcurrentLinkedDeque<ActiveChunkUpdate> activeChunkUpdateQueue = new ConcurrentLinkedDeque<>();

    public AreaInstance(String arenaName, ChunkData[][] chunkMap) {
        this.arenaName = arenaName;
        this.chunkMap = chunkMap;
        areaSize = IVector2.of(chunkMap.length, chunkMap[0].length);
    }

    @Override
    public void run() {
        ActiveChunkUpdate next;
        while ((next = activeChunkUpdateQueue.poll()) != null) {
            next.additions.forEach(idx -> activeChunks.compute(idx, (key, value) -> {
                if (value == null) {
                    return new AtomicInteger(1);
                } else {
                    value.incrementAndGet();
                    return value;
                }
            }));
            next.removals.forEach((idx) -> activeChunks.computeIfPresent(idx, (key, value) -> {
                value.decrementAndGet();
                return value;
            }));
        }
    }

    public void queueActiveChunk(ActiveChunkUpdate areaUpdate) {
        activeChunkUpdateQueue.add(areaUpdate);
    }

    public String getArenaName() {
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


}

