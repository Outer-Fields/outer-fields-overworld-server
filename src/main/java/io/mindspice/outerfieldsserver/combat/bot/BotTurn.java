package io.mindspice.outerfieldsserver.combat.bot;

import io.mindspice.outerfieldsserver.combat.bot.behavior.BehaviorGraph;
import io.mindspice.outerfieldsserver.combat.bot.state.BotPlayerState;
import io.mindspice.outerfieldsserver.combat.bot.state.TreeFocusState;
import io.mindspice.outerfieldsserver.combat.enums.PawnIndex;
import io.mindspice.outerfieldsserver.util.Log;


public class BotTurn implements Runnable {
    private final PawnIndex pawnIndex;
    private final BehaviorGraph behaviorGraph = BehaviorGraph.getInstance();
    private final BotPlayerState botPlayerState;


    public BotTurn(BotPlayerState botPlayerState, PawnIndex pawnIndex) {
        this.pawnIndex = pawnIndex;
        this.botPlayerState = botPlayerState;
    }

    @Override
    public void run() {
        try {
            var focusState = new TreeFocusState();
            behaviorGraph.playTurn(botPlayerState, focusState, pawnIndex);
        } catch (Exception e) {
            Log.SERVER.error(this.getClass(), "Error Running Bot Turn On BotPlayerId "
                    + botPlayerState.getPlayerState().getId(), e
            );
        }
    }
}


