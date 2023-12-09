package io.mindspce.outerfieldsserver.objects.player;

import io.mindspce.outerfieldsserver.area.AreaInstance;


public class PlayerCharacter {
    public final PlayerLocation playerLocation;

    public PlayerCharacter(AreaInstance currArea) {
        this.playerLocation = new PlayerLocation(currArea);
    }
}
