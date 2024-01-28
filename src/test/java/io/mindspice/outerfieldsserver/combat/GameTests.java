package io.mindspice.outerfieldsserver.combat;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.mindspice.databaseservice.client.schema.Card;
import io.mindspice.mindlib.data.geometry.IVector2;
import io.mindspice.mindlib.util.JsonUtils;
import io.mindspice.outerfieldsserver.combat.bot.BotFactory;
import io.mindspice.outerfieldsserver.combat.bot.state.BotPlayerState;
import io.mindspice.outerfieldsserver.combat.cards.AbilityCard;
import io.mindspice.outerfieldsserver.combat.cards.ActionCard;
import io.mindspice.outerfieldsserver.combat.cards.PowerCard;
import io.mindspice.outerfieldsserver.combat.enums.PawnIndex;
import io.mindspice.outerfieldsserver.combat.enums.PlayerAction;
import io.mindspice.outerfieldsserver.combat.enums.StatType;
import io.mindspice.outerfieldsserver.combat.gameroom.MatchInstance;
import io.mindspice.outerfieldsserver.combat.gameroom.action.ActivePower;
import io.mindspice.outerfieldsserver.combat.gameroom.effect.ActiveEffect;
import io.mindspice.outerfieldsserver.combat.gameroom.pawn.Pawn;
import io.mindspice.outerfieldsserver.combat.gameroom.state.PlayerGameState;

import io.mindspice.outerfieldsserver.core.Settings;
import io.mindspice.outerfieldsserver.combat.schema.websocket.incoming.NetGameAction;
import io.mindspice.outerfieldsserver.data.PlayerData;
import io.mindspice.outerfieldsserver.enums.AreaId;
import io.mindspice.outerfieldsserver.enums.ClothingItem;
import io.mindspice.outerfieldsserver.util.Log;
import io.mindspice.outerfieldsserver.entities.PlayerEntity;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;


public class GameTests {
    private BotFactory botFactory = BotFactory.GET();
    private ScheduledExecutorService exec = Executors.newScheduledThreadPool(8);

    // Plays a full bot game. Debug log inputs/outputs. Settings.advancedDebug can be set to true
    // to also enable the stats of all pawns after each action and various other detailed logging.
    // Debugging the full integration is challenging, running advance debug logging provides enough information
    // to manually follow the actions and game flow to hopefully identify bug
    // To run quickly chance the delay time in BotPlayerStat doTurn to 0
    @Test
    public void runTestGame() throws InterruptedException {
        var activeGames = Collections.synchronizedCollection(new ArrayList<>(20));
        //   while (true) {
//            if (activeGames.size() >= 2) {
//                Thread.sleep(10000);
//                continue;
//            }
        TestPlayer p1 = new TestPlayer(ThreadLocalRandom.current().nextInt(99999));
        Settings.GET();
        TestPlayer p2 = new TestPlayer(ThreadLocalRandom.current().nextInt(99999));
        p1.setFullPlayerData(new PlayerData("Player1"));
        p2.setFullPlayerData(new PlayerData("Player2"));
        int rnd = ThreadLocalRandom.current().nextInt(130, 200);
        int lvl1 = (int) ThreadLocalRandom.current().nextDouble(0.93, 1.07) * rnd;
        int lvl2 = (int) ThreadLocalRandom.current().nextDouble(0.93, 1.07) * rnd;
        BotPlayerState player1 = botFactory.getBotPlayerStateForBotVsBot(p1, lvl1);
        BotPlayerState player2 = botFactory.getBotPlayerStateForBotVsBot(p2, lvl2);
        player1.setEnemyPlayer(player2);
        player2.setEnemyPlayer(player1);
        p1.setPlayerGameState(player1);
        p2.setPlayerGameState(player2);
        MatchInstance game = new MatchInstance(player1, player2, false);

        ScheduledFuture<?> gameProc = exec.scheduleWithFixedDelay(
                game,
                0,
                200,
                TimeUnit.MILLISECONDS
        );
        activeGames.add(gameProc);
        game.setReady(p1.getPlayerId());
        game.setReady(p2.getPlayerId());
        game.getResultFuture().thenAccept(result -> {
            gameProc.cancel(true);
            activeGames.remove(gameProc);
            System.out.println("Finsihed on Round:" + result.roundCount());
        }).exceptionally(ex -> {
            Log.SERVER.error(this.getClass(), "Error on finalize bot match callback");
            return null;
        });
        // }

        Thread.sleep(10000000);
    }

    public static class TestPlayer extends PlayerEntity {
        PlayerGameState pgs;

        public TestPlayer(int id) {
            super(-1, id, "Nonnome", List.of(),
                    new ClothingItem[]{ClothingItem.EMPTY, ClothingItem.EMPTY, ClothingItem.EMPTY,
                            ClothingItem.EMPTY, ClothingItem.EMPTY, ClothingItem.EMPTY,},
                    AreaId.TEST, IVector2.negOne(), null);
        }

        public void setPlayerGameState(PlayerGameState psg) {
            this.pgs = psg;
        }

        @Override
        public void send(Object obj) {
            System.out.println("Msg Out | Id:" + pgs.getId());
            System.out.println(writePretty(obj));
        }

        @Override
        public boolean isConnected() {
            return true;
        }

        @Override
        public void onMessage(NetGameAction msg) {
            if (inCombat) {
                System.out.println("Action In | Id:" + pgs.getId());
                System.out.println(writePretty(msg));
                matchInstance.addMsg(id, msg);
            } else {
                Log.SERVER.debug(this.getClass(), getLoggable() + " | Websocket message while not in game");
            }
            // if they are not in a game ignore their WS messages
        }
    }


    public record ActionIn(
            PlayerAction action,
            Card cardPlayed,
            PawnIndex playingPawn,
            PawnIndex targetPawn
    ) {

    }


    public record PawnStats(
            PawnIndex pawnIndex,
            Map<StatType, Integer> stats,
            List<ActiveEffect> effects,
            List<ActivePower> powers,
            ActionCard actionCard1,
            ActionCard actionCard2,
            AbilityCard abilityCard1,
            AbilityCard abilityCard2,
            PowerCard powerCard
    ) {
        public PawnStats(Pawn pawn) {
            this(
                    pawn.getIndex(),
                    pawn.getStatsMap(),
                    pawn.getStatusEffects(),
                    pawn.getActivePowers(),
                    pawn.getActionCard(1),
                    pawn.getActionCard(2),
                    pawn.getAbilityCard(1),
                    pawn.getAbilityCard(2),
                    pawn.getPowerCard()
            );
        }
    }

    public static String writePretty(Object obj) {
        try {
            return JsonUtils.getMapper().writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}





