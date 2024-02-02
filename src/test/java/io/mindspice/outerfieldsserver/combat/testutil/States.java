package io.mindspice.outerfieldsserver.combat.testutil;

import io.mindspice.outerfieldsserver.combat.GameTests;
import io.mindspice.outerfieldsserver.combat.bot.BotFactory;
import io.mindspice.outerfieldsserver.combat.bot.state.BotPlayerState;
import io.mindspice.outerfieldsserver.combat.gameroom.MatchInstance;
import io.mindspice.outerfieldsserver.data.PlayerData;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


public class States {

    public static MatchInstance getReadiedGameRoom() {
        BotFactory botFactory = BotFactory.GET();
        GameTests.TestPlayer p1 = new GameTests.TestPlayer(1);
        GameTests.TestPlayer p2 = new GameTests.TestPlayer(2);
        p1.setFullPlayerData(new PlayerData("Player1"));
        p2.setFullPlayerData(new PlayerData("Player2"));
        BotPlayerState player1 = botFactory.getBotPlayerStateForBotVsBot(p1, 150);
        BotPlayerState player2 = botFactory.getBotPlayerStateForBotVsBot(p2, 150);
        player1.setEnemyPlayer(player2);
        player2.setEnemyPlayer(player1);
        p1.setPlayerGameState(player1);
        p2.setPlayerGameState(player2);
        MatchInstance game = new MatchInstance(player1, player2, false);
        game.setReady(1);
        game.setReady(2);
        return game;
    }
}
