package io.mindspce.outerfieldsserver.components;

import io.mindspce.outerfieldsserver.core.GameSettings;
import io.mindspice.mindlib.data.geometry.IMutVector2;
import io.mindspice.mindlib.data.geometry.IVector2;


public class Position {
    protected IMutVector2 currChunkIndex = IVector2.ofMutable(0, 0);
    protected IMutVector2 currTileIndex = IVector2.ofMutable(0, 0);
    protected IMutVector2 globalPos = IVector2.ofMutable(0, 0);
    protected IMutVector2 localPos = IVector2.ofMutable(0, 0);

    public IVector2 getCurrChunkIndex() {
        return currChunkIndex;
    }

    public IVector2 getCurrTileIndex() {
        return currTileIndex;
    }

    public IVector2 getGlobalPos() {
        return globalPos;
    }

    public IVector2 getLocalPos() {
        return localPos;
    }

    public IVector2 getGlobalTileIndex() {
        return IVector2.of(
                currChunkIndex.x() * GameSettings.GET().chunkSize().x() + currTileIndex.x(),
                currChunkIndex.y() * GameSettings.GET().chunkSize().y() + currTileIndex.y());
    }

    public void setCurrChunkIndex(IVector2 newChunkIndex) {
        currChunkIndex.setXY(newChunkIndex.x(), newChunkIndex.y());
    }

    public void setCurrChunkIndex(int x, int y) {
        currChunkIndex.setXY(x, y);
    }

    public void setCurrChunkIndexFromGlobalPos(int x, int y) {
        currChunkIndex.setXY(
                x / GameSettings.GET().chunkSize().x(),
                y / GameSettings.GET().chunkSize().y()
        );
    }

    public void setCurrChunkIndexFromGlobalPos(IVector2 newPos) {
        currChunkIndex.setXY(
                newPos.x() / GameSettings.GET().chunkSize().x(),
                newPos.y() / GameSettings.GET().chunkSize().y()
        );
    }

    public void setCurrTileIndex(IVector2 newTileIndex) {
        currTileIndex.setXY(newTileIndex.x(), newTileIndex.y());
    }

    public void setCurrTileIndex(int x, int y) {
        currTileIndex.setXY(x, y);
    }

    public void setCurrTileIndexFromGlobalPos(int x, int y) {
        currChunkIndex.setXY(
                x / GameSettings.GET().chunkSize().x(),
                y / GameSettings.GET().chunkSize().y()
        );
    }

    public void setCurrTileIndexFromGlobalPos(IVector2 newPos) {
        currTileIndex.setXY(
                newPos.x() % GameSettings.GET().chunkSize().x(),
                newPos.y() % GameSettings.GET().chunkSize().y()
        );
    }

    public void setCurrTileIndexFromLocalPos(int x, int y) {
        currTileIndex.setXY(
                x / GameSettings.GET().tileSize(),
                y / GameSettings.GET().tileSize());
    }

    public void setCurrTileIndexFromLocalPos(IVector2 newPos) {
        currTileIndex.setXY(
                newPos.x() / GameSettings.GET().tileSize(),
                newPos.y() / GameSettings.GET().tileSize());
    }

    public void setGlobalPos(IVector2 newPos) {
        globalPos.setXY(newPos.x(), newPos.y());
    }

    public void setGlobalPos(int x, int y) {
        globalPos.setXY(x, y);
    }

    public void setLocalPos(IVector2 newPos) {
        localPos.setXY(newPos.x(), newPos.y());
    }

    public void setLocalPos(int x, int y) {
        localPos.setXY(x, y);
    }

    public void setLocalPosFromGlobal(int x, int y) {
        localPos.setX(x % GameSettings.GET().chunkSize().x());
        localPos.setY(y % GameSettings.GET().chunkSize().y());
    }

    public void setLocalPosFromGlobal(IVector2 newPos) {
        localPos.setX(newPos.x() % GameSettings.GET().chunkSize().x());
        localPos.setY(newPos.y() % GameSettings.GET().chunkSize().y());
    }
}



