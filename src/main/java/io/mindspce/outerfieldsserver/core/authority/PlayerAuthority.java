package io.mindspce.outerfieldsserver.core.authority;

import io.mindspce.outerfieldsserver.area.AreaInstance;
import io.mindspce.outerfieldsserver.area.TileData;
import io.mindspce.outerfieldsserver.core.GameSettings;
import io.mindspce.outerfieldsserver.datacontainers.DynamicTileRef;
import io.mindspice.mindlib.data.geometry.ILine2;
import io.mindspice.mindlib.data.geometry.IMutLine2;
import io.mindspice.mindlib.data.geometry.IPolygon2;


public class PlayerAuthority {

    public static boolean validateCollision(AreaInstance area, DynamicTileRef[][] tileRefs, ILine2 mVector) {
        for (int x = 0; x < tileRefs.length; ++x) {
            for (int y = 0; y < tileRefs.length; ++y) {
                TileData tile = tileRefs[x][y].getTileRef();
                if (tile == null) { continue; }
                if (!tile.hasCollision()) { continue; }
                IPolygon2 colShape = tileRefs[x][y].getChunkRef().getCollision(tile.collisionId());
                if (colShape != null && colShape.intersects(mVector)) { return false; }
            }
        }
        return true;
    }

    public static IMutLine2 validateDistance(IMutLine2 mVector, long lastTimestamp, long currentTimestamp) {
        double timeDiffSec = (currentTimestamp - lastTimestamp) / 1000.0; // Convert milliseconds to seconds
        double maxDist = GameSettings.GET().maxSpeed() * timeDiffSec;
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
                mVector.setEnd(adjX, adjY);
            }
        }
        return mVector;
    }
}
