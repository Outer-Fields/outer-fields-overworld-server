package io.mindspice.outerfieldsserver.combat;

import io.mindspice.outerfieldsserver.combat.cards.AbilityCard;
import io.mindspice.outerfieldsserver.combat.cards.ActionCard;
import io.mindspice.outerfieldsserver.combat.cards.PowerCard;
import io.mindspice.outerfieldsserver.combat.schema.PawnSet;
import org.junit.jupiter.api.Test;


public class PawnSetTest {


    @Test
    void testLimits() {
        for(int i =0; i < 2000; ++i) {
            PawnSet ps = PawnSet.getRandomPawnSet2();
            for(var lo : ps.pawnLoadouts()) {
                System.out.println(lo.actionDeck().stream().mapToInt(ActionCard::getLevel).summaryStatistics().getAverage());
                System.out.println(lo.abilityDeck().stream().mapToInt(AbilityCard::getLevel).summaryStatistics().getAverage());
                System.out.println(lo.powerDeck().stream().mapToInt(PowerCard::getLevel).summaryStatistics().getAverage());
            }
        }
    }
}
