package io.mindspice.outerfieldsserver.combat.cards;

import io.mindspice.outerfieldsserver.combat.enums.PawnIndex;
import io.mindspice.outerfieldsserver.combat.gameroom.action.ActionReturn;
import io.mindspice.outerfieldsserver.combat.gameroom.state.PlayerMatchState;

public interface Potion {
   ActionReturn consumePotion(PlayerMatchState player, PawnIndex targetPawn);
}
