package io.mindspce.outerfieldsserver.core.authority;

import io.mindspce.outerfieldsserver.area.AreaInstance;
import io.mindspce.outerfieldsserver.area.TileData;
import io.mindspce.outerfieldsserver.core.GameSettings;
import io.mindspce.outerfieldsserver.data.wrappers.DynamicTileRef;
import io.mindspce.outerfieldsserver.entities.player.PlayerState;
import io.mindspice.mindlib.data.collections.other.GridArray;
import io.mindspice.mindlib.data.geometry.IAtomicLine2;
import io.mindspice.mindlib.data.geometry.ILine2;
import io.mindspice.mindlib.data.geometry.IMutLine2;
import io.mindspice.mindlib.data.geometry.IPolygon2;

import java.util.HashSet;
import java.util.function.Consumer;


public class PlayerAuthority {

    public static boolean validateCollision(AreaInstance area, GridArray<DynamicTileRef> tileRefs, IAtomicLine2 mVector) {
        HashSet<Integer> checked = new HashSet<>(9);
        for (int i = 0; i < tileRefs.getFlatSize(); ++i) {
            DynamicTileRef tile = tileRefs.getFlat(i);
            if (tile == null || tile.getTileRef() == null) { continue; }
            if (!tile.getTileRef().hasCollision()) { continue; }
            int colId = tile.getTileRef().collisionId();
            if (checked.contains(colId)) { continue; }
            checked.add(colId);
            IPolygon2 colShape = tile.getChunkRef().getCollision(colId);
            if (colShape != null && colShape.intersects(mVector)) {
                mVector.setEnd(mVector.start());
                return false;
            }
        }
        return true;
    }

    public static boolean validateDistance(IAtomicLine2 mVector, long lastTimestamp, long currentTimestamp) {
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
                return false;
            }
        }
        return true;
    }


}
