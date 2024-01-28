package io.mindspice.outerfieldsserver.combat.gameroom;

import io.mindspice.outerfieldsserver.combat.gameroom.state.ActiveRoundState;
import io.mindspice.outerfieldsserver.combat.gameroom.state.PlayerGameState;
import io.mindspice.outerfieldsserver.combat.schema.websocket.outgoing.game.NetStatus;
import io.mindspice.outerfieldsserver.util.Log;
import io.mindspice.outerfieldsserver.util.gamelogger.GameLogger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;


public class CombatInstance extends MatchInstance implements Runnable {

    // init the player state and combat managers before join to room, this allows easy insertion of bots
    public CombatInstance(PlayerGameState p1GameState, PlayerGameState p2GameState) {
        super(p1GameState, p2GameState, true);
    }

    @Override
    protected boolean readyCheck() {
        if (player1.isReady() && player2.isReady()) {
            player1.send(new NetStatus(true, false, 0));
            player2.send(new NetStatus(true, false, 0));
            round = 1;
            isStarted = true;
            activeRound = new ActiveRoundState(player1, player2, 1);
            Log.SERVER.debug(this.getClass(), "CombatRoom: " + roomId + " | Starting comat both players ready");
            return true;
        }
        if (round == 0 && Instant.now().getEpochSecond() - initTime > 30) { // This handles failing to read at start
            Log.SERVER.info("CombatRoom: " + roomId + " starting combat without player being ready");
            return true;
        }
        return false;
    }

    @Override
    protected boolean pauseCheck() {
        long now = Instant.now().getEpochSecond();
        for (var player : disconnPlayers) {
            if (player.getPlayer().isConnected() && !player.isReady()) {
                if (now - lastGameRestore < 3) { continue; }
                lastGameRestore = now;
                player.getCombatManager().sendGameRestore();
            } else if (player.getPlayer().isConnected() && player.isReady()) {
                if (now - lastGameRestore < 3) { continue; }
                lastGameRestore = now;
                player.getCombatManager().sendGameRestoreInfo(
                        getActivePlayerId() == player.getId() ? activeRound.getActiveTurn().getActivePawns() : List.of(),
                        getActivePlayerId() == player.getId()
                );
                var enemy = player.getId() == player1.getId() ? player2 : player1;
                enemy.getCombatManager().sendTurnUpdate(
                        getActivePlayerId() == enemy.getId() ? activeRound.getActiveTurn().getActivePawns() : List.of(),
                        getActivePlayerId() == enemy.getId());
                Log.SERVER.info("CombatRoom: " + roomId + " | Player reconnected: " + player);
                disconnPlayers.remove(player); //CoW Array so we can do this
            }
        }
        return true;
    }

    @Override
    protected boolean connTest() {
        if (!player1.getPlayer().isConnected() && !disconnPlayers.contains(player1)) {
            disconnPlayers.add(player1);
            Log.SERVER.info("CombatRoom: " + roomId + " | Player disconnected: " + player1.getPlayer().getLoggable());
        }
        if (!player2.getPlayer().isConnected() && !disconnPlayers.contains(player2)) {
            disconnPlayers.add(player2);

            Log.SERVER.info("CombatRoom: " + roomId + " | Player disconnected: " + player2.getPlayer().getLoggable());
        }
        return disconnPlayers.isEmpty();

    }


    @Override
    public void update() {
        try {
            long now = Instant.now().getEpochSecond();

            if (gameOver) { return; } // No reason to run, results will soon be gotten and room destroyed

            if (!isStarted) {
                if (!readyCheck()) { return; }
            }

            if (isPaused) {
                if (!pauseCheck()) { return; }
            }

            if (now - connTimeTimer > 3) {
                connTimeTimer = now;
                connTest();
            }

            switch (activeRound.update()) {
                case ACTIVE -> processActionQueue();
                case AWAITING_FINISH -> { return; }
                case FINISHED -> {
                    round++; // atomic doesn't matter, we just need it to be observable
                    activeRound = new ActiveRoundState(player1, player2, round);
                    Log.SERVER.debug(this.getClass(), "CombatRoom: " + roomId + " | Finished round: " + round);
                }
                case GAME_OVER -> {
                    if (doGameLog) {
                        try {
                            GameLogger.GET().endLog(roomId);
                        } catch (Exception e) {
                            Log.SERVER.error(this.getClass(), "CombatRoom: " + roomId + " | Error writing endgame log ", e);

                        }
                    }
                    Log.SERVER.info("CombatRoom: " + roomId + " | Combat over | Round: " + round + " | Total Time: "
                            + BigDecimal.valueOf((Instant.now().getEpochSecond() - initTime) / 60.0).setScale(3, RoundingMode.HALF_UP));
                    gameOver = true;
                    endGame();
                }
            }
        } catch (Exception e) {
            Log.SERVER.error(this.getClass(), "CombatRoom: " + roomId + " | Exception in update loop ", e);
        }
    }
}
