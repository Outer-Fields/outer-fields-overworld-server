package io.mindspice.outerfieldsserver.combat.gameroom.action.logic;

import io.mindspice.outerfieldsserver.combat.cards.Card;
import io.mindspice.outerfieldsserver.combat.enums.SpecialAction;
import io.mindspice.outerfieldsserver.combat.gameroom.state.PawnInterimState;

import java.util.List;

public interface IEffectCalc {
   void doEffect(IEffect effectLogic, List<PawnInterimState> playerStates, List<PawnInterimState> targetStates, Card card, SpecialAction special);

   boolean isSelf();
   boolean isPos();
   int getMulti();
}

