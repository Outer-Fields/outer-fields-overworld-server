package io.mindspce.outerfieldsserver.core.calculators;

import io.mindspce.outerfieldsserver.area.AreaInstance;
import io.mindspce.outerfieldsserver.area.ChunkData;
import io.mindspce.outerfieldsserver.area.TileData;
import io.mindspce.outerfieldsserver.core.configuration.GameSettings;
import io.mindspce.outerfieldsserver.enums.Direction;
import io.mindspce.outerfieldsserver.util.ChunkTileIndex;
import io.mindspce.outerfieldsserver.util.GridUtils;
import io.mindspice.mindlib.data.geometry.IMutVector2;
import io.mindspice.mindlib.data.geometry.IVector2;

import java.util.*;


public class NavigationCalculator {
    private static final int CHUNK_SIZE = GameSettings.GET().chunkSize().x();

    public static List<IVector2> getPathTo(AreaInstance area, ChunkTileIndex start, ChunkTileIndex target) {

        return aStarPathfinding(area, start, target);
    }

    private static List<IVector2> aStarPathfinding(AreaInstance area, ChunkTileIndex start, ChunkTileIndex target) {
        Map<ChunkTileIndex, ChunkTileIndex> cameFrom = new HashMap<>();
        Map<ChunkTileIndex, Integer> costSoFar = new HashMap<>();
        PriorityQueue<Node> frontier = new PriorityQueue<>(Comparator.comparingInt(Node::priority));

        frontier.add(new Node(start, 0));
        costSoFar.put(start, 0);

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

        return reconstructPath(cameFrom, start, target);
    }

    private static List<ChunkTileIndex> getNeighbors(AreaInstance area, ChunkTileIndex current) {
        List<ChunkTileIndex> neighbors = new ArrayList<>();
        int tilesPerChunk = GameSettings.GET().tilesPerChunk().x(); // Assuming square chunks

        for (var dir : Direction.get4PointList()) {
            IMutVector2 neighborTileIndex = IVector2.ofMutable(
                    current.tileIndex().x() + dir.asVec2().x(),
                    current.tileIndex().y() + dir.asVec2().y()
            );
            IMutVector2 neighborChunkIndex = IVector2.ofMutable(current.chunkIndex());

            // Adjust chunk index if the neighbor tile index is outside the current chunk
            if (neighborTileIndex.x() < 0) {
                neighborChunkIndex.withXDec(); // Move chunk to the left
                neighborTileIndex.setX(tilesPerChunk - 1);
            } else if (neighborTileIndex.x() >= tilesPerChunk) {
                neighborChunkIndex.withXInc(); // Move chunk to the right
                neighborTileIndex.setX(0);
            }

            if (neighborTileIndex.y() < 0) {
                neighborChunkIndex.withYDec(); // Move chunk upwards
                neighborTileIndex.setY(tilesPerChunk - 1);
            } else if (neighborTileIndex.y() >= tilesPerChunk) {
                neighborChunkIndex.withYInc(); // Move chunk downwards
                neighborTileIndex.setY(0);
            }

            // Check if the calculated neighbor chunk index is within area bounds
            if (neighborChunkIndex.x() < 0 || neighborChunkIndex.x() >= area.getAreaSize().x() ||
                    neighborChunkIndex.y() < 0 || neighborChunkIndex.y() >= area.getAreaSize().y()) {
                continue; // Skip if the neighbor chunk is out of bounds
            }

            ChunkData neighborChunk = area.getChunkByIndex(neighborChunkIndex);
            TileData neighborTile = neighborChunk.getTileByIndex(neighborTileIndex);

            if (neighborTile.navData().isNavigable()) {
                neighbors.add(new ChunkTileIndex(neighborChunkIndex, neighborTileIndex));
            }
        }

        return neighbors;
    }

    private static int manhattanDistance(ChunkTileIndex index1, ChunkTileIndex index2) {
        IVector2 globalPos1 = IVector2.of(
                index1.chunkIndex().x() * GameSettings.GET().chunkSize().x() + index1.tileIndex().x(),
                index1.chunkIndex().y() * GameSettings.GET().chunkSize().y() + index1.tileIndex().y()
        );

        IVector2 globalPos2 = IVector2.of(
                index2.chunkIndex().x() * GameSettings.GET().chunkSize().x() + index2.tileIndex().x(),
                index2.chunkIndex().y() * GameSettings.GET().chunkSize().y() + index2.tileIndex().y()
        );

        return Math.abs(globalPos1.x() - globalPos2.x()) + Math.abs(globalPos1.y() - globalPos2.y());
    }

