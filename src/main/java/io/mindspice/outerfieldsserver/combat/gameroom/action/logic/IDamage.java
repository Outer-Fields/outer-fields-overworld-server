package io.mindspice.outerfieldsserver.combat.gameroom.action.logic;

import io.mindspice.outerfieldsserver.combat.enums.ActionType;
import io.mindspice.outerfieldsserver.combat.enums.SpecialAction;
import io.mindspice.outerfieldsserver.combat.enums.StatType;
import io.mindspice.outerfieldsserver.combat.gameroom.state.PawnInterimState;

import java.util.Map;


public interface IDamage {
    void doDamage(PawnInterimState player, PawnInterimState target, ActionType actionType,
                  SpecialAction special, Map<StatType, Integer> damage, boolean isSelf);
}
