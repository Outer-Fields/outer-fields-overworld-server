package io.mindspce.outerfieldsserver.entities.player;

import io.mindspce.outerfieldsserver.area.AreaInstance;
import io.mindspce.outerfieldsserver.area.TileData;
import io.mindspce.outerfieldsserver.components.Position;
import io.mindspce.outerfieldsserver.core.GameSettings;
import io.mindspce.outerfieldsserver.core.authority.PlayerAuthority;
import io.mindspce.outerfieldsserver.data.wrappers.DynamicTileRef;
import io.mindspce.outerfieldsserver.enums.EntityType;
import io.mindspce.outerfieldsserver.systems.event.EntityEvent;
import io.mindspce.outerfieldsserver.util.GridUtils;
import io.mindspice.mindlib.data.geometry.*;


public class PlayerPosition extends Position {
//    volatile AreaInstance currArea;
//    final PlayerState playerState; // Slight circular dependency to link chunk subscriptions
////    final IMutVector2[][] localChunkGrid = new IMutVector2[3][3];
//    final DynamicTileRef[][] localTileGrid = new DynamicTileRef[5][5];

//    long lastTimeStamp = -1;
//
//    public PlayerPosition(PlayerState playerState) {
//        this.playerState = playerState;
//        for (int x = 0; x < 5; ++x) {
//            for (int y = 0; y < 5; ++y) {
//                localTileGrid[x][y] = new DynamicTileRef(
//                        currArea,
//                        IVector2.of(
//                                GameSettings.GET().tileSize() * (x - 2),
//                                GameSettings.GET().tileSize() * (y - 2)
//                        )
//                );
//            }
//        }
//    }
//
//    public void init(AreaInstance currArea, int globalPosX, int globalPosY) {
//        this.currArea = currArea;
//        updatePlayerPos(globalPosX, globalPosY, System.currentTimeMillis());
//    }
//
//    //todo initialize chunk grid
//
//    public ClientUpdateRtn updatePlayerPos(int posX, int posY, long currTimeStamp) {
//        mVector.setStart(globalPos.x(), globalPos.y());
//        mVector.setEnd(posX, posY);
//
//        if (lastTimeStamp != -1) {
//            if (!PlayerAuthority.validateCollision(currArea, localTileGrid, mVector)) {
//                // TODO log abuse and force sync
//                lastTimeStamp = currTimeStamp;
//                return ClientUpdateRtn.NO_CHANGE;
//            }
//
//            ILine2 checkedMVec = PlayerAuthority.validateDistance(mVector, lastTimeStamp, currTimeStamp);
//            lastTimeStamp = currTimeStamp; // Set this as soon as possible
//            boolean invalidPos = false;
//            if (checkedMVec.end().x() != posX || checkedMVec.end().y() != posY) {
//                invalidPos = true;
//                setGlobalPos(checkedMVec.end().x(), checkedMVec.end().y());
//            } else {
//                setGlobalPos(posX, posY);
//            }
//        }
//
//        setLocalPosFromGlobal(globalPos.x(), globalPos.y());
//        updateTileGrid(globalPos.x(), globalPos.y());
//        setCurrTileIndexFromLocalPos(localPos.x(), localPos.y());
//
//        if (getCurrTile() == null) { // player not in tile grid
//            setGlobalPos(mVector.start().x(), mVector.start().y());
//            setLocalPosFromGlobal(globalPos.x(), globalPos.y());
//        }
//
//        if (GridUtils.isNewChunk(getCurrChunkIndex(), globalPos)) {
//            setCurrChunkIndexFromGlobalPos(posX, posY);
//            return ClientUpdateRtn.CHUNK_UPDATE;
//        }
//
//        boolean wasChange = !mVector.start().equals(globalPos);
//
//        if (wasChange) {
//            EntityEvent entityEvent = new EntityEvent(
//                    currArea.getId(),
//                    playerState.getPositionData().getCurrChunk(),
//                    EntityType.PLAYER,
//                    playerState.getId()
//            );
//            currArea.getEventManager().pushEntityEvent(entityEvent);
//        }
//        return mVector.start().equals(globalPos) ? ClientUpdateRtn.NO_CHANGE : ClientUpdateRtn.CHANGE;
//    }
//
//    void updateTileGrid(int posX, int posY) {
//        for (int x = 0; x < 5; ++x) {
//            for (int y = 0; y < 5; ++y) {
//                localTileGrid[x][y].updatePos(posX, posY);
//            }
//        }
//    }

//    public TileData getCurrTile() {
//        return localTileGrid[3][3].getTileRef();
//    }
//
////    public IVector2 getCurrChunk() {
////        return localChunkGrid[1][1];
////    }
////
////    public IVector2[][] getLocalChunkGrid() {
////        return localChunkGrid;
////    }
//
//    public AreaInstance getCurrArea() {
//        return currArea;
//    }
//
//    public void setCurrArea(AreaInstance newArea) {
//        this.currArea = newArea;
//    }

//    private ActiveChunkUpdate updateChunkGrid() {
//        IVector2 lastIndex = localChunkGrid[1][1];
//        Direction newX = null;
//        Direction newY = null;
//
//        if (currChunkIndex.x() > lastIndex.x()) {
//            newX = Direction.EAST;
//        } else if (currChunkIndex.x() < lastIndex.x()) {
//            newX = Direction.WEST;
//        }
//        if (currChunkIndex.y() > lastIndex.y()) {
//            newY = Direction.SOUTH;
//        } else if (currChunkIndex.y() < lastIndex.y()) {
//            newY = Direction.NORTH;
//        }
//
//        ActiveChunkUpdate activeChunkUpdate = new ActiveChunkUpdate();
//        calcGridRemovals(activeChunkUpdate, newX, newY);
//        calcGridAdditions(activeChunkUpdate, newX, newY);
//
//        if (localChunkGrid[1][1] == null) {
//            // TODO some type authoritative check, as this is an error or abude
//        }
//        return activeChunkUpdate;
//
////        int centerX = currChunkIndex.x() - 1;
////        int centerY = currChunkIndex.y() - 1;
////        for (int i = 0; i < localGrid.length; i++) {
////            for (int j = 0; j < localGrid[i].length; j++) {
////                int worldX = centerX + (i - 1);
////                int worldY = centerY + (j - 1);
////                localGrid[i][j] = currArea.getChunkByIndex(worldX, worldY);
////            }
////        }
//
//    }
//
//    private void calcGridRemovals(ActiveChunkUpdate areaUpdate, Direction newX, Direction newY) {
//        if (newX != null) {
//            int x = newX == Direction.EAST ? 0 : 2;
//            for (int y = 0; y < 3; ++y) {
//                areaUpdate.removals.add(IVector2.of(localChunkGrid[x][y]));
//            }
//        }
//
//        if (newY != null) {
//            int y = newY == Direction.SOUTH ? 0 : 2;
//            for (int x = 0; x < 3; ++x) {
//                areaUpdate.removals.add(IVector2.of(localChunkGrid[x][y]));
//            }
//        }
//    }
//
//    private void calcGridAdditions(ActiveChunkUpdate areaUpdate, Direction newX, Direction newY) {
//        int centerX = currChunkIndex.x() - 1;
//        int centerY = currChunkIndex.y() - 1;
//        if (newX != null) {
//            shiftGridHorizontal(newX);
//            int col = newX == Direction.EAST ? 2 : 0;
//            for (int row = 0; row < 3; ++row) {
//                int worldX = centerX + (col - 1);
//                int worldY = centerY + (row - 1);
//                localChunkGrid[col][row].setXY(worldX, worldY);
//                areaUpdate.additions.add(IVector2.of(worldX, worldY));
//            }
//        }
//
//        if (newY != null) {
//            shiftGridVertical(newY);
//            int row = newY == Direction.SOUTH ? 2 : 0;
//            for (int col = 0; col < 3; ++col) {
//                if (newX == null || col != (newX == Direction.EAST ? 2 : 0)) {
//                    int worldX = centerX + (col - 1);
//                    int worldY = centerY + (row - 1);
//                    localChunkGrid[col][row].setXY(worldX, worldY);
//                    areaUpdate.additions.add(IVector2.of(worldX, worldY));
//                }
//            }
//        }
//    }
//
//    private void shiftGridHorizontal(Direction direction) {
//        if (direction == Direction.EAST) {
//            // Shift each row to the left
//            for (int y = 0; y < 3; y++) {
//                localChunkGrid[0][y] = localChunkGrid[1][y];
//                localChunkGrid[1][y] = localChunkGrid[2][y];
//                localChunkGrid[2][y] = null; // Make room for the new column on the right
//            }
//        } else if (direction == Direction.WEST) {
//            // Shift each row to the right
//            for (int y = 0; y < 3; y++) {
//                localChunkGrid[2][y] = localChunkGrid[1][y];
//                localChunkGrid[1][y] = localChunkGrid[0][y];
//                localChunkGrid[0][y] = null; // Make room for the new column on the left
//            }
//        }
//    }
//
//    private void shiftGridVertical(Direction direction) {
//        if (direction == Direction.SOUTH) {
//            // Shift each column down
//            for (int x = 0; x < 3; x++) {
//                localChunkGrid[x][0] = localChunkGrid[x][1];
//                localChunkGrid[x][1] = localChunkGrid[x][2];
//                localChunkGrid[x][2] = null; // Make room for the new row at the bottom
//            }
//        } else if (direction == Direction.NORTH) {
//            // Shift each column up
//            for (int x = 0; x < 3; x++) {
//                localChunkGrid[x][2] = localChunkGrid[x][1];
//                localChunkGrid[x][1] = localChunkGrid[x][0];
//                localChunkGrid[x][0] = null; // Make room for the new row at the top
//            }
//        }
//    }
//

}