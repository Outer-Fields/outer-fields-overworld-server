package io.mindspce.outerfieldsserver.objects.player;

import io.mindspce.outerfieldsserver.area.AreaInstance;
import io.mindspce.outerfieldsserver.area.ChunkData;
import io.mindspce.outerfieldsserver.core.GameSettings;
import io.mindspce.outerfieldsserver.datacontainers.ActiveChunkUpdate;
import io.mindspce.outerfieldsserver.enums.Direction;
import io.mindspce.outerfieldsserver.util.GridUtils;
import io.mindspice.mindlib.data.geometry.IMutRec2;
import io.mindspice.mindlib.data.geometry.IMutVector2;
import io.mindspice.mindlib.data.geometry.IRect2;
import io.mindspice.mindlib.data.geometry.IVector2;


public class PlayerLocation {
    private AreaInstance currArea;
    private final ChunkData[][] localGrid = new ChunkData[3][3];
    private final IMutVector2 currChunkIndex = IVector2.ofMutable(0, 0);
    private final IMutVector2 currTileIndex = IVector2.ofMutable(0, 0);
    private final IMutVector2 globalPos = IVector2.ofMutable(0, 0);
    private final IMutVector2 localPos = IVector2.ofMutable(0, 0);
    private final IMutRec2 updateArea;

    public PlayerLocation(AreaInstance currArea) {
        this.currArea = currArea;
        IVector2 playerViewBuffer = GameSettings.GET().playerViewWithBuffer();
        updateArea = IRect2.fromCenterMutable(0, 0, playerViewBuffer.x(), playerViewBuffer.y());
    }

    public void updatePlayerPos(int x, int y) {
        setGlobalPos(x, y);
        setLocalPos(x, y);
        if (GridUtils.isNewChunk(currChunkIndex, globalPos)) {
            setCurrChunkIndex();
            updateLocalGrid();
        }
        setCurrTileIndex();
    }