    private static ArrayList<IVector2> reconstructPath(Map<ChunkTileIndex, ChunkTileIndex> cameFrom,
            ChunkTileIndex start,
            ChunkTileIndex goal) {



        ArrayList<IVector2> path = new ArrayList<>();
        ChunkTileIndex current = goal;
        while (!current.equals(start)) {
            path.addFirst(GridUtils.tileToGlobal(current));
            current = cameFrom.get(current);
        }
        path.addFirst(GridUtils.tileToGlobal(start));
        return path;
    }

    private record Node(
            ChunkTileIndex chunkTile,
            int priority
    ) { }
}

//    public static List<TileData> getPathTo(AreaInstance area, IVector2 startingChunk,
//            IVector2 startingTile, IVector2 targetChunk, IVector2 targetTile) {
//
//        IVector2 globalStart = GridUtils.tileToGlobal(startingChunk, startingTile);
//        IVector2 globalTarget = GridUtils.tileToGlobal(targetChunk, targetTile);
//        return aStarPathfinding(area, globalStart, globalTarget);
//    }
//
//    private static List<IVector2> aStarPathfinding(AreaInstance area, IVector2 start, IVector2 target) {
//        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingInt(n -> n.priority));
//        Map<IVector2, IVector2> cameFrom = new HashMap<>();
//        Map<IVector2, Integer> costSoFar = new HashMap<>();
//
//        openSet.add(new Node(start, 0));
//        costSoFar.put(start, 0);
//
//        while (!openSet.isEmpty()) {
//            IVector2 current = openSet.poll().position;
//
//            if (current.equals(target)) {
//                break; // Path found
//            }
//
//            for (IVector2 next : getNeighbors(area, current)) {
//                int newCost = costSoFar.get(current) + getMovementCost(area, current, next);
//                if (!costSoFar.containsKey(next) || newCost < costSoFar.get(next)) {
//                    costSoFar.put(next, newCost);
//                    int priority = newCost + manhattanDistance(next, target);
//                    openSet.add(new Node(next, priority));
//                    cameFrom.put(next, current);
//                }
//            }
//        }
//
//        return reconstructPath(cameFrom, start, target);
//    }
//
//    private static int getMovementCost(AreaInstance area, IVector2 from, IVector2 to) {
//        return 1;
//    }
//
//    private static List<IVector2> getNeighbors(AreaInstance area, IVector2 current) {
//        List<IVector2> neighbors = new ArrayList<>();
//
//        // Assuming CHUNK_SIZE is the dimension of each chunk
//        IVector2 chunk = new IVector2(current.x() / CHUNK_SIZE, current.y() / CHUNK_SIZE);
//        IVector2 tile = new IVector2(current.x() % CHUNK_SIZE, current.y() % CHUNK_SIZE);
//
//        // Directions: up, down, left, right
//        int[][] directions = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
//        for (int[] dir : directions) {
//            int nx = tile.x() + dir[0];
//            int ny = tile.y() + dir[1];
//            IVector2 nextChunk = chunk;
//
//            // Check for chunk boundaries and adjust chunk coordinates
//            if (nx < 0) {
//                nextChunk = new IVector2(chunk.x() - 1, chunk.y());
//                nx = CHUNK_SIZE - 1; // Last tile of the previous chunk
//            } else if (nx >= CHUNK_SIZE) {
//                nextChunk = new IVector2(chunk.x() + 1, chunk.y());
//                nx = 0; // First tile of the next chunk
//            }
//            if (ny < 0) {
//                nextChunk = new IVector2(chunk.x(), chunk.y() - 1);
//                ny = CHUNK_SIZE - 1; // Last tile of the previous chunk
//            } else if (ny >= CHUNK_SIZE) {
//                nextChunk = new IVector2(chunk.x(), chunk.y() + 1);
//                ny = 0; // First tile of the next chunk
//            }
//
//            // Convert back to global coordinates
//            IVector2 globalNeighbor = GridUtils.tileToGlobal(nextChunk, new IVector2(nx, ny));
//            neighbors.add(globalNeighbor);
//        }
//
//        return neighbors;
//    }
//
//    private static int manhattanDistance(IVector2 a, IVector2 b) {
//        return Math.abs(a.x() - b.x()) + Math.abs(a.y() - b.y());
//    }
//
//    private static List<IVector2> reconstructPath(Map<IVector2, IVector2> cameFrom, IVector2 start, IVector2 target) {
//        LinkedList<IVector2> path = new LinkedList<>();
//        IVector2 current = target;
//
//        while (!current.equals(start)) {
//            path.addFirst(current); // Add to the beginning of the list
//            current = cameFrom.get(current);
//        }
//        path.addFirst(start); // Add the start position
//        return path;
//    }
//
//    private static record Node(
//            IVector2 position,
//            int priority
//    ) { }
//}
