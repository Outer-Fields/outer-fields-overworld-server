package io.mindspce.outerfieldsserver.core;

import io.mindspice.mindlib.data.geometry.IVector2;


public class GameSettings {
    private static GameSettings INSTANCE = new GameSettings();
    private IVector2 playerViewVec = IVector2.of(960, 540);
    private IVector2 playerViewBuffer = IVector2.of(384, 384);
    private IVector2 playerViewWithBuffer = playerViewVec.add(playerViewBuffer);
    private IVector2 chunkSize = IVector2.of(1920, 1920);
    private IVector2 tilesPerChunk = IVector2.of(60, 60);
    private IVector2 worldSize = IVector2.of(512, 512);
    private int tileSize = 32;
    private int maxSpeed = 1000;
    private int tickRate = 20;
    private int npcTickInterval = 2;
    private int itemTickInterval = 30;
    private int locationTickInterval = 90;

    public static GameSettings GET() {
        return INSTANCE;
    }

    public IVector2 playerViewVec() {
        return playerViewVec;
    }

    public IVector2 playerViewBuffer() {
        return playerViewBuffer;
    }

    public IVector2 playerViewWithBuffer() {
        return playerViewWithBuffer;
    }

    public IVector2 chunkSize() {
        return chunkSize;
    }

    public IVector2 tilesPerChunk() {
        return tilesPerChunk;
    }

    public int tileSize() {
        return tileSize;
    }

    public IVector2 worldSize() {
        return worldSize;
    }

    public int maxSpeed() {
        return maxSpeed;
    }

    public int tickRate() {
        return tickRate;
    }

    public int npcTickInterval() {
        return npcTickInterval;
    }

    public int itemTickInterval() {
        return itemTickInterval;
    }

    public int locationTickInterval() {
        return locationTickInterval;
    }
}
