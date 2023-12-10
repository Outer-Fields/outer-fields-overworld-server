package io.mindspce.outerfieldsserver.objects.player;

import io.mindspce.outerfieldsserver.area.AreaInstance;
import io.mindspce.outerfieldsserver.area.ChunkData;
import io.mindspce.outerfieldsserver.enums.PlayerUpdateLevel;
import io.mindspice.mindlib.data.geometry.IRect2;
import io.mindspice.mindlib.data.geometry.IVector2;


public class PlayerCharacter extends PlayerEntity {
    private final PlayerLocation location;
    private final EntityUpdateData entityUpdateData = new EntityUpdateData();

    public PlayerCharacter(AreaInstance currArea) {
        this.location = new PlayerLocation(currArea);

    }

    public void sendWorldUpdate(PlayerUpdateLevel updateLevel) {
        IRect2 updateBounds = location.getUpdateBounds();
        ChunkData[][] localGrid = location.getLocalGrid();
        entityUpdateData.reset(updateLevel);
        for (int x = 0; x < 3; ++x) {
            for (int y = 0; y < 3; ++y) {
                ChunkData chunk = localGrid[x][y];
                updateCheck(chunk, updateBounds, updateLevel);
            }
        }

        switch (updateLevel) {
            case PLAYERS_ONLY -> {


            }
            case PLAYERS_AND_NPC -> { }
            case ALL -> { }
        }
    }

    private void updateCheck(ChunkData chunk, IRect2 updateBounds, PlayerUpdateLevel updateLevel) {
        if (chunk == null) { return; }
        if (!updateBounds.intersects(chunk.getBoundsRect())) {
            return;
        }

        for (var player : chunk.getActivePlayers()) {
            if (updateBounds.withinBounds(player.getGlobalPos())) {
                entityUpdateData.addPlayerUpdate(player.asEntity());
            }
        }

        if (updateLevel == PlayerUpdateLevel.PLAYERS_AND_NPC || updateLevel == PlayerUpdateLevel.ALL) {
            for (var npc : chunk.getActiveNpcs()) {
                if (updateBounds.withinBounds(npc.getGlobalPos())) {
                    entityUpdateData.addNpcUpdate(npc.asEntity());
                }
            }
            for (var enemy : chunk.getActiveEnemies()) {
                if (updateBounds.withinBounds(enemy.getGlobalPos())) {
                    entityUpdateData.addEnemyUpdate(enemy.asEntity());
                }
            }
        }

        if (updateLevel == PlayerUpdateLevel.ALL) {
            for (var item : chunk.getActiveItems()) {
                if (updateBounds.withinBounds(item.getGlobalPos())) {
                    entityUpdateData.addItemUpdate(item.asEntity());
                }
            }
            for (var location : chunk.getLocationStates()) {
                if (updateBounds.withinBounds(location.globalPos)) {
                    entityUpdateData.addLocationUpdate(location.asEntity());
                }
            }
        }
    }

    public IVector2 getLocalPos() {
        return location.getLocalPos();
    }

    @Override
    public IVector2 getGlobalPos() {
        return location.getGlobalPos();
    }

    public void getViewUpdate() {

    }

    @Override
    public PlayerEntity asEntity() {
        return null;
    }
}
