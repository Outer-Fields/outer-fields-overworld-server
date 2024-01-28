package io.mindspice.outerfieldsserver.combat;

import io.mindspice.outerfieldsserver.combat.cards.PawnCard;
import io.mindspice.outerfieldsserver.combat.cards.TalismanCard;
import io.mindspice.outerfieldsserver.combat.cards.WeaponCard;
import io.mindspice.outerfieldsserver.combat.enums.PawnIndex;
import io.mindspice.outerfieldsserver.combat.gameroom.pawn.Pawn;
import org.junit.jupiter.api.Test;

import java.util.List;


public class RegenTest {



    @Test
    void regenTest(){
        Pawn pawn = new Pawn(PawnIndex.PAWN1, PawnCard.DARK_SENTINEL, TalismanCard.BALANCE_BEAD,
                WeaponCard.ZWEIHANDER_OF_EXCELLENCE, WeaponCard.ZWEIHANDER_OF_EXCELLENCE, List.of(), List.of(), List.of());

        System.out.println(pawn.getStatsMap());
        pawn.regenerate();
        System.out.println(pawn.getStatsMap());
    }
}
