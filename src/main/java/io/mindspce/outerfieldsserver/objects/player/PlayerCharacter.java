package io.mindspce.outerfieldsserver.objects.player;

import io.mindspce.outerfieldsserver.area.AreaInstance;
import io.mindspce.outerfieldsserver.area.ChunkData;
import io.mindspce.outerfieldsserver.util.GridUtils;
import io.mindspice.mindlib.data.geometry.IVector2;

import java.util.HashSet;


public class PlayerCharacter {
    public final PlayerLocation location;
    public final HashSet<IVector2> updateVectors = new HashSet<>();

    public PlayerCharacter(AreaInstance currArea) {
        this.location = new PlayerLocation(currArea);
    }

    public void sendWorldUpdate() {
        var loc = location.getUpdateArea();




    }
}
