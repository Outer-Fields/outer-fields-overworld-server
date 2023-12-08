package io.mindspce.outerfieldsserver.navigation;

import io.mindspce.outerfieldsserver.area.AreaInstance;
import io.mindspce.outerfieldsserver.area.ChunkData;
import io.mindspce.outerfieldsserver.area.NavData;
import io.mindspce.outerfieldsserver.area.TileData;
import io.mindspce.outerfieldsserver.core.calculators.NavigationCalculator;
import io.mindspce.outerfieldsserver.core.configuration.GameSettings;
import io.mindspce.outerfieldsserver.enums.NavPath;
import io.mindspce.outerfieldsserver.util.ChunkTileIndex;
import io.mindspce.outerfieldsserver.util.GridUtils;
import io.mindspice.mindlib.data.geometry.IVector2;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.h2.H2ConsoleProperties;

import java.util.Arrays;
import java.util.Set;


public class NavigationTest {

    @Test
    void testNavigation() {
        var tilesPerChunk = GameSettings.GET().tilesPerChunk().x();
        TileData[][] tiles1 = new TileData[tilesPerChunk][tilesPerChunk];
        TileData[][] tiles2 = new TileData[tilesPerChunk][tilesPerChunk];

        for (int x = 0; x < tilesPerChunk; ++x) {
            for (int y = 0; y < tilesPerChunk; ++y) {
                if (y == 0 && x == 21) {
                    tiles1[x][y] = new TileData(IVector2.of(x, y), new NavData(Set.of(NavPath.TEST)));
                } else {
                    tiles1[x][y] = new TileData(IVector2.of(x, y), x == 20 ? new NavData(Set.of(NavPath.TEST)) : new NavData());
                }

            }
        }

        for (int x = 0; x < tilesPerChunk; ++x) {
            for (int y = 0; y < tilesPerChunk; ++y) {
                tiles2[x][y] = new TileData(IVector2.of(x, y), x == 21 ? new NavData(Set.of(NavPath.TEST)) : new NavData());
            }
        }

        ChunkData chunkTop = new ChunkData(IVector2.of(0,0), tiles2);
        ChunkData chunkBottom = new ChunkData(IVector2.of(0,1), tiles1);

        GridUtils.printNavMap(tiles2);

        System.out.println("\t\t\t");
        GridUtils.printNavMap(tiles1);
        AreaInstance area = new AreaInstance("test", new ChunkData[][]{{chunkTop, chunkBottom}});

        ChunkTileIndex curr = new ChunkTileIndex(IVector2.of(0,1),IVector2.of(20, 50));
        ChunkTileIndex target = new ChunkTileIndex(IVector2.of(0,0),IVector2.of(21, 10));
        System.out.println(NavigationCalculator.getPathTo(area, curr, target));
    }
}
