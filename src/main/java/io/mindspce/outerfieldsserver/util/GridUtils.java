package io.mindspce.outerfieldsserver.util;

import io.mindspce.outerfieldsserver.datacontainers.ChunkTileIndex;
import io.mindspce.outerfieldsserver.area.TileData;
import io.mindspce.outerfieldsserver.core.GameSettings;
import io.mindspice.mindlib.data.geometry.IVector2;


public class GridUtils {

    public static IVector2 calcChunkPos(IVector2 chunkIndex) {
        return chunkIndex.multiply(GameSettings.GET().chunkSize());
    }

    public static IVector2 calcChunkIndex(IVector2 pos) {
        return pos.divide(GameSettings.GET().chunkSize());
    }

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

    public static boolean

}
