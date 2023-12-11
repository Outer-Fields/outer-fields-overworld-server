package io.mindspce.outerfieldsserver.entities.player;

import io.mindspce.outerfieldsserver.area.AreaInstance;
import io.mindspce.outerfieldsserver.area.ChunkData;
import io.mindspce.outerfieldsserver.enums.PlayerUpdateLevel;
import io.mindspice.mindlib.data.geometry.IRect2;
import io.mindspice.mindlib.data.geometry.IVector2;


public class PlayerCharacter extends PlayerEntity {
    private PlayerSession session;
    private final PlayerPosition positionData;
    private final EntityUpdateData entityUpdateData = new EntityUpdateData();

    public PlayerCharacter(AreaInstance currArea) {
        this.positionData = new PlayerPosition(currArea);

    }

    public synchronized void sendWorldUpdate(PlayerUpdateLevel updateLevel) {
        IRect2 updateBounds = positionData.getUpdateBounds();
        ChunkData[][] localGrid = positionData.getLocalGrid();
        entityUpdateData.reset(updateLevel);
        for (int x = 0; x < 3; ++x) {
            for (int y = 0; y < 3; ++y) {
                ChunkData chunk = localGrid[x][y];
                updateCheck(chunk, updateBounds, updateLevel);
            }
        }

        switch (updateLevel) {
            case PLAYERS_ONLY -> {
                sendPlayerUpdate();
            }
            case PLAYERS_AND_NPC -> {
                sendPlayerUpdate();
                sendNpcEnemyUpdates();
            }
            case ALL -> {
                sendPlayerUpdate();
                sendNpcEnemyUpdates();
                sendItemLocationUpdates();
            }
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

    private void sendPlayerUpdate() { session.send(entityUpdateData.getPlayerUpdates()); }

    private void sendNpcEnemyUpdates() {
        session.send(entityUpdateData.getNpcUpdates());
        session.send(entityUpdateData.getEnemyUpdates());
    }

    private void sendItemLocationUpdates() {
        session.send(entityUpdateData.getItemUpdates());
        session.send(entityUpdateData.getLocationUpdates());
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

    @Override
    public PlayerEntity asEntity() {
        return null;
    }
}
