package io.mindspice.outerfieldsserver.combat.bot.behavior.logic;

import io.mindspice.outerfieldsserver.combat.bot.state.BotPlayerState;
import io.mindspice.outerfieldsserver.combat.bot.state.TreeFocusState;
import io.mindspice.outerfieldsserver.combat.enums.PawnIndex;


public abstract class Decision {
    private final String name;

    public Decision(String name) {
        this.name = name;
    }

    public abstract boolean getDecision(BotPlayerState botPlayerState, TreeFocusState focusState, PawnIndex selfIndex);

    public String getName() {
        return name;
    }

}
