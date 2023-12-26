package io.mindspce.outerfieldsserver.systems;

import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.enums.AreaId;
import io.mindspice.mindlib.data.geometry.IVector2;


public class EventData {

    public record AreaUpdate(boolean isPlayer, AreaId oldArea, AreaId newArea) { }


    public record ChunkUpdate(boolean isPlayer, IVector2 oldChunk, IVector2 newChunk) { }


    public record AreaEntered(boolean isPlayer, Entity enteredEntity) { }


    public record PositionUpdate(boolean isPlayer, IVector2 position) { }
}
