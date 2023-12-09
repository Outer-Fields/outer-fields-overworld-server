package io.mindspce.outerfieldsserver.util;

import io.mindspce.outerfieldsserver.datacontainers.ChunkTileIndex;
import io.mindspce.outerfieldsserver.core.NavCalc.GameSettings;
import io.mindspice.mindlib.data.geometry.IVector2;

import org.junit.jupiter.api.Test;

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
}