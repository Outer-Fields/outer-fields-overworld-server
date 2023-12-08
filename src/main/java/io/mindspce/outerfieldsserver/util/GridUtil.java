package io.mindspce.outerfieldsserver.util;

import io.mindspce.outerfieldsserver.core.state.configuration.GameSettings;
import io.mindspice.mindlib.data.geometry.IVector2;


public class GridUtil {

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


}
