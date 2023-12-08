package io.mindspce.outerfieldsserver.objects.player;

import io.mindspce.outerfieldsserver.core.configuration.GameSettings;
import io.mindspice.mindlib.data.geometry.IMutVector2;
import io.mindspice.mindlib.data.geometry.IVector2;


public class PlayerState2 {

    private final IMutVector2[][] localGrid = new IMutVector2[3][3];
    private IMutVector2 currChunkIndex;
    private IMutVector2 currChunkPos;
    private IMutVector2 globalPos = IVector2.ofMutable(0, 0);
    private IMutVector2 localPos = IVector2.ofMutable(0, 0);

    public PlayerState2() {
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                localGrid[i][j] = IVector2.ofMutable(i - 1, j - 1);
            }
        }
    }

    public synchronized void updatePlayerPos(int x, int y) {
        setGlobalPos(x, y);
        setLocalPos(x, y);

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                var gridLoc = localGrid[i][j];
                gridLoc.setXY(x + i - 1, y + j - 1);
            }
        }
    }

    private void setGlobalPos(int x, int y) {
        globalPos.setXY(x, y);
    }

    private void setLocalPos(int x, int y) {
        localPos.setX(x % GameSettings.GET().chunkSize().x());
        localPos.setY(y % GameSettings.GET().chunkSize().y());
    }

    public synchronized IVector2[][] getLocalGrid() {
        return localGrid;
    }
}