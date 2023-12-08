package io.mindspce.outerfieldsserver.player;

import io.mindspce.outerfieldsserver.core.state.configuration.GameSettings;
import io.mindspice.mindlib.data.geometry.IVector2;


public class PlayerWorldState {
    private final IVector2[][] localGrid = new IVector2[3][3];
    private IVector2 currChunkIndex;
    private IVector2 currChunkPos;
    private float globalPosX, globalPosY;
    private float localPosX, localPosY;


//    public void updatePlayerPos(int x, int y) {
//        setGlobalPos(x, y);
//
//        this.playerChunkPos = playerPos;
//        for (int i = 0; i < 3; ++i) {
//            for (int j = 0; j < 3; ++j) {
//                localGrid[i][j] = IVector2.of(playerPos.x() + i - 1, playerPos.y() + j - 1);
//            }
//        }
//    }

    private void setGlobalPos(int x, int y) {
        globalPosX = x;
        globalPosY = y;
    }

    private void setLocalPos(int x, int y) {
        localPosX = x % GameSettings.GET().chunkSize.x();
        localPosY = y % GameSettings.GET().chunkSize.y();
    }

    public IVector2[][] getLocalGrid() {
        return localGrid;
    }
}