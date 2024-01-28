package io.mindspice.outerfieldsserver.combat;

import io.mindspice.outerfieldsserver.combat.cards.*;
import io.mindspice.outerfieldsserver.combat.enums.EffectType;
import io.mindspice.outerfieldsserver.combat.enums.PawnIndex;
import io.mindspice.outerfieldsserver.combat.gameroom.action.ActionReturn;
import io.mindspice.outerfieldsserver.combat.gameroom.pawn.Pawn;
import io.mindspice.outerfieldsserver.combat.gameroom.state.ActiveTurnState;
import io.mindspice.outerfieldsserver.combat.gameroom.state.PawnInterimState;
import io.mindspice.outerfieldsserver.combat.testutil.States;
import io.mindspice.outerfieldsserver.core.Settings;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static io.mindspice.outerfieldsserver.combat.enums.StatType.MP;
import static io.mindspice.outerfieldsserver.combat.enums.StatType.SP;


public class Random {

    @Test
    void listEffects() {
        Arrays.stream(EffectType.values()).forEach(s -> System.out.println("\"" + s + "\""));
    }

    @Test
    void debugActivePower() {
        var   card= WeaponCard.KNUCKLEDUSTER_GOLD;
        Pawn pawn = new Pawn(PawnIndex.PAWN1, PawnCard.OKRA_WARRIOR, TalismanCard.BALANCE_BEAD, null, null, List.of(),
                List.of(), List.of());
        Arrays.stream(card.getStats().getTargetEffects()).forEach(p -> System.out.println(p.amount));
        pawn.addStatusEffect(card.getStats().getTargetEffects()[0]);
        pawn.addStatusEffect(card.getStats().getTargetEffects()[0]);
        System.out.println(pawn.getStatusEffects());
        System.out.println(pawn.getStatusEffects().get(0).doEffect());
        var trues = 0;
        for (int i = 0; i < 1000; ++i) {
            if (pawn.getStatusEffects().get(0).doEffect()) {
                trues++;
            }
            if (pawn.getStatusEffects().get(1).doEffect()) {
                trues++;
            }
        }

        System.out.println(trues);
    }

    @Test
    void debugModifiers() {
        var   card= AbilityCard.ENFEEBLED_HEART_GOLD;
        Pawn pawn = new Pawn(PawnIndex.PAWN1, PawnCard.OKRA_WARRIOR, TalismanCard.BALANCE_BEAD, null, null, List.of(),
                List.of(), List.of());
        System.out.println(pawn.getStatsMap());
        pawn.addStatusEffect(card.getStats().getTargetEffects()[0]);
        System.out.println(pawn.getStatsMap());
    }

    @Test
    void cardStepThrough() {
        var room = States.getReadiedGameRoom();
        Settings.GET().advancedDebug = true;
        for (int i = 0; i < 10; ++ i) {
            WeaponCard card = WeaponCard.SHADOW_DAGGER;
            ActionReturn aReturn = card.playCard(room.getPlayer1(), room.getPlayer2(), PawnIndex.PAWN1, PawnIndex.PAWN2, card.getStats());
            System.out.println("player damaged Pawns");

            aReturn.playerPawnStates.stream().filter(PawnInterimState::hasDamage).forEach(p -> {
                System.out.println(p.getDamageMap());
                System.out.println(p.getPawnIndex());
                System.out.println(p.getActionFlags());
            });

            room.getPlayer1().getCombatManager().doAction(aReturn);
        }
    }

    @Test
    void confusionTest() {
        var room = States.getReadiedGameRoom();
        Settings.GET().advancedDebug = true;
        var player1 = room.getPlayer1();
        var player2 = room.getPlayer2();
        player1.getPawn(PawnIndex.PAWN1).setAbilityCard1(AbilityCard.ABILITY_PLUNDER);
        player1.getPawn(PawnIndex.PAWN1).updateStat(SP, 10000, true);
        player1.getPawn(PawnIndex.PAWN1).updateStat(MP, 10000, true);

        for (int i = 0; i < 5; ++ i) {
            ActiveTurnState turnState = new ActiveTurnState(player1, player2,1);
            var ar = AbilityCard.ABILITY_PLUNDER.playCard(player1,player2, PawnIndex.PAWN1, PawnIndex.PAWN2, AbilityCard.ABILITY_PLUNDER.getStats());
            var ar2 = ActionCard.SWIFT_SHOT.playCard(player1,player2, PawnIndex.PAWN1, PawnIndex.PAWN2, ActionCard.SWIFT_SHOT.getStats());
            System.out.println("ar");
            System.out.println(ar.isInvalid);
            System.out.println("ar2");
            System.out.println(ar2.isInvalid);
            System.out.println(player1.getPawn(PawnIndex.PAWN1).getStatsMap());
            System.out.println(ar2.invalidMsg);
           // turnState.doAction(new NetGameAction(PlayerAction.ABILITY_CARD_1, PawnIndex.PAWN1, PawnIndex.PAWN2));
        }
    }

    @Test
    void configTest() {
        System.out.println(Settings.GET().projectMessages);
    }
}
