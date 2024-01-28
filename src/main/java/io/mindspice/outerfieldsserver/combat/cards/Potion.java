package io.mindspice.outerfieldsserver.combat.cards;

import io.mindspice.outerfieldsserver.combat.enums.PawnIndex;
import io.mindspice.outerfieldsserver.combat.gameroom.action.ActionReturn;
import io.mindspice.outerfieldsserver.combat.gameroom.state.PlayerGameState;

public interface Potion {
   ActionReturn consumePotion(PlayerGameState player, PawnIndex targetPawn);
}
