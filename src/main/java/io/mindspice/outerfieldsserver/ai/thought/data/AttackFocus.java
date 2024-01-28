package io.mindspice.outerfieldsserver.ai.thought.data;

import io.mindspice.outerfieldsserver.components.dataclasses.ContainedEntity;
import io.mindspice.outerfieldsserver.components.npc.NPCMovement;
import io.mindspice.outerfieldsserver.components.player.ViewRect;
import io.mindspice.outerfieldsserver.entities.AreaEntity;
import io.mindspice.outerfieldsserver.entities.CharacterEntity;
import io.mindspice.outerfieldsserver.entities.PlayerEntity;
import io.mindspice.outerfieldsserver.enums.EntityType;
import io.mindspice.outerfieldsserver.enums.FactionType;
import io.mindspice.mindlib.data.collections.lists.primative.IntList;
import io.mindspice.mindlib.data.geometry.IRect2;
import io.mindspice.mindlib.data.geometry.IVector2;

import java.util.List;


public class AttackFocus {
    public final NPCMovement movementController;
    public AreaEntity area;
    public final ViewRect viewRect;
    public ContainedEntity trackedEntity;
    public List<FactionType> enemyFactions;
    public IntList enemyEntities = new IntList(1);
    public IntList enemyPlayers = new IntList(1);
    public final IVector2 spawnPos;
    public final IRect2 wanderArea;
    public long lastTargetCheck = -1;
    public final long checkTimeInterval;
    public final int attackDistance;
    public long nextWanderTime = 0;
    public IVector2 wanderInterval;

    public AttackFocus(NPCMovement movementController, AreaEntity area, ViewRect viewRect,
            List<FactionType> dispisedFactions, IVector2 spawnPos, IVector2 wanderAreaSize,
            long checkTimeIntervalMs, int attackDistance, IVector2 wanderInterval) {
        this.movementController = movementController;
        this.area = area;
        this.viewRect = viewRect;
        this.enemyFactions = dispisedFactions;
        this.spawnPos = spawnPos;
        this.checkTimeInterval = checkTimeIntervalMs;
        this.attackDistance = attackDistance;
        this.wanderArea = IRect2.fromCenter(spawnPos, wanderAreaSize);
        this.wanderInterval = wanderInterval;
    }

    public void addEnemyEntity(int id) {
        enemyEntities.add(id);
    }

    public void addEnemyPlayers(int id) {
        enemyPlayers.add(id);
    }

    public void addEnemyFaction(FactionType faction) {
        this.enemyFactions.add(faction);
    }

    public void removeEnemyEntity(int id) {
        enemyEntities.removeAllValuesOf(id);
    }

    public void removeEnemyPlayers(int id) {
        enemyPlayers.removeAllValuesOf(id);
    }

    public void removeEnemyFaction(FactionType faction) {
        this.enemyFactions.remove(faction);
    }

    public boolean isAggressiveTowards(CharacterEntity entity) {
        if (entity.entityType() == EntityType.PLAYER) {
            if (enemyPlayers.contains(((PlayerEntity) entity).playerId())){
                return  true;
            }
        }
        return enemyFactions.contains(FactionType.ALL)
                || enemyFactions.stream().anyMatch(f -> entity.factions().contains(f))
                || enemyEntities.contains(entity.entityId());
    }
}
