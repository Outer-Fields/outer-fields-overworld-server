package io.mindspce.outerfieldsserver.util;

import io.mindspce.outerfieldsserver.data.wrappers.ChunkTileIndex;
import io.mindspce.outerfieldsserver.core.GameSettings;
import io.mindspice.mindlib.data.geometry.IAtomicRect2;
import io.mindspice.mindlib.data.geometry.IMutRect2;
import io.mindspice.mindlib.data.geometry.IRect2;
import io.mindspice.mindlib.data.geometry.IVector2;

import io.mindspice.mindlib.data.tuples.Pair;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * This class tests the tileToGlobal method of the GridUtils class.
 * The tileToGlobal method converts a chunk TileIndex to a global vector position in the game's grid.
 */
public class GridUtilsTest {

    @Test
    public void testTileToGlobal() {
        // Given a chunk and a tile within the chunk
        IVector2 chunk = IVector2.of(2, 3);
        IVector2 tile = IVector2.of(1, 1);
        ChunkTileIndex chunkTileIndex = new ChunkTileIndex(chunk, tile);

        // When we convert that to a global position using GridUtils.tileToGlobal
        IVector2 result = GridUtils.tileToGlobal(chunkTileIndex);

        // Then the result should be as expected
        int tileSize = GameSettings.GET().tileSize();
        int chunkSize = GameSettings.GET().chunkSize().x();  // Assuming square chunks
        IVector2 expected = IVector2.of(
                (chunk.x() * chunkSize + tile.x() * tileSize),
                (chunk.y() * chunkSize + tile.y() * tileSize)
        );
        assertEquals(expected, result, "The method GridUtils.tileToGlobal did not calculate the global position correctly");
    }

    @Test
    public void testGridShiftCalc() {
//
//        IVector2[][] grid = new IVector2[3][3];
//        IVector2[][] grid2 = new IVector2[3][3];
//        for (int i = 0; i < 3; ++i) {
//            for (int j = 0; j < 3; ++j) {
//                grid[i][j] = IVector2.of(i, j);
//            }
//        }
//        for (int i = 0; i < 3; ++i) {
//            for (int j = 0; j < 3; ++j) {
//                grid2[i][j] = IVector2.of(i + 1, j + 1);
//            }
//        }
//        var chunksize = GameSettings.GET().chunkSize();
        IAtomicRect2 r1 = IRect2.fromCenterAtomic(IVector2.of(0, 0), GameSettings.GET().playerViewWithBuffer().add(GameSettings.GET().playerViewBuffer()));
        IRect2 r2 = IRect2.of(GameSettings.GET().playerViewBuffer(), GameSettings.GET().playerViewWithBuffer());

//        var t = System.nanoTime();
//        boolean[] b = new boolean[4];

        IVector2 vec = IVector2.ofMutable(0, 0);
        Set<String> r1List = new HashSet<>(100000);
        Set<String> r2List = new HashSet<>(100000);
        var t = System.nanoTime();
        for (int i = 0; i < 10_000; ++i) {
            for (int j = 0; j < 10_000; ++j) {
                if (r1.contains(vec)) {
                    r1List.add("F");
                }
                r1.reCenter(i / 10, j / 10);
            }

        }
        System.out.println((System.nanoTime() - t) / (10_000 * 10_000));
        System.out.println(r1List);


    }
}