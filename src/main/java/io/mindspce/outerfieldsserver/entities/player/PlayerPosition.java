package io.mindspce.outerfieldsserver.entities.player;

import io.mindspce.outerfieldsserver.area.AreaInstance;
import io.mindspce.outerfieldsserver.area.ChunkData;
import io.mindspce.outerfieldsserver.area.TileData;
import io.mindspce.outerfieldsserver.components.Position;
import io.mindspce.outerfieldsserver.core.GameSettings;
import io.mindspce.outerfieldsserver.core.authority.PlayerAuthority;
import io.mindspce.outerfieldsserver.datacontainers.ActiveChunkUpdate;
import io.mindspce.outerfieldsserver.datacontainers.DynamicTileRef;
import io.mindspce.outerfieldsserver.enums.Direction;
import io.mindspce.outerfieldsserver.util.GridUtils;
import io.mindspice.mindlib.data.geometry.*;

import java.awt.geom.Line2D;


public class PlayerPosition extends Position {
    private AreaInstance currArea;
    private final ChunkData[][] localChunkGrid = new ChunkData[3][3];
    private final DynamicTileRef[][] localTileGrid = new DynamicTileRef[5][5];
    private final IMutRec2 updateBounds;
    private final IMutLine2 moveVector = ILine2.ofMutable(0, 0, 0, 0);
    private long lastTimeStamp = -1;

    public PlayerPosition(AreaInstance currArea, int startX, int startY) {
        this.currArea = currArea;
        IVector2 playerViewBuffer = GameSettings.GET().playerViewWithBuffer();
        updateBounds = IRect2.fromCenterMutable(0, 0, playerViewBuffer.x(), playerViewBuffer.y());
        setGlobalPos(startX, startY);
        setLocalPosFromGlobal(startX, startY);
        for (int x = 0; x < 5; ++x) {
            for (int y = 0; y < 5; ++y) {
                localTileGrid[x][y] = new DynamicTileRef(
                        currArea,
                        IVector2.of(GameSettings.GET().tileSize() * (x - 2),
                                GameSettings.GET().tileSize() * (y - 2)
                        )
                );
            }
        }
    }

    public void updatePlayerPos(int posX, int posY, long currTimeStamp) {
        moveVector.setStart(globalPos.x(), globalPos.y());
        moveVector.setEnd(posX, posY);

        if (lastTimeStamp != -1) {
            if (!PlayerAuthority.validateCollision(currArea, localTileGrid, moveVector)) {
                // TODO log abuse and force sync
                lastTimeStamp = currTimeStamp;
                return;
            }

            ILine2 checkedMVec = PlayerAuthority.validateDistance(moveVector, lastTimeStamp, currTimeStamp);
            lastTimeStamp = currTimeStamp; // Set this as soon as possible
            if (checkedMVec.end().x() != posX || checkedMVec.end().y() != posY) {
                //TODO log
                setGlobalPos(checkedMVec.end().x(), checkedMVec.end().y());
            } else {
                setGlobalPos(posX, posY);
            }
        }
        setLocalPosFromGlobal(globalPos.x(), globalPos.y());
        updateTileGrid(globalPos.x(), globalPos.y());

        if (getCurrTile() == null) { // player not in tilegrid
            setGlobalPos(moveVector.start().x(), moveVector.start().y());
        }

        if (GridUtils.isNewChunk(currChunkIndex, globalPos)) {
            setCurrChunkIndexFromGlobalPos(posX, posY);
            updateLocalGrid();
        }
        setCurrTileIndexFromLocalPos(localPos.x(), localPos.y());
    }

    private void updateTileGrid(int posX, int posY) {
        for (int x = 0; x < 5; ++x) {
            for (int y = 0; y < 5; ++y) {
                localTileGrid[x][y].updatePos(posX, posY);
            }
        }
    }

    public TileData getCurrTile() {
        return localTileGrid[3][3].getTileRef();
    }

    public ChunkData getCurrChunk() {
        return localChunkGrid[1][1];
    }

    public IRect2 getUpdateBounds() {
        updateBounds.reCenter(globalPos);
        return updateBounds;
    }

    public ChunkData[][] getLocalChunkGrid() {
        return localChunkGrid;
    }

    public AreaInstance getCurrArea() {
        return currArea;
    }

    public void setCurrArea(AreaInstance newArea) {
        this.currArea = newArea;
    }

    private void updateLocalGrid() {
        IVector2 lastIndex = localChunkGrid[1][1].getIndex();
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

        if (localChunkGrid[1][1] == null) {
            // TODO some type authoritative check, as this is an error or abude
        }
    }

    private void calcGridRemovals(ActiveChunkUpdate areaUpdate, Direction newX, Direction newY) {
        if (newX != null) {
            int x = newX == Direction.EAST ? 0 : 2;
            for (int y = 0; y < 3; ++y) {
                areaUpdate.removals.add(localChunkGrid[x][y].getIndex());
            }
        }

        if (newY != null) {
            int y = newY == Direction.SOUTH ? 0 : 2;
            for (int x = 0; x < 3; ++x) {
                areaUpdate.removals.add(localChunkGrid[x][y].getIndex());
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
                localChunkGrid[col][row] = currArea.getChunkByIndex(worldX, worldY);
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
                    localChunkGrid[col][row] = currArea.getChunkByIndex(worldX, worldY);
                    areaUpdate.additions.add(IVector2.of(worldX, worldY));
                }
            }
        }
    }

    private void shiftGridHorizontal(Direction direction) {
        if (direction == Direction.EAST) {
            // Shift each row to the left
            for (int y = 0; y < 3; y++) {
                localChunkGrid[0][y] = localChunkGrid[1][y];
                localChunkGrid[1][y] = localChunkGrid[2][y];
                localChunkGrid[2][y] = null; // Make room for the new column on the right
            }
        } else if (direction == Direction.WEST) {
            // Shift each row to the right
            for (int y = 0; y < 3; y++) {
                localChunkGrid[2][y] = localChunkGrid[1][y];
                localChunkGrid[1][y] = localChunkGrid[0][y];
                localChunkGrid[0][y] = null; // Make room for the new column on the left
            }
        }
    }

    private void shiftGridVertical(Direction direction) {
        if (direction == Direction.SOUTH) {
            // Shift each column down
            for (int x = 0; x < 3; x++) {
                localChunkGrid[x][0] = localChunkGrid[x][1];
                localChunkGrid[x][1] = localChunkGrid[x][2];
                localChunkGrid[x][2] = null; // Make room for the new row at the bottom
            }
        } else if (direction == Direction.NORTH) {
            // Shift each column up
            for (int x = 0; x < 3; x++) {
                localChunkGrid[x][2] = localChunkGrid[x][1];
                localChunkGrid[x][1] = localChunkGrid[x][0];
                localChunkGrid[x][0] = null; // Make room for the new row at the top
            }
        }
    }


}