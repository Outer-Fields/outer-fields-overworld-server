package io.mindspce.outerfieldsserver.entities.player;

import io.mindspce.outerfieldsserver.enums.EntityType;
import io.mindspce.outerfieldsserver.enums.PlayerUpdateLevel;
import io.mindspce.outerfieldsserver.networking.outgoing.NetEntityUpdate;
import io.mindspce.outerfieldsserver.entities.item.ItemEntity;
import io.mindspce.outerfieldsserver.entities.locations.LocationEntity;
import io.mindspce.outerfieldsserver.entities.nonplayer.NonPlayerEntity;

import java.util.ArrayList;



public class EntityUpdateData {
    private final NetEntityUpdate<PlayerEntity> playerUpdates;
    private final NetEntityUpdate<NonPlayerEntity> npcUpdates;
    private final NetEntityUpdate<NonPlayerEntity> enemyUpdates;
    private final NetEntityUpdate<ItemEntity> itemUpdates;
    private final NetEntityUpdate<LocationEntity> locationUpdates;

    public EntityUpdateData() {
        playerUpdates = new NetEntityUpdate<PlayerEntity>(EntityType.PLAYER, new ArrayList<>(25));
        npcUpdates = new NetEntityUpdate<NonPlayerEntity>(EntityType.NPC, new ArrayList<>(25));
        enemyUpdates = new NetEntityUpdate<NonPlayerEntity>(EntityType.ENEMY, new ArrayList<>(25));
        itemUpdates = new NetEntityUpdate<ItemEntity>(EntityType.ITEM, new ArrayList<>(25));
        locationUpdates = new NetEntityUpdate<LocationEntity>(EntityType.LOCATION, new ArrayList<>(25));
    }

    public void reset(PlayerUpdateLevel updateLevel) {
        playerUpdates.clear();
        if (updateLevel == PlayerUpdateLevel.PLAYERS_AND_NPC || updateLevel == PlayerUpdateLevel.ALL) {
            npcUpdates.clear();
            enemyUpdates.clear();
        }
        if (updateLevel == PlayerUpdateLevel.ALL) {
            itemUpdates.clear();
            locationUpdates.clear();
        }
    }

    public void addPlayerUpdate(PlayerEntity player) {
        playerUpdates.add(player);
    }

    public void addNpcUpdate(NonPlayerEntity npc) {
        npcUpdates.add(npc);
    }

    public void addEnemyUpdate(NonPlayerEntity enemy) {
        enemyUpdates.add(enemy);
    }

    public void addItemUpdate(ItemEntity item) {
        itemUpdates.add(item);
    }

    public void addLocationUpdate(LocationEntity location) {
        locationUpdates.add(location);
    }

    public NetEntityUpdate<PlayerEntity> getPlayerUpdates() {
        return playerUpdates;
    }

    public NetEntityUpdate<NonPlayerEntity> getNpcUpdates() {
        return npcUpdates;
    }

    public NetEntityUpdate<NonPlayerEntity> getEnemyUpdates() {
        return enemyUpdates;
    }

    public NetEntityUpdate<ItemEntity> getItemUpdates() {
        return itemUpdates;
    }

    public NetEntityUpdate<LocationEntity> getLocationUpdates() {
        return locationUpdates;
    }

}
