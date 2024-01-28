package io.mindspice.outerfieldsserver.combat.bot.behavior.core;

import io.mindspice.outerfieldsserver.combat.bot.state.BotPlayerState;
import io.mindspice.outerfieldsserver.combat.bot.state.TreeFocusState;
import io.mindspice.outerfieldsserver.combat.enums.PawnIndex;

import static io.mindspice.outerfieldsserver.combat.bot.behavior.core.Node.Type.ROOT;


public class RootNode extends Node {

    public RootNode() {
        super(ROOT, "Root");
    }

    @Override
    public boolean travel(BotPlayerState botPlayerState, TreeFocusState focusState, PawnIndex selfIndex) {
        for (Node child : adjacentNodes) {
            if (child.travel(botPlayerState, focusState, selfIndex)) { return true; }
        }
        //TODO logic for if all decisions fail
        return false;
    }
}
