package io.mindspice.outerfieldsserver.combat.gameroom.action;

import io.mindspice.outerfieldsserver.combat.cards.AbilityCard;
import io.mindspice.outerfieldsserver.combat.cards.ActionCard;
import io.mindspice.outerfieldsserver.combat.cards.WeaponCard;
import io.mindspice.outerfieldsserver.combat.enums.InvalidMsg;
import io.mindspice.outerfieldsserver.combat.enums.PawnIndex;
import io.mindspice.outerfieldsserver.combat.gameroom.state.PlayerGameState;


public class ActionFactory {
    private final PlayerGameState player;
    private final PlayerGameState enemy;

    public ActionFactory(PlayerGameState player, PlayerGameState enemy) {
        this.player = player;
        this.enemy = enemy;
    }

    public ActionReturn playActionCard1(PawnIndex playerIndex, PawnIndex targetIndex) {
        ActionCard card = player.getPawn(playerIndex).getActionCard(1);
        if (card == null) {
            return ActionReturn.getInvalid(player.getPawn(playerIndex), InvalidMsg._INVALID_CARD, "NULL");
        }
        var actionReturn = card.playCard(player, enemy, playerIndex, targetIndex);
        if (!actionReturn.isInvalid) { player.getPawn(playerIndex).removeActionCard(1); }
        return actionReturn;
    }

    public ActionReturn playActionCard2(PawnIndex playerIndex, PawnIndex targetIndex) {
        ActionCard card = player.getPawn(playerIndex).getActionCard(2);
        if (card == null) {
            return ActionReturn.getInvalid(player.getPawn(playerIndex), InvalidMsg._INVALID_CARD, "NULL");
        }
        var actionReturn = card.playCard(player, enemy, playerIndex, targetIndex);
        if (!actionReturn.isInvalid) { player.getPawn(playerIndex).removeActionCard(2); }
        return actionReturn;
    }

    public ActionReturn playAbilityCard1(PawnIndex playerIndex, PawnIndex targetIndex) {
        AbilityCard card = player.getPawn(playerIndex).getAbilityCard(1);
        if (card == null) {
            return ActionReturn.getInvalid(player.getPawn(playerIndex), InvalidMsg._INVALID_CARD, "NULL");
        }
        var actionReturn = card.playCard(player, enemy, playerIndex, targetIndex);
        if (!actionReturn.isInvalid) { player.getPawn(playerIndex).removeAbilityCard(1); }
        return actionReturn;
    }

    public ActionReturn playAbilityCard2(PawnIndex playerIndex, PawnIndex targetIndex) {
        AbilityCard card = player.getPawn(playerIndex).getAbilityCard(2);
        if (card == null) {
            return ActionReturn.getInvalid(player.getPawn(playerIndex), InvalidMsg._INVALID_CARD, "NULL");
        }
        var actionReturn = card.playCard(player, enemy, playerIndex, targetIndex);
        if (!actionReturn.isInvalid) { player.getPawn(playerIndex).removeAbilityCard(2); }
        return actionReturn;
    }

    public ActionReturn attackWeapon1(PawnIndex playerIndex, PawnIndex targetIndex) {
        WeaponCard card = player.getPawn(playerIndex).getWeaponCard(1);
        if (card == null) {
            return ActionReturn.getInvalid(player.getPawn(playerIndex), InvalidMsg._INVALID_CARD, "NULL");
        }
        return card.playCard(player, enemy, playerIndex, targetIndex);
    }

    public ActionReturn attackWeapon2(PawnIndex playerIndex, PawnIndex targetIndex) {
        WeaponCard card = player.getPawn(playerIndex).getWeaponCard(2);
        if (card == null) {
            return ActionReturn.getInvalid(player.getPawn(playerIndex), InvalidMsg._INVALID_CARD, "NULL");
        }
        return card.playCard(player, enemy, playerIndex, targetIndex);
    }

//    public ActionReturn consumePotion(PawnIndex targetIndex, PotionCard potion) {
//        var actionReturn = potion.consumePotion(player, targetIndex);
//        actionReturn.actionString = getActionInfo(PlayerAction.POTION, potion.name(), targetIndex, targetIndex);
//        return actionReturn;
//    }


}













































