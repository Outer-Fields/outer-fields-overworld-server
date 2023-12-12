package io.mindspce.outerfieldsserver.entities.player;

import io.mindspce.outerfieldsserver.area.AreaInstance;
import io.mindspice.mindlib.data.geometry.IVector2;


public class PlayerCharacter extends PlayerEntity {
    private PlayerSession session;
    private final PlayerPosition positionData;

    public PlayerCharacter(AreaInstance currArea, int startX, int startY) {
        this.positionData = new PlayerPosition(currArea, startX, startY);

    }

    public IVector2 getLocalPos() {
        return positionData.getLocalPos();
    }

    public PlayerSession getSession() {
        return session;
    }

    public PlayerPosition getPositionData() {
        return positionData;
    }

    @Override
    public IVector2 getGlobalPos() {
        return positionData.getGlobalPos();
    }

    public void getViewUpdate() {

    }

}