    public IRect2 getUpdateArea() {
        updateArea.reCenter(globalPos);
        IVector2 chunkSize = GameSettings.GET().chunkSize();
        int leftChunkX = updateArea.start().x() / chunkSize.x();
        int leftChunkTopY = (updateArea.start().y() / chunkSize.y();
        int leftChunkBottomY = (updateArea.start().y() + updateArea.size().y()) / chunkSize.y();

        int rightChunkX = updateArea.end().x() / chunkSize.x();
        int rightChunkTopX = updateArea.end().y() / chunkSize.y();
        int rightChunkBottomX = (updateArea.end().y() + updateArea.size().y()) / chunkSize.y();

        if (leftChunkX != currChunkIndex.x()) {

        }
    }

    private void setGlobalPos(int x, int y) {
        globalPos.setXY(x, y);
    }

    private void setLocalPos(int x, int y) {
        localPos.setX(x % GameSettings.GET().chunkSize().x());
        localPos.setY(y % GameSettings.GET().chunkSize().y());
    }

    private void setCurrChunkIndex() {
        currChunkIndex.setXY(
                globalPos.x() / GameSettings.GET().chunkSize().x(),
                globalPos.y() / GameSettings.GET().chunkSize().y()
        );
    }

    private void setCurrTileIndex() {
        currTileIndex.setXY(
                localPos.x() / GameSettings.GET().tileSize(),
                localPos.y() / GameSettings.GET().tileSize()
        );
    }

    private void updateLocalGrid() {

        IVector2 lastIndex = localGrid[1][1].getIndex();
        Direction newX = null;
        Direction newY = null;

        if (currChunkIndex.x() > lastIndex.x()) {
            newX = Direction.EAST;
        } else if (currChunkIndex.x() < lastIndex.x()) {
            newX = Direction.WEST;
        }
        if (currChunkIndex.y() > lastIndex.y()) {
            newY = Direction.SOUTH;
        } else if (currChunkIndex.y() < lastIndex.y()) {
            newY = Direction.NORTH;
        }

        ActiveChunkUpdate activeChunkUpdate = new ActiveChunkUpdate();
        calcGridRemovals(activeChunkUpdate, newX, newY);
        calcGridAdditions(activeChunkUpdate, newX, newY);
        currArea.queueActiveChunk(activeChunkUpdate);

//        int centerX = currChunkIndex.x() - 1;
//        int centerY = currChunkIndex.y() - 1;
//        for (int i = 0; i < localGrid.length; i++) {
//            for (int j = 0; j < localGrid[i].length; j++) {
//                int worldX = centerX + (i - 1);
//                int worldY = centerY + (j - 1);
//                localGrid[i][j] = currArea.getChunkByIndex(worldX, worldY);
//            }
//        }

        if (localGrid[1][1] == null) {
            // TODO some type authoritative check, as this is an error or abude
        }
    }

    private void calcGridRemovals(ActiveChunkUpdate areaUpdate, Direction newX, Direction newY) {
        if (newX != null) {
            int x = newX == Direction.EAST ? 0 : 2;
            for (int y = 0; y < 3; ++y) {
                areaUpdate.removals.add(localGrid[x][y].getIndex());
            }
        }

        if (newY != null) {
            int y = newY == Direction.SOUTH ? 0 : 2;
            for (int x = 0; x < 3; ++x) {
                areaUpdate.removals.add(localGrid[x][y].getIndex());
            }
        }
    }

    private void calcGridAdditions(ActiveChunkUpdate areaUpdate, Direction newX, Direction newY) {
        int centerX = currChunkIndex.x() - 1;
        int centerY = currChunkIndex.y() - 1;
        if (newX != null) {
            shiftGridHorizontal(newX);
            int col = newX == Direction.EAST ? 2 : 0;
            for (int row = 0; row < 3; ++row) {
                int worldX = centerX + (col - 1);
                int worldY = centerY + (row - 1);
                localGrid[col][row] = currArea.getChunkByIndex(worldX, worldY);
                areaUpdate.additions.add(IVector2.of(worldX, worldY));
            }
        }

        if (newY != null) {
            shiftGridVertical(newY);
            int row = newY == Direction.SOUTH ? 2 : 0;
            for (int col = 0; col < 3; ++col) {
                if (newX == null || col != (newX == Direction.EAST ? 2 : 0)) {
                    int worldX = centerX + (col - 1);
                    int worldY = centerY + (row - 1);
                    localGrid[col][row] = currArea.getChunkByIndex(worldX, worldY);
                    areaUpdate.additions.add(IVector2.of(worldX, worldY));
                }
            }
        }
    }

    private void shiftGridHorizontal(Direction direction) {
        if (direction == Direction.EAST) {
            // Shift each row to the left
            for (int y = 0; y < 3; y++) {
                localGrid[0][y] = localGrid[1][y];
                localGrid[1][y] = localGrid[2][y];
                localGrid[2][y] = null; // Make room for the new column on the right
            }
        } else if (direction == Direction.WEST) {
            // Shift each row to the right
            for (int y = 0; y < 3; y++) {
                localGrid[2][y] = localGrid[1][y];
                localGrid[1][y] = localGrid[0][y];
                localGrid[0][y] = null; // Make room for the new column on the left
            }
        }
    }

    private void shiftGridVertical(Direction direction) {
        if (direction == Direction.SOUTH) {
            // Shift each column down
            for (int x = 0; x < 3; x++) {
                localGrid[x][0] = localGrid[x][1];
                localGrid[x][1] = localGrid[x][2];
                localGrid[x][2] = null; // Make room for the new row at the bottom
            }
        } else if (direction == Direction.NORTH) {
            // Shift each column up
            for (int x = 0; x < 3; x++) {
                localGrid[x][2] = localGrid[x][1];
                localGrid[x][1] = localGrid[x][0];
                localGrid[x][0] = null; // Make room for the new row at the top
            }
        }
    }


}