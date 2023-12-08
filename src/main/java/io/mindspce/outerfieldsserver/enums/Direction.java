package io.mindspce.outerfieldsserver.enums;

import io.mindspice.mindlib.data.geometry.IVector2;

import java.util.List;


public enum Direction {
    NORTH(IVector2.of(0, -1)),
    SOUTH(IVector2.of(0, 1)),
    EAST(IVector2.of(-1, 0)),
    WEST(IVector2.of(1, 0)),
    NORTH_EAST(IVector2.of(1, -1)),
    NORTH_WEST(IVector2.of(-1, -1)),
    SOUTH_EAST(IVector2.of(1, 1)),
    SOUTH_WEST(IVector2.of(-1, 1));

    private final IVector2 vector2;

    private static final List<Direction> fourPointList = List.of(NORTH, SOUTH, EAST, WEST);

    Direction(IVector2 vector2) { this.vector2 = vector2; }

    public IVector2 asVec2() {
        return vector2;
    }

    public static List<Direction> get4PointList() {
        return fourPointList;
    }
}
