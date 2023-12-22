package io.mindspce.outerfieldsserver.enums;

import io.mindspice.mindlib.data.geometry.IMutVector2;
import io.mindspice.mindlib.data.geometry.IVector2;

import java.util.List;
import java.util.Map;


public enum Direction {
    NORTH(IVector2.of(0, -1)),
    SOUTH(IVector2.of(0, 1)),
    EAST(IVector2.of(1, 0)),
    WEST(IVector2.of(-1, 0)),
    NORTH_EAST(IVector2.of(1, -1)),
    NORTH_WEST(IVector2.of(-1, -1)),
    SOUTH_EAST(IVector2.of(1, 1)),
    SOUTH_WEST(IVector2.of(-1, 1)),
    CENTER(IVector2.of(0, 0));

    private final IVector2 vector2;
    private static final Direction[][] directions;

    static {
        directions = new Direction[][]{
                {NORTH_WEST, NORTH, NORTH_EAST},
                {WEST, CENTER, EAST},
                {SOUTH_WEST, SOUTH, SOUTH_EAST}
        };
    }

    ;
    private static final List<Direction> fourPointList = List.of(NORTH, SOUTH, EAST, WEST);
    private static final List<Direction> eightPointList = List.of(
            NORTH, NORTH_EAST, EAST, SOUTH_EAST, SOUTH, SOUTH_WEST, WEST, NORTH_WEST
    );

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

    public static Direction getDirectionOf(int currX, int currY, int targetX, int targetY) {
        int deltaX = currX - targetX;
        int deltaY = currY - targetY;

        // Adjust deltaX and deltaY to be -1, 0, or 1
        deltaX = Integer.compare(deltaX, 0);
        deltaY = Integer.compare(deltaY, 0);

        // Access the directions array with adjusted indices
        System.out.println(deltaX + " | " + deltaY);
        return directions[deltaY + 1][deltaX + 1];
    }
    public static Direction getDirectionOf(IVector2 target, IVector2 current) {
        return getDirectionOf(target.x(), target.y(), current.x(), current.y());
    }
}
