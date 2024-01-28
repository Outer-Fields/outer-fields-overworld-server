package io.mindspice.outerfieldsserver.combat.gameroom.action.logic;

import io.mindspice.outerfieldsserver.combat.enums.StatType;
import io.mindspice.outerfieldsserver.combat.gameroom.action.ActionReturn;
import io.mindspice.outerfieldsserver.combat.gameroom.pawn.Pawn;

import java.util.Map;


public class Cost {

    public static final Cost GET = new Cost();

    private Cost() {
    }

    public boolean doCost(Pawn playerPawn, Map<StatType, Integer> cardCost, ActionReturn actionReturn) {
        for (var stat : cardCost.entrySet()) {
            if (playerPawn.getStat(stat.getKey()) < stat.getValue()) {
                return false;
            }
        }
        playerPawn.updateStats(cardCost, false);
        actionReturn.setCost(cardCost);
        return true;
    }
}
