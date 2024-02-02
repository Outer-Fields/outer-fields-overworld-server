package io.mindspice.outerfieldsserver.data;

import io.mindspice.outerfieldsserver.combat.enums.PawnIndex;
import io.mindspice.outerfieldsserver.combat.enums.StatType;
import io.mindspice.outerfieldsserver.combat.gameroom.effect.ActiveEffect;
import io.mindspice.outerfieldsserver.combat.gameroom.pawn.Pawn;

import java.util.List;
import java.util.Map;


public record OverWorldPawnState(
        PawnIndex pawnIndex,
        Map<StatType, Integer> stats,
        Map<StatType, Integer> statsMax,
        List<ActiveEffect> effects,
        boolean isDead
) {
    public static OverWorldPawnState fromPawn(Pawn pawn) {
        return new OverWorldPawnState(
                pawn.getIndex(),
                Map.copyOf(pawn.getStatsMap()),
                Map.copyOf(pawn.getStatsMaxMap()),
                List.copyOf(pawn.getStatusEffects()),
                pawn.isDead()
        );
    }
}


