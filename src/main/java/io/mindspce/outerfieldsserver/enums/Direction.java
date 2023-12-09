package io.mindspce.outerfieldsserver.enums;

import io.mindspice.mindlib.data.geometry.IMutVector2;
import io.mindspice.mindlib.data.geometry.IVector2;

import java.util.List;
import java.util.Map;


public enum Direction {
    NORTH(IVector2.of(0, -1)),
    SOUTH(IVector2.of(0, 1)),
    EAST(IVector2.of(-1, 0)),
    WEST(IVector2.of(1, 0)),
    NORTH_EAST(IVector2.of(1, -1)),
    NORTH_WEST(IVector2.of(-1, -1)),
    SOUTH_EAST(IVector2.of(1, 1)),
    SOUTH_WEST(IVector2.of(-1, 1)),
    CENTER(IVector2.of(0, 0));

    private final IVector2 vector2;

    private static final List<Direction> fourPointList = List.of(NORTH, SOUTH, EAST, WEST);
    private static final List<Direction> eightPointList = List.of(
            NORTH, NORTH_EAST, EAST, SOUTH_EAST, SOUTH, SOUTH_WEST, WEST, NORTH_WEST
    );
    private static final Map<IVector2, Direction> directionMap = Map.of(
            NORTH.vector2, NORTH,
            SOUTH.vector2, SOUTH,
            EAST.vector2, EAST,
            WEST.vector2, WEST,
            NORTH_EAST.vector2, NORTH_EAST,
            NORTH_WEST.vector2, NORTH_WEST,
            SOUTH_EAST.vector2, SOUTH_EAST,
            SOUTH_WEST.vector2, SOUTH_WEST

    );
    private static final IMutVector2 cacheVector = IVector2.ofMutable(0, 0);

    Direction(IVector2 vector2) { this.vector2 = vector2; }

    public IVector2 asVec2() {
        return vector2;
    }

    public static List<Direction> get4PointList() {
        return fourPointList;
    }

    public static List<Direction> get8PointList() {
        return eightPointList;
    }

    public static Map<IVector2, Direction> getDirectionMap() {
        return directionMap;
    }

    public static Direction getDirectionOf(int targetX, int targetY, int currentX, int currentY) {
        int deltaX = Integer.compare(targetX - currentX, 0);
        int deltaY = Integer.compare(targetY - currentY, 0);
        cacheVector.setXY(deltaX, deltaY);
        return directionMap.get(cacheVector);
    }

    public static Direction getDirectionOf(IVector2 target, IVector2 current) {
        return getDirectionOf(target.x(), target.y(), current.x(), current.y());
    }
}
