package io.mindspce.outerfieldsserver.entities.player;

import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.entities.item.ItemEntity;
import io.mindspce.outerfieldsserver.entities.locations.LocationEntity;
import io.mindspce.outerfieldsserver.entities.nonplayer.NonPlayerEntity;
import io.mindspce.outerfieldsserver.networking.NetMsgOut;
import io.mindspce.outerfieldsserver.networking.NetSerializer;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.BitSet;
import java.util.stream.IntStream;


public class EntityUpdateContainer {
    // Reuse these to avoid constant allocation
    private PlayerEntity[] playerEntities = new PlayerEntity[50];
    private NonPlayerEntity[] nonPlayerEntities = new NonPlayerEntity[50];
    private ItemEntity[] itemEntities = new ItemEntity[50];
    private LocationEntity[] locationEntities = new LocationEntity[50];

    // Bit sets to easily flag if extra info need sent for first seen entities
    private final BitSet newPlayerBitset = new BitSet(50);
    private final BitSet newNonPlayerBitSet = new BitSet(50);
    private final BitSet newItemBitset = new BitSet(50);
    private final BitSet newLocationBitSet = new BitSet(50);

    // Keep track of count to overwrite array elements, this has little effect
    // on garbage collections since most entities are long-lived
    int playerEntCount = 0, playerEntLength = 50;
    int nonPlayerEntCount = 0, nonPlayerEntLength = 50;
    int itemEntCount = 0, itemEntLength = 50;
    int locationEntCount = 0, locationEntLength = 50;

    public void reset() {
        playerEntCount = nonPlayerEntCount = itemEntCount = locationEntCount = 0;
    }

    public void cleanup() {
        if (playerEntLength > 50) {
            playerEntities = new PlayerEntity[50];
            playerEntLength = 50;
        } else {
            Arrays.fill(playerEntities, null);
        }
        if (nonPlayerEntLength > 50) {
            nonPlayerEntities = new NonPlayerEntity[50];
            nonPlayerEntLength = 50;
        } else {
            Arrays.fill(nonPlayerEntities, null);

        }
        if (itemEntLength > 50) {
            itemEntities = new ItemEntity[50];
            itemEntLength = 50;
        } else {
            Arrays.fill(itemEntities, null);
        }
        if (locationEntLength > 50) {
            locationEntities = new LocationEntity[50];
            locationEntLength = 50;
        } else {
            Arrays.fill(locationEntities, null);

        }
    }

    public void addNewEntity(Entity entity, boolean isNew) {
        switch (entity.entityType()) {
            case PLAYER -> {
                if (playerEntCount == playerEntLength) {
                    playerEntities = Arrays.copyOf(playerEntities, (int) (playerEntLength * 1.2));
                }
                newPlayerBitset.set(playerEntCount, isNew);
                playerEntities[playerEntCount++] = (PlayerEntity) entity;
            }
            case NON_PLAYER -> {
                if (nonPlayerEntCount == nonPlayerEntLength) {
                    nonPlayerEntities = Arrays.copyOf(nonPlayerEntities, (int) (nonPlayerEntLength * 1.2));
                }
                newNonPlayerBitSet.set(nonPlayerEntCount, isNew);
                nonPlayerEntities[nonPlayerEntCount++] = (NonPlayerEntity) entity;
            }
            case ITEM -> {
                if (itemEntLength == itemEntCount) {
                    itemEntities = Arrays.copyOf(itemEntities, (int) (itemEntLength * 1.2));
                }
                newItemBitset.set(itemEntCount, isNew);
                itemEntities[itemEntCount++] = (ItemEntity) entity;
            }
            case LOCATION -> {
                if (locationEntCount == locationEntLength) {
                    locationEntities = Arrays.copyOf(locationEntities, (int) (locationEntLength * 1.2));
                }
                newLocationBitSet.set(locationEntCount, isNew);
                locationEntities[locationEntCount++] = (LocationEntity) entity;
            }
        }
    }

    public byte[] getAsEntityPayLoad() {
        int playerBytes = IntStream.range(0, playerEntCount)
                .map(i -> newPlayerBitset.get(i)
                        ? NetSerializer.NEW_CHARACTER_BYTES
                        : NetSerializer.ENTITY_UPDATE_BYTES
                        + playerEntities[i].states().length)
                .sum();

        int nonPlayerBits = IntStream.range(0, nonPlayerEntCount)
                .map(i -> newNonPlayerBitSet.get(i)
                        ? NetSerializer.NEW_CHARACTER_BYTES
                        : NetSerializer.ENTITY_UPDATE_BYTES
                        + nonPlayerEntities[i].states().length)
                .sum();

        int itemBytes = IntStream.range(0, itemEntCount)
                .map(i -> newItemBitset.get(i)
                        ? NetSerializer.NEW_ITEM_BYTES
                        : NetSerializer.ENTITY_UPDATE_BYTES
                        + itemEntities[i].states().length)
                .sum();

        int locationBytes = IntStream.range(0, locationEntCount)
                .map(i -> newLocationBitSet.get(i)
                        ? NetSerializer.NEW_LOCATION_BYTES
                        : NetSerializer.ENTITY_UPDATE_BYTES
                        + locationEntities[i].states().length)
                .sum();

        int payloadSize = playerBytes + nonPlayerBits + itemBytes + locationBytes;
        int msgSize = 1 + 4 + payloadSize;

        ByteBuffer buffer = ByteBuffer.allocate(msgSize);
        buffer.put(NetMsgOut.EntityUpdate.value);
        buffer.putInt(payloadSize);

        for (int i = 0; i < playerEntCount; ++i) {
            NetSerializer.newPlayerToBuffer(buffer, playerEntities[i]);
        }
        for (int i = 0; i < nonPlayerEntCount; ++i) {
            NetSerializer.newNonPlayerToBuffer(buffer, nonPlayerEntities[i]);
        }
        for (int i = 0; i < itemEntCount; ++i) {
            NetSerializer.newItemToBuffer(buffer, itemEntities[i]);
        }
        for (int i = 0; i < locationEntCount; ++i) {
            NetSerializer.newLocationToBuffer(buffer, locationEntities[i]);
        }
        cleanup();
        return buffer.array();
    }
}
