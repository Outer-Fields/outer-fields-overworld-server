package io.mindspice.outerfieldsserver.core.authority;

import io.mindspice.outerfieldsserver.entities.AreaEntity;
import io.mindspice.outerfieldsserver.core.WorldSettings;
import io.mindspice.outerfieldsserver.data.wrappers.DynamicTileRef;
import io.mindspice.mindlib.data.collections.other.GridArray;
import io.mindspice.mindlib.data.geometry.*;
import io.mindspice.outerfieldsserver.enums.AreaId;

import java.util.List;


public class PlayerAuthority {

    public static boolean validateCollision(AreaEntity area, GridArray<DynamicTileRef> tileRefs, IRect2 viewRect,
            IMutLine2 mVector) {
        for (int i = 0; i < tileRefs.getFlatSize(); ++i) {
            DynamicTileRef tile = tileRefs.getFlat(i);
            if (tile == null || tile.getTileRef() == null) { continue; }
            if (!tile.getTileRef().hasCollision()) { continue; }

            List<QuadItem<IPolygon2>> colShape = area.queryCollisionGrid(viewRect);
            for (int j = 0; j < colShape.size(); ++j) {
                if (colShape.get(j).item().intersects(mVector) || colShape.get(j).item().contains(mVector.end())) {
                    return false;
                }
            }
            return true;
        }
        return true;
    }

    public static boolean validateSpawn(AreaId areaId, IRect2 spawnRect, IVector2 pos) {
        List<QuadItem<IPolygon2>> colShape = areaId.areaEntity.queryCollisionGrid(spawnRect);
        for (int i = 0; i < colShape.size(); ++i) {
            if (colShape.get(i).item().contains(pos)) {
                return false;
            }
        }
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
