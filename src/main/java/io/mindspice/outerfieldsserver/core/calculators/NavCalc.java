package io.mindspice.outerfieldsserver.core.calculators;

import io.mindspice.outerfieldsserver.entities.AreaEntity;
import io.mindspice.outerfieldsserver.entities.ChunkEntity;
import io.mindspice.outerfieldsserver.area.TileData;
import io.mindspice.outerfieldsserver.core.WorldSettings;
import io.mindspice.outerfieldsserver.enums.Direction;
import io.mindspice.outerfieldsserver.data.wrappers.ChunkTileIndex;
import io.mindspice.outerfieldsserver.util.GridUtils;
import io.mindspice.mindlib.data.geometry.IMutVector2;
import io.mindspice.mindlib.data.geometry.IVector2;

import java.util.*;


/**
 * The NavigationCalculator class provides methods for calculating paths between two points in a given area.
 */
public class NavCalc {
    private static final int CHUNK_SIZE = WorldSettings.GET().chunkSize().x();

    /**
     * Performs A* pathfinding algorithm to find the shortest path between the start and target chunk
     * tile indices in the given area instance.
     *
     * @param area   The area instance to perform pathfinding in.
     * @param start  The starting chunk tile index.
     * @param target The target chunk tile index.
     * @return The list of global tile indices representing the shortest path from the start to the target.
     */
    public static List<IVector2> getPathTo(AreaEntity area, IVector2 start, IVector2 target, float speed) {
        Map<ChunkTileIndex, ChunkTileIndex> cameFrom = new HashMap<>();
        Map<ChunkTileIndex, Integer> costSoFar = new HashMap<>();

        PriorityQueue<Node> frontier = new PriorityQueue<>(Comparator.comparingInt(Node::priority));
        ChunkTileIndex origin = GridUtils.globalToChunkTile(start);
        ChunkTileIndex destin = GridUtils.globalToChunkTile(target);

        frontier.add(new Node(origin, 1));
        costSoFar.put(origin, 0);

        while (!frontier.isEmpty()) {
            Node current = frontier.poll();
            ChunkTileIndex currentTileIndex = current.chunkTile();

            if (currentTileIndex.equals(target)) {
                break;
            }

            for (ChunkTileIndex next : getNeighbors(area, currentTileIndex)) {
                int newCost = costSoFar.get(currentTileIndex) + 1; // Assuming uniform cost
                if (!costSoFar.containsKey(next) || newCost < costSoFar.get(next)) {
                    costSoFar.put(next, newCost);
                    int priority = newCost + manhattanDistance(currentTileIndex, next);
                    frontier.add(new Node(next, priority));
                    cameFrom.put(next, currentTileIndex);
                }
            }
        }

        return interpolatePath(reconstructPath(cameFrom, origin, destin, start, target), speed);
    }

    /**
     * Retrieves the neighboring ChunkTileIndex objects for a given ChunkTileIndex within an AreaInstance.
     * Neighbors are determined based on the 4-point compass directions (NORTH, SOUTH, EAST, WEST).
     *
     * @param area    The AreaInstance being analyzed.
     * @param current The ChunkTileIndex for which neighbors are being retrieved.
     * @return A List of ChunkTileIndex objects representing the neighboring tiles.
     */
    // TODO at 8 point calc?
    private static List<ChunkTileIndex> getNeighbors(AreaEntity area, ChunkTileIndex current) {
        List<ChunkTileIndex> neighbors = new ArrayList<>();
        int tilesPerChunk = WorldSettings.GET().tilesPerChunk().x(); // Assuming square chunks

        for (var dir : Direction.get8PointList()) {
            IMutVector2 neighborTileIndex = IVector2.ofMutable(
                    current.tileIndex().x() + dir.asVec2().x(),
                    current.tileIndex().y() + dir.asVec2().y()
            );
            IMutVector2 neighborChunkIndex = IVector2.ofMutable(current.chunkIndex());

            // Adjust chunk index if the neighbor tile index is outside the current chunk
            boolean crossesBoundary = false;
            if (neighborTileIndex.x() < 0) {
                neighborChunkIndex.withXDec(); // Move chunk to the left
                neighborTileIndex.setX(tilesPerChunk - 1);
                crossesBoundary = true;
            } else if (neighborTileIndex.x() >= tilesPerChunk) {
                neighborChunkIndex.withXInc(); // Move chunk to the right
                neighborTileIndex.setX(0);
                crossesBoundary = true;
            }

            if (neighborTileIndex.y() < 0) {
                neighborChunkIndex.withYDec(); // Move chunk upwards
                neighborTileIndex.setY(tilesPerChunk - 1);
                crossesBoundary = true;
            } else if (neighborTileIndex.y() >= tilesPerChunk) {
                neighborChunkIndex.withYInc(); // Move chunk downwards
                neighborTileIndex.setY(0);
                crossesBoundary = true;
            }

            // If the tile index crosses the boundary, ensure the chunk index is still valid
            if (crossesBoundary) {
                if (neighborChunkIndex.x() < 0 || neighborChunkIndex.x() >= area.getAreaSize().x() ||
                        neighborChunkIndex.y() < 0 || neighborChunkIndex.y() >= area.getAreaSize().y()) {
                    continue; // Skip if the neighbor chunk is out of bounds
                }
            }

            ChunkEntity neighborChunk = area.getChunkByIndex(neighborChunkIndex);
            if (neighborChunk == null) {
                // TODO log?
                continue;
            }
            TileData neighborTile = neighborChunk.getTileByIndex(neighborTileIndex);
            if (neighborTile == null) {
                // TODO log?
                continue;
            }

            if (neighborTile.isNavigable()) {
                neighbors.add(new ChunkTileIndex(neighborChunkIndex, neighborTileIndex));
            }
        }

        return neighbors;
    }

