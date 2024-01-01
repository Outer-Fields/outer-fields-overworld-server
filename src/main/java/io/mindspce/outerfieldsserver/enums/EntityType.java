package io.mindspce.outerfieldsserver.enums;

import io.mindspce.outerfieldsserver.area.ChunkEntity;
import io.mindspce.outerfieldsserver.entities.item.ItemEntity;
import io.mindspce.outerfieldsserver.entities.locations.LocationEntity;
import io.mindspce.outerfieldsserver.entities.nonplayer.NonPlayerEntity;
import io.mindspce.outerfieldsserver.entities.player.PlayerEntity;


public enum EntityType {
    PLAYER((byte) 0, PlayerEntity.class),
    NON_PLAYER((byte) 1, NonPlayerEntity.class),
    AREA((byte) 2, ItemEntity.class),
    LOCATION((byte) 3, LocationEntity.class),
    CHUNK_ENTITY((byte) 4, ChunkEntity.class);

    public final byte value;
    public final Class<?> clazz;

    EntityType(byte value, Class<?> clazz) {
        this.value = value;
        this.clazz = clazz;
    }

}
