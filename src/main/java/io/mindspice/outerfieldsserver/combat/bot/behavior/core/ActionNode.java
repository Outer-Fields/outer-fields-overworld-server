package io.mindspice.outerfieldsserver.combat.bot.behavior.core;

import io.mindspice.outerfieldsserver.combat.bot.behavior.logic.Action;
import io.mindspice.outerfieldsserver.combat.bot.state.BotPlayerState;
import io.mindspice.outerfieldsserver.combat.bot.state.TreeFocusState;
import io.mindspice.outerfieldsserver.combat.enums.PawnIndex;
import io.mindspice.outerfieldsserver.core.Settings;
import io.mindspice.outerfieldsserver.util.gamelogger.GameLogger;

import static io.mindspice.outerfieldsserver.combat.bot.behavior.core.Node.Type.ACTION;


public class ActionNode extends Node {
    private final Action action;

    public ActionNode(Action action, String name) {
        super(ACTION, name);
        this.action = action;
    }

    @Override
    public boolean travel(BotPlayerState botPlayerState, TreeFocusState focusState, PawnIndex selfIndex) {
        boolean rtn = action.doAction(botPlayerState, focusState, selfIndex);
        if (rtn) {
            focusState.action = name;
            focusState.decisions.add(name);
            if (Settings.GET().gameLogging) {
                GameLogger.GET().addBotDecision(
                        botPlayerState.getRoomId(), botPlayerState.getId(), selfIndex, focusState.decisions
                );
            }
        }
        return rtn;
        //return action.doAction(botPlayerState, focusState, selfIndex);
    }
}
