package io.mindspice.outerfieldsserver.combat.bot;


import io.mindspice.outerfieldsserver.combat.bot.state.BotPlayerState;
import io.mindspice.outerfieldsserver.combat.gameroom.state.PlayerGameState;
import io.mindspice.outerfieldsserver.data.PlayerData;
import io.mindspice.outerfieldsserver.combat.schema.PawnSet;
import io.mindspice.outerfieldsserver.entities.PlayerEntity;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


public class BotFactory {

    private static final BotFactory INSTANCE = new BotFactory();
    private final ScheduledExecutorService botExecutor = Executors.newSingleThreadScheduledExecutor();


    private BotFactory() {
    }

    public static BotFactory GET(){
        return INSTANCE;
    }

    public BotPlayerState getBotPlayerState(PlayerGameState enemyPlayerState, int enemyPawnSetLevel) {
        PawnSet botPawnSet = PawnSet.getRandomPawnSet(enemyPawnSetLevel);
        BotPlayer botPlayer = new BotPlayer();
        botPlayer.setFullPlayerData(new PlayerData("Bot"));
        return new BotPlayerState(botPlayer, enemyPlayerState, botPawnSet, botExecutor);
    }
    public BotPlayerState getHighLvlBotPlayerState(PlayerGameState enemyPlayerState) {
        PawnSet botPawnSet = PawnSet.getRandomPawnSet2();
        BotPlayer botPlayer = new BotPlayer();
        botPlayer.setFullPlayerData(new PlayerData("Bot"));
        return new BotPlayerState(botPlayer, enemyPlayerState, botPawnSet, botExecutor);
    }

    public BotPlayerState getBotPlayerStateForBotVsBot(PlayerEntity botPlayer, int enemyPawnSetLevel) {
        PawnSet botPawnSet = PawnSet.getRandomPawnSet(enemyPawnSetLevel);
        return new BotPlayerState(botPlayer, botPawnSet, botExecutor);
    }

    public BotPlayerState getCombatBot(PlayerGameState playerState, int enemyId){
        return getBotPlayerState(playerState, 150); // FIXME this needs to use the id to look up a preset for the bots cards
    }



}