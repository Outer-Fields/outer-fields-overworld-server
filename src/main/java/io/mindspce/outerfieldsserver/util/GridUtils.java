package io.mindspce.outerfieldsserver.util;

import io.mindspce.outerfieldsserver.area.ChunkJson;
import io.mindspce.outerfieldsserver.data.wrappers.ChunkTileIndex;
import io.mindspce.outerfieldsserver.area.TileData;
import io.mindspce.outerfieldsserver.core.GameSettings;
import io.mindspice.mindlib.data.geometry.IVector2;
import io.mindspice.mindlib.data.tuples.Pair;
import io.mindspice.mindlib.util.JsonUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class GridUtils {

//    public static IVector2 calcChunkPos(IVector2 chunkIndex) {
//        return chunkIndex.multiply(GameSettings.GET().chunkSize());
//    }

//    public static IVector2 calcChunkIndex(IVector2 pos) {
//        return pos.divide(GameSettings.GET().chunkSize());
//    }

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
                (chunk.x() * GameSettings.GET().chunkSize().x()) + (tile.x() * GameSettings.GET().tileSize()),
                (chunk.y() * GameSettings.GET().chunkSize().y()) + (tile.y() * GameSettings.GET().tileSize())
        );
    }

    public static IVector2 globalToLocalTile(IVector2 chunkIndex, IVector2 globalPos) {
        return IVector2.of(
                (globalPos.x() - chunkIndex.x() * GameSettings.GET().chunkSize().x()) / GameSettings.GET().tileSize(),
                (globalPos.y() - chunkIndex.y() * GameSettings.GET().chunkSize().y()) / GameSettings.GET().tileSize()
        );
    }

    public static IVector2 globalToChunk(IVector2 globalPos) {
        int chunkX = globalPos.x() / GameSettings.GET().chunkSize().x();
        int chunky = globalPos.y() / GameSettings.GET().chunkSize().y();
        return IVector2.of(chunkX, chunky);
    }

    public static ChunkTileIndex globalToChunkTile(IVector2 globalPos) {
        int chunkX = globalPos.x() / GameSettings.GET().chunkSize().x();
        int chunky = globalPos.y() / GameSettings.GET().chunkSize().y();
        int tileXPos = globalPos.x() % GameSettings.GET().chunkSize().x();
        int tileYPos = globalPos.y() % GameSettings.GET().chunkSize().y();
        int tileX = tileXPos / GameSettings.GET().tileSize();
        int tileY = tileYPos / GameSettings.GET().tileSize();
        return new ChunkTileIndex(IVector2.of(chunkX, chunky), IVector2.of(tileX, tileY));
    }

    public static boolean isNewChunk(IVector2 lastChunk, IVector2 globalPos) {
        int currChunkX = globalPos.x() / GameSettings.GET().chunkSize().x();
        int currChunkY = globalPos.y() / GameSettings.GET().chunkSize().y();
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
        return IVector2.of(chunkIndex).multiply(GameSettings.GET().chunkSize());
    }




}
