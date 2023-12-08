package io.mindspce.outerfieldsserver.core.state.configuration;

import io.mindspice.mindlib.data.geometry.IVector2;


public class GameSettings {
    private static GameSettings INSTANCE = new GameSettings();
    public IVector2 playerViewVec = IVector2.of(960, 540);
    public IVector2 playerViewBuffer = IVector2.of(256, 256);
    public IVector2 playerViewWithBuffer = playerViewVec.add(playerViewBuffer);
    public IVector2 chunkSize = IVector2.of(3840, 3840);

    public static GameSettings GET() {
        return INSTANCE;
    }

    public IVector2 chunkSize() {
        return chunkSize;
    }
}
