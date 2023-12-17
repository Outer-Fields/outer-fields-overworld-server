package io.mindspce.outerfieldsserver.navigation;

import io.mindspce.outerfieldsserver.area.AreaInstance;
import io.mindspce.outerfieldsserver.area.ChunkData;
import io.mindspce.outerfieldsserver.area.NavData;
import io.mindspce.outerfieldsserver.area.TileData;
import io.mindspce.outerfieldsserver.core.calculators.NavCalc;
import io.mindspce.outerfieldsserver.core.GameSettings;
import io.mindspce.outerfieldsserver.enums.AreaId;
import io.mindspce.outerfieldsserver.enums.NavPath;
import io.mindspce.outerfieldsserver.data.wrappers.ChunkTileIndex;
import io.mindspce.outerfieldsserver.util.GridUtils;
import io.mindspice.mindlib.data.geometry.IVector2;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;


public class NavigationTest {

    @Test
    void testNavigation() {
//        var tilesPerChunk = GameSettings.GET().tilesPerChunk().x();
//        TileData[][] tiles1 = new TileData[tilesPerChunk][tilesPerChunk];
//        TileData[][] tiles2 = new TileData[tilesPerChunk][tilesPerChunk];
//
//        for (int x = 0; x < tilesPerChunk; ++x) {
//            for (int y = 0; y < tilesPerChunk; ++y) {
//                if (y == 0 && x == 21) {
//                    tiles1[x][y] = new TileData(IVector2.of(x, y), new NavData(Set.of(NavPath.TEST)));
//                } else {
//                    tiles1[x][y] = new TileData(IVector2.of(x, y), x == 20 ? new NavData(Set.of(NavPath.TEST)) : new NavData());
//                }
//
//            }
//        }
//
//        for (int x = 0; x < tilesPerChunk; ++x) {
//            for (int y = 0; y < tilesPerChunk; ++y) {
//                tiles2[x][y] = new TileData(IVector2.of(x, y), x == 21 ? new NavData(Set.of(NavPath.TEST)) : new NavData());
//            }
//        }
//
//        ChunkData chunkTop = new ChunkData(IVector2.of(0,0), tiles2);
//        ChunkData chunkBottom = new ChunkData(IVector2.of(0,1), tiles1);
//
//        GridUtils.printNavMap(tiles2);
//
//        System.out.println("\t\t\t");
//        GridUtils.printNavMap(tiles1);
//        AreaInstance area = new AreaInstance("test", new ChunkData[][]{{chunkTop, chunkBottom}});
//
//        ChunkTileIndex curr = new ChunkTileIndex(IVector2.of(0,1),IVector2.of(20, 50));
//        ChunkTileIndex target = new ChunkTileIndex(IVector2.of(0,0),IVector2.of(21, 10));
//        System.out.println(NavigationCalculator.getPathTo(area, curr, target));

        int tilesPerChunk = GameSettings.GET().tilesPerChunk().x();
        TileData[][] tiles1 = new TileData[tilesPerChunk][tilesPerChunk]; // chunkBottom
        TileData[][] tiles2 = new TileData[tilesPerChunk][tilesPerChunk]; // chunkTop

// Create zigzag paths in chunkBottom and chunkTop
        for (int x = 0; x < tilesPerChunk; ++x) {
            for (int y = 0; y < tilesPerChunk; ++y) {
                boolean isNavigable = (x % 3 == 0 && y < 5) || (x % 3 == 1 && y >= 5);
                tiles1[x][y] = new TileData(IVector2.of(x, y), isNavigable ? new NavData(Set.of(NavPath.TEST)) : null);
                tiles2[x][y] = new TileData(IVector2.of(x, y), isNavigable ? new NavData(Set.of(NavPath.TEST)) : null);
            }
        }

        GridUtils.printNavMap(tiles2);

        System.out.println("\t\t\t");
        GridUtils.printNavMap(tiles1);
        ChunkData chunkTop = new ChunkData(IVector2.of(0, 0), tiles2, null);
        ChunkData chunkBottom = new ChunkData(IVector2.of(0, 1), tiles1, null);
        AreaInstance area = new AreaInstance(AreaId.TEST, new ChunkData[][]{{chunkTop, chunkBottom}});

        ChunkTileIndex curr = new ChunkTileIndex(IVector2.of(0, 1), IVector2.of(4, 59)); // Start in chunkBottom
        ChunkTileIndex target = new ChunkTileIndex(IVector2.of(0, 0), IVector2.of(4, 59)); // End in chunkTop
        int [] tArr = new int[100];
        for (int i = 0; i < 100; ++i) {
            var t = System.nanoTime();
            var path = NavCalc.getPathTo(area, curr, target);
            tArr[i] = (int) (System.nanoTime() - t);
            System.out.println(path);
        }
        System.out.println((int) Arrays.stream(tArr).average().getAsDouble());
    }
}
