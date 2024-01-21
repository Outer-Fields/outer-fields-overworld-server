package io.mindspce.outerfieldsserver.ai.thought.data;

import io.mindspce.outerfieldsserver.components.dataclasses.ContainedEntity;
import io.mindspce.outerfieldsserver.components.player.ViewRect;
import io.mindspce.outerfieldsserver.enums.FactionType;
import io.mindspice.mindlib.data.geometry.IRect2;
import io.mindspice.mindlib.data.geometry.IVector2;

import java.util.List;


public class AttackFocus {
    public ViewRect viewRect;
    public ContainedEntity trackedEntity;
    public List<FactionType> aggressiveTowards;
    public final IVector2 spawnPos;
    public final IRect2 wanderArea;
    public long lastTargetCheck = -1;
    public final long checkTimeInterval;
    public final int attackDistance;

    public AttackFocus(ViewRect viewRect, List<FactionType> aggressiveTowards, IVector2 spawnPos, IRect2 wanderArea,
            long checkTimeInterval, int attackDistance) {
        this.viewRect = viewRect;
        this.aggressiveTowards = aggressiveTowards;
        this.spawnPos = spawnPos;
        this.checkTimeInterval = checkTimeInterval;
        this.attackDistance = attackDistance;
        this.wanderArea = IRect2.fromCenter(spawnPos, wanderArea.size());
    }
}
