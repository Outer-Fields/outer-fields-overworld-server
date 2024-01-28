package io.mindspice.outerfieldsserver.core.authority;

import io.mindspice.outerfieldsserver.entities.AreaEntity;
import io.mindspice.outerfieldsserver.core.WorldSettings;
import io.mindspice.outerfieldsserver.data.wrappers.DynamicTileRef;
import io.mindspice.mindlib.data.collections.other.GridArray;
import io.mindspice.mindlib.data.geometry.*;

import java.util.List;


public class PlayerAuthority {

    public static boolean validateCollision(AreaEntity area, GridArray<DynamicTileRef> tileRefs, IRect2 viewRect,
            IMutLine2 mVector) {
        for (int i = 0; i < tileRefs.getFlatSize(); ++i) {
            DynamicTileRef tile = tileRefs.getFlat(i);
            if (tile == null || tile.getTileRef() == null) { continue; }
            if (!tile.getTileRef().hasCollision()) { continue; }

            List<QuadItem<IPolygon2>> colShape = area.queryCollisionGrid(viewRect);
            for (var quadItem : colShape) {
                if (quadItem.item().intersects(mVector) || quadItem.item().contains(mVector.end())) {
                    return false;
                }
            }
            return true;
        }

        // Calculate the direction vector from start to end
//                    System.out.println(mVector.start());
//                    System.out.println(mVector.end());
        //   IVector2 direction = Direction.getDirectionOf(mVector.start(), mVector.end()).asVec2();
//                    System.out.println(Direction.getDirectionOf(mVector.start(), mVector.end()));
//                    System.out.println(direction);
        //   mVector.setEnd(mVector.start().x() + (direction.x() * 16), mVector.start().y() + (direction.y() * 16));
        //mVector.setEnd(mVector.start());
////                    // Normalize the direction vector
////                    IVector2 normalizedDirection = direction.normalize();
////                    // Scale the normalized direction by -16 pixels to move in the opposite direction
////                    System.out.println(mVector.end());
////                    IVector2 adjustment = normalizedDirection.scale(32);
////                    System.out.println(adjustment);
////                    // Calculate the new position
////                    IVector2 newPosition = mVector.start().add(adjustment);
//                    mVector.setStart(mVector.end().add(direction));
//                    //   mVector.setEnd(newPosition);
//
//                    System.out.println(tileRefs.get(0, 0).getTileRef().index());
//                    IVector2 newCenter = tileRefs.get(0, 0).getTileRef().index().add(direction.x(), direction.y());
//                    System.out.println("newCenter : " + newCenter);
//                    ChunkData chunkData = area.getChunkByIndex(GridUtils.globalToChunk(mVector.end()));
//                    if (chunkData == null) {
//                        System.out.println("Null chunk tileData");
//                        continue;
//                    }
//
////                    int startX = (direction.x() > 0) ? newCenter.x() + 1 : newCenter.x() - 1;
////                    int endX = (direction.x() > 0) ? newCenter.x() - 1 : newCenter.x() + 1; // -3 and +3 due to the loop conditions
////                    int stepX = (direction.x() > 0) ? -1 : 1;
////
////                    int startY = (direction.y() > 0) ? newCenter.y() + 1 : newCenter.y() - 1;
////                    int endY = (direction.y() > 0) ? newCenter.y() - 1 : newCenter.y() + 2; // -3 and +3 due to the loop conditions
////                    int stepY = (direction.y() > 0) ? -1 : 1;
////
////                    for (int x = startX; (stepX > 0) ? x < endX : x > endX; x += stepX) {
////                        for (int y = startY; (stepY > 0) ? y < endY : y > endY; y += stepY) {
////                            TileData tileData = chunkData.getTileByIndex(IVector2.of(x, y));
////                            if (tileData == null || !tileData.hasCollision()) {
////                                // Process logic
////                                mVector.setEnd(GridUtils.tileToGlobal(chunkData.index(), IVector2.of(x, y)));
////                                return false;
////                            }
////                        }
////                    }
////                    return false;
//
//                    int startX = (direction.x() > 0) ? newCenter.x() + 1 : newCenter.x() - 1;
//                    int endX = (direction.x() > 0) ? newCenter.x() - 1 : newCenter.x() + 1; // -3 and +3 due to the loop conditions
//                    int stepX = (direction.x() > 0) ? -1 : 1;
//
//                    int startY = (direction.y() > 0) ? newCenter.y() + 1 : newCenter.y() - 1;
//                    int endY = (direction.y() > 0) ? newCenter.y() - 1 : newCenter.y() + 2; // -3 and +3 due to the loop conditions
//                    int stepY = (direction.y() > 0) ? -1 : 1;
//
//                    for (int x = newCenter.x(); x < newCenter.x() + 3; ++x) {
//                        for (int y = newCenter.y(); y < newCenter.y() + 3; ++y) {
//                            TileData tileData = chunkData.getTileByIndex(IVector2.of(x, y));
//                            if (tileData == null || !tileData.hasCollision()) {
//                                // Process logic
//                                mVector.setEnd(GridUtils.tileToGlobal(chunkData.index(), IVector2.of(x, y)));
//                                return false;
//                            }
//                        }
//                    }
//                    return false;
//
////                    for (int x = newCenter.x() - 2; x < newCenter.x() + 2; ++x) {
////                        for (int y = newCenter.y() - 2; y < newCenter.y() + 2; ++y) {
////                            TileData tileData = chunkData.getTileByIndex(IVector2.of(x, y));
////                            if (tileData == null || !tileData.hasCollision()) {
////                           //     System.out.println("reset");
////                                mVector.setEnd(GridUtils.tileToGlobal(chunkData.index(), IVector2.of(x, y)));
////s                                return false;
////                            }
////                        }
////                    }
//
////                //    if (quadItem.item().contains(mVector.end())) {
////                        int startX, startY, endX, endY, stepX, stepY;
////                    IVector2 d2 = Direction.getDirectionOf(mVector.end(), mVector.start()).asVec2();
////                    System.out.println(d2);
////                        if (d2.x() > 0) { // Right or diagonal right
////                            startX = 0;
////                            endX = 5;
////                            stepX = 1;
////                        } else if (d2.x() < 0) { // Left or diagonal left
////                            startX = 5 - 1;
////                            endX = -1;
////                            stepX = -1;
////                        } else {
////                            startX = 0;
////                            endX = 5;
////                            stepX = 1; // No horizontal movement
////                        }
////
////                        if (d2.y() > 0) { // Down or diagonal down
////                            startY = 0;
////                            endY = 5;
////                            stepY = 1;
////                        } else if (d2.y() < 0) { // Up or diagonal up
////                            startY = 5 - 1;
////                            endY = -1;
////                            stepY = -1;
////                        } else {
////                            startY = 0;
////                            endY = 5;
////                            stepY = 1; // No vertical movement
////                        }
////
////                        System.out.println("doing loop check");
////                        for (int x = startX; (stepX > 0) ? x < endX : x > endX; x += stepX) {
////                            for (int y = startY; (stepY > 0) ? y < endY : y > endY; y += stepY) {
////                                DynamicTileRef tileRef = tileRefs.get(x, y);
////                                if (tileRef.getChunkRef() == null) {
////                                    System.out.println("null chunk");
////                                    continue;
////                                }
////                                // null means no tile tileData (collision)
////                                System.out.println("tile null:" + tileRef.getTileRef() == null);
//////                                System.out.println("tile.has");
////                                if (tileRef.getTileRef() == null || !tileRef.getTileRef().hasCollision()) {
////                                    System.out.println("reset pos");
////                                    mVector.setEnd(GridUtils.tileToGlobal(tileRef.getChunkRef().index(), tileRef.getTileRef().index()));
////                                }
////                            }
////             //           }
////                    }
////
////                    System.out.println(mVector.end());
////                    return false;
//                }
//            }
//        }
        return true;
    }

    public static boolean validateDistance(IMutLine2 mVector, long lastTimestamp, long currentTimestamp) {
        double timeDiffSec = (currentTimestamp - lastTimestamp) / 1000.0; // Convert milliseconds to seconds
        double maxDist = WorldSettings.GET().maxSpeed() * timeDiffSec;
        double travelDist = mVector.distance();

        if (travelDist > maxDist) {
            int deltaX = mVector.end().x() - mVector.start().x();
            int deltaY = mVector.end().y() - mVector.start().y();
            double magnitude = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
            if (magnitude != 0) {
                // Normalize direction
                double normX = deltaX / magnitude;
                double normY = deltaY / magnitude;

                // Scale to max distance
                int moveX = (int) (normX * maxDist);
                int moveY = (int) (normY * maxDist);

                int adjX = mVector.start().x() + moveX;
                int adjY = mVector.start().y() + moveY;
                mVector.shiftLine(adjX, adjY);
                return false;
            }
        }
        return true;
    }


}
