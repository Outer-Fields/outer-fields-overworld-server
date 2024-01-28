package io.mindspice.outerfieldsserver.combat.bot.behavior.logic;

import io.mindspice.outerfieldsserver.combat.bot.state.BotPlayerState;
import io.mindspice.outerfieldsserver.combat.bot.state.TreeFocusState;
import io.mindspice.outerfieldsserver.combat.enums.PawnIndex;


public abstract class Action {
    public abstract boolean doAction(BotPlayerState botPlayerState, TreeFocusState focusState, PawnIndex selfIndex);
}
