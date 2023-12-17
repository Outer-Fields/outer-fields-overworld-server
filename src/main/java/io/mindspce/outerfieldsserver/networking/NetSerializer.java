package io.mindspce.outerfieldsserver.networking;

import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.entities.item.ItemEntity;
import io.mindspce.outerfieldsserver.entities.locations.LocationEntity;
import io.mindspce.outerfieldsserver.entities.nonplayer.NonPlayerEntity;
import io.mindspce.outerfieldsserver.entities.player.PlayerEntity;
import io.mindspce.outerfieldsserver.enums.EntityType;
import io.mindspce.outerfieldsserver.enums.State;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;


public class NetSerializer {
    public static final int ENTITY_UPDATE_BYTES = 15;
    public static final int NEW_CHARACTER_BYTES = 53;
    public static final int NEW_ITEM_BYTES = 51;
    public static final int NEW_LOCATION_BYTES = 51;


    /* Entity Update
     *-------------------
     *  byte DataType 1
     *  byte entityType 1
     *  int entityId 4
     *  byte stateCount 1
     *  byte entityState n
     *  int posX 4
     *  int posY 4
     * ------------------
     * 15 bytes
     */

    /* Entity New Player/NonPlayer
     *--------------------
     *  byte DataType 1
     *  byte entityType 1
     *  int entityId 4
     *  byte stateCount 1
     *  byte entityState n
     *  String name 32
     *  byte[] outfit  6
     *  int entityId 4
     *  int posX 4
     *  int posY 4
     * ------------------
     * 37 bytes
     */


    /* Entity New Item
     *--------------------
     * byte DataType 1
     * byte EntityType 1
     * int entityId 4
     * string name 32
     * int ItemKey 4
     * byte stateCount 1
     * byte entityState n
     * int posX 4
     * int posy 4
     * ----------------
     * 51 bytes
     */


    /* Entity New Location
     * byte DataType 1
     * byte EntityType 1
     * int entityId 4
     *  int LocationKey 4
     * byte stateCount 1
     * byte entityState n
     * string name 32
     * int posX 4
     * int posy 4
     *  51 bytes
     */

    public static ByteBuffer entityUpdateToBuffer(EntityType entityType, Entity entity) {
        return entityUpdateToBuffer(
                ByteBuffer.allocate(ENTITY_UPDATE_BYTES + entity.states().length), entityType, entity
        );
    }

    public static ByteBuffer entityUpdateToBuffer(ByteBuffer buffer, EntityType entityType, Entity entity) {
        buffer.put(DataType.ENTITY_UPDATE.value);
        buffer.put(entityType.value);
        buffer.putInt(entity.id());
        buffer.put((byte) entity.states().length);
        for (State state : entity.states()) { buffer.put(state.value); }
        buffer.putInt(entity.globalPosition().x());
        buffer.putInt(entity.globalPosition().y());
        return buffer;
    }

    public static ByteBuffer newItemToBuffer(ItemEntity entity) {
        return newItemToBuffer(ByteBuffer.allocate(NEW_ITEM_BYTES + entity.states().length), entity);
    }

    public static ByteBuffer newItemToBuffer(ByteBuffer buffer, ItemEntity entity) {
        buffer.put(DataType.NEW_ITEM.value);
        buffer.put(EntityType.ITEM.value);
        buffer.putInt(entity.id());
        buffer.put(padName(entity.name()));
        buffer.putInt(entity.key());
        buffer.put((byte) entity.states().length);
        for (State state : entity.states()) { buffer.put(state.value); }
        buffer.putInt(entity.globalPosition().x());
        buffer.putInt(entity.globalPosition().y());
        return buffer;
    }

    public static ByteBuffer newLocationToBuffer(LocationEntity entity) {
        return newLocationToBuffer(ByteBuffer.allocate(NEW_ITEM_BYTES + entity.states().length), entity);
    }

    public static ByteBuffer newLocationToBuffer(ByteBuffer buffer, LocationEntity entity) {
        buffer.put(DataType.NEW_LOCATION.value);
        buffer.put(EntityType.ITEM.value);
        buffer.putInt(entity.id());
        buffer.put(padName(entity.name()));
        buffer.putInt(entity.key());
        buffer.put((byte) entity.states().length);
        for (State state : entity.states()) { buffer.put(state.value); }
        buffer.putInt(entity.globalPosition().x());
        buffer.putInt(entity.globalPosition().y());
        return buffer;
    }

    public static ByteBuffer newPlayerToBuffer(PlayerEntity entity) {
        return newPlayerToBuffer(
                ByteBuffer.allocate(NEW_CHARACTER_BYTES + entity.states().length), entity);
    }

    public static ByteBuffer newPlayerToBuffer(ByteBuffer buffer, PlayerEntity entity) {
        buffer.put(DataType.NEW_CHARACTER.value);
        buffer.put(EntityType.PLAYER.value);
        buffer.putInt(entity.id());
        buffer.put((byte) entity.states().length);
        for (State state : entity.states()) { buffer.put(state.value); }
        buffer.put(padName(entity.name()));
        buffer.put(entity.outfit().asByteArray());
        buffer.putInt(entity.globalPosition().x());
        buffer.putInt(entity.globalPosition().y());
        return buffer;
    }

    public static ByteBuffer newNonPlayerToBuffer(NonPlayerEntity entity) {
        return newNonPlayerToBuffer(
                ByteBuffer.allocate(NEW_CHARACTER_BYTES + entity.states().length), entity);
    }

    public static ByteBuffer newNonPlayerToBuffer(ByteBuffer buffer, NonPlayerEntity entity) {
        buffer.put(DataType.NEW_CHARACTER.value);
        buffer.put(EntityType.NON_PLAYER.value);
        buffer.putInt(entity.id());
        buffer.put((byte) entity.states().length);
        for (State state : entity.states()) { buffer.put(state.value); }
        buffer.put(padName(entity.name()));
        buffer.put(entity.outfit().asByteArray());
        buffer.putInt(entity.globalPosition().x());
        buffer.putInt(entity.globalPosition().y());
        return buffer;
    }

    private static byte[] padName(String name) {
        if (name.length() > 16) { name = name.substring(0, 16); }
        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_16);
        if (nameBytes.length < 32) {
            nameBytes = Arrays.copyOf(nameBytes, 32);
        }
        return nameBytes;
    }


}