    /**
     * Calculates the Manhattan distance between two ChunkTileIndex objects.
     * Manhattan Distance is the sum of the absolute values of the differences of the Cartesian coordinates.
     *
     * @param index1 The first ChunkTileIndex object.
     * @param index2 The second ChunkTileIndex object.
     * @return The Manhattan distance between the two ChunkTileIndex objects.
     */
    private static int manhattanDistance(ChunkTileIndex index1, ChunkTileIndex index2) {
        IVector2 globalPos1 = IVector2.of(
                index1.chunkIndex().x() * WorldSettings.GET().chunkSize().x() + index1.tileIndex().x(),
                index1.chunkIndex().y() * WorldSettings.GET().chunkSize().y() + index1.tileIndex().y()
        );

        IVector2 globalPos2 = IVector2.of(
                index2.chunkIndex().x() * WorldSettings.GET().chunkSize().x() + index2.tileIndex().x(),
                index2.chunkIndex().y() * WorldSettings.GET().chunkSize().y() + index2.tileIndex().y()
        );

        return Math.abs(globalPos1.x() - globalPos2.x()) + Math.abs(globalPos1.y() - globalPos2.y());
    }

    /**
     * Reconstructs the path from the start ChunkTileIndex to the goal ChunkTileIndex using the
     * provided map of cameFrom relationships. Returns coordinates as an array of IVector2.
     *
     * @param cameFrom The map of ChunkTileIndex objects indicating the previous ChunkTileIndex for each
     *                 visited ChunkTileIndex during the pathfinding.
     * @param start    The starting ChunkTileIndex.
     * @param goal     The goal ChunkTileIndex.
     * @return The list of global tile indices representing the shortest path from the start to the goal.
     */
    private static List<IVector2> reconstructPath(Map<ChunkTileIndex, ChunkTileIndex> cameFrom,
            ChunkTileIndex start, ChunkTileIndex goal, IVector2 origin, IVector2 destin) {

        LinkedList<IVector2> path = new LinkedList<>();
        ChunkTileIndex current = goal;
        path.addFirst(destin);
        while (!current.equals(start)) {
            path.addFirst(chunkToVector(current));
            current = cameFrom.get(current);
            if (current == null) {
                // TODO log this
                return List.of();
            }
        }
       // path.addFirst(chunkToVector(start));
        path.addFirst(origin);
        return path;
    }

    /**
     * Fuzzes a tile by adding random offsets to its position, to make movement more natural
     *
     * @param tileChunk The ChunkTileIndex of the tile to fuzz.
     * @return The fuzzed position of the tile.
     */

    private static IVector2 chunkToVector(ChunkTileIndex tileChunk) {
        IVector2 chunk = tileChunk.chunkIndex();
        IVector2 tile = tileChunk.tileIndex();

        IVector2 pos = IVector2.ofMutable(
                (chunk.x() * WorldSettings.GET().chunkSize().x()) + (tile.x() * WorldSettings.GET().tileSize()),
                (chunk.y() * WorldSettings.GET().chunkSize().y()) + (tile.y() * WorldSettings.GET().tileSize())
        ).add(16, 16);

//        if (ThreadLocalRandom.current().nextFloat(1) < 0.2) {
//            pos.add(
//                    ThreadLocalRandom.current().nextInt(-8, 8),
//                    ThreadLocalRandom.current().nextInt(-8, 8)
//            );
//        }

        return pos;
    }

    private record Node(
            ChunkTileIndex chunkTile,
            int priority
    ) { }

    public static List<IVector2> interpolatePath(List<IVector2> originalPath, float speed) {
        List<IVector2> interpolatedPath = new LinkedList<>();
        float stepSize = speed * (1.0f / WorldSettings.GET().tickRate());


        for (int i = 0; i < originalPath.size() - 1; i++) {
            IVector2 start = originalPath.get(i);
            IVector2 end = originalPath.get(i + 1);

            // Add the start position
            interpolatedPath.add(start);

            float distance = start.distanceTo(end);
            int steps = Math.max(1, (int) (distance / stepSize));

            for (int step = 0; step < steps; step++) {
                float t = step / (float) steps;
                interpolatedPath.add(IVector2.lerp(start, end, t));
            }
        }

        // Add the last position

        return interpolatedPath;
    }
}