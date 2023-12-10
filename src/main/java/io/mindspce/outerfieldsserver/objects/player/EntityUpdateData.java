package io.mindspce.outerfieldsserver.objects.player;

import io.mindspce.outerfieldsserver.enums.EntityType;
import io.mindspce.outerfieldsserver.enums.PlayerUpdateLevel;
import io.mindspce.outerfieldsserver.networking.outgoing.NetEntityUpdate;
import io.mindspce.outerfieldsserver.objects.item.ItemEntity;
import io.mindspce.outerfieldsserver.objects.locations.LocationEntity;
import io.mindspce.outerfieldsserver.objects.nonplayer.EnemyEntity;
import io.mindspce.outerfieldsserver.objects.nonplayer.NpcEntity;

import java.util.ArrayList;



public class EntityUpdateData {
    private final NetEntityUpdate<PlayerEntity> playerUpdates;
    private final NetEntityUpdate<NpcEntity> npcUpdates;
    private final NetEntityUpdate<EnemyEntity> enemyUpdates;
    private final NetEntityUpdate<ItemEntity> itemUpdates;
    private final NetEntityUpdate<LocationEntity> locationUpdates;

    public EntityUpdateData() {
        playerUpdates = new NetEntityUpdate<>(EntityType.PLAYER, new ArrayList<>(25));
        npcUpdates = new NetEntityUpdate<>(EntityType.NPC, new ArrayList<>(25));
        enemyUpdates = new NetEntityUpdate<>(EntityType.ENEMY, new ArrayList<>(25));
        itemUpdates = new NetEntityUpdate<>(EntityType.ITEM, new ArrayList<>(25));
        locationUpdates = new NetEntityUpdate<>(EntityType.LOCATION, new ArrayList<>(25));
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

    public void addNpcUpdate(NpcEntity npc) {
        npcUpdates.add(npc);
    }

    public void addEnemyUpdate(EnemyEntity enemy) {
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

    public NetEntityUpdate<NpcEntity> getNpcUpdates() {
        return npcUpdates;
    }

    public NetEntityUpdate<EnemyEntity> getEnemyUpdates() {
        return enemyUpdates;
    }

    public NetEntityUpdate<ItemEntity> getItemUpdates() {
        return itemUpdates;
    }

    public NetEntityUpdate<LocationEntity> getLocationUpdates() {
        return locationUpdates;
    }

}
