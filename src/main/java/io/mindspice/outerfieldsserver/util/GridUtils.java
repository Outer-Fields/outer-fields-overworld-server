package io.mindspice.outerfieldsserver.util;

import io.mindspice.mindlib.data.geometry.IRect2;
import io.mindspice.outerfieldsserver.area.ChunkJson;
import io.mindspice.outerfieldsserver.data.wrappers.ChunkTileIndex;
import io.mindspice.outerfieldsserver.area.TileData;
import io.mindspice.outerfieldsserver.core.WorldSettings;
import io.mindspice.mindlib.data.geometry.IVector2;
import io.mindspice.mindlib.data.tuples.Pair;
import io.mindspice.mindlib.util.JsonUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class GridUtils {


    public static void printGrid(IVector2[][] grid) {
        for (int x = 0; x < grid[0].length; x++) {
            for (int y = 0; y < grid.length; y++) {
                System.out.print(grid[y][x] + " ");
            }
            System.out.println();
        }
    }

    public static void printNavMap(TileData[][] tileData) {
        for (int x = 0; x < tileData[0].length; x++) {
            for (int y = 0; y < tileData.length; y++) {
                if (tileData[y][x].isNavigable()) {
                    System.out.print("* ");
                } else {
                    System.out.print("# ");
                }
            }
            System.out.println();
        }
    }

    public static IVector2 tileToGlobal(ChunkTileIndex chunkTileIndex) {
        return tileToGlobal(chunkTileIndex.chunkIndex(), chunkTileIndex.tileIndex());
    }

    public static IVector2 tileToGlobal(IVector2 chunk, IVector2 tile) {
        return IVector2.of(
                (chunk.x() * WorldSettings.GET().chunkSize().x()) + (tile.x() * WorldSettings.GET().tileSize()),
                (chunk.y() * WorldSettings.GET().chunkSize().y()) + (tile.y() * WorldSettings.GET().tileSize())
        );
    }

    public static IVector2 globalToLocalTile(IVector2 chunkIndex, IVector2 globalPos) {
        return IVector2.of(
                (globalPos.x() - chunkIndex.x() * WorldSettings.GET().chunkSize().x()) / WorldSettings.GET().tileSize(),
                (globalPos.y() - chunkIndex.y() * WorldSettings.GET().chunkSize().y()) / WorldSettings.GET().tileSize()
        );
    }

    public static IVector2 tileIndexToChunk(IVector2 tileIndex) {
        return IVector2.of(
                tileIndex.x() / WorldSettings.GET().chunkSize().x(),
                tileIndex.y() / WorldSettings.GET().chunkSize().y()
        );
    }

    public static IVector2 globalToChunk(IVector2 globalPos) {
        int chunkX = globalPos.x() / WorldSettings.GET().chunkSize().x();
        int chunky = globalPos.y() / WorldSettings.GET().chunkSize().y();
        return IVector2.of(chunkX, chunky);
    }

    public static ChunkTileIndex globalToChunkTile(IVector2 globalPos) {
        int chunkX = globalPos.x() / WorldSettings.GET().chunkSize().x();
        int chunky = globalPos.y() / WorldSettings.GET().chunkSize().y();
        int tileXPos = globalPos.x() % WorldSettings.GET().chunkSize().x();
        int tileYPos = globalPos.y() % WorldSettings.GET().chunkSize().y();
        int tileX = tileXPos / WorldSettings.GET().tileSize();
        int tileY = tileYPos / WorldSettings.GET().tileSize();
        return new ChunkTileIndex(IVector2.of(chunkX, chunky), IVector2.of(tileX, tileY));
    }

    public static boolean isNewChunk(IVector2 lastChunk, IVector2 globalPos) {
        int currChunkX = globalPos.x() / WorldSettings.GET().chunkSize().x();
        int currChunkY = globalPos.y() / WorldSettings.GET().chunkSize().y();
        return lastChunk.x() != currChunkX || lastChunk.y() != currChunkY;
    }

    public static Pair<List<IVector2>, List<IVector2>> getChunkDeltas(IVector2 oldPos, IVector2 newPos) {
        List<IVector2> oldChunks = new ArrayList<>(5);
        List<IVector2> newChunks = new ArrayList<>(5);
        List<IVector2> currGrid = new ArrayList<>(9);
        int minX = Math.min(oldPos.x(), newPos.x()) - 1;
        int maxX = Math.max(oldPos.x(), newPos.x()) + 1;
        int minY = Math.min(oldPos.y(), newPos.y()) - 1;
        int maxY = Math.max(oldPos.y(), newPos.y()) + 1;

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                if (x >= newPos.x() - 1 && x <= newPos.x() + 1 && y >= newPos.y() - 1 && y <= newPos.y() + 1) {
                    if (!(x >= oldPos.x() - 1 && x <= oldPos.x() + 1 && y >= oldPos.y() - 1 && y <= oldPos.y() + 1)) {
                        newChunks.add(IVector2.of(x, y));
                    }
                } else if (x >= oldPos.x() - 1 && x <= oldPos.x() + 1 && y >= oldPos.y() - 1 && y <= oldPos.y() + 1) {
                    oldChunks.add(IVector2.of(x, y));
                }
            }
        }
        return Pair.of(oldChunks, newChunks);
    }

    public static ChunkJson parseChunkJson(File file) throws IOException {
        return JsonUtils.getMapper().readValue(file, ChunkJson.class);
    }

    public static IVector2 chunkIndexToGlobal(IVector2 chunkIndex) {
        return IVector2.of(chunkIndex).multiply(WorldSettings.GET().chunkSize());
    }

    public static int areaTileCount(IRect2 area) {
       return (area.size().x() / 32) * (area.size().y() / 32);
    }




}
