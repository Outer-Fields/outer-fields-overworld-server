package io.mindspice.outerfieldsserver.combat.gameroom.action;

import io.mindspice.outerfieldsserver.combat.cards.PowerCard;
import io.mindspice.outerfieldsserver.combat.enums.ActionType;
import io.mindspice.outerfieldsserver.combat.enums.EffectType;
import io.mindspice.outerfieldsserver.combat.enums.PowerEnums;
import io.mindspice.outerfieldsserver.combat.gameroom.gameutil.LuckModifier;
import io.mindspice.outerfieldsserver.util.gamelogger.PowerRecord;

import java.util.EnumMap;

import static io.mindspice.outerfieldsserver.combat.enums.PowerEnums.PowerReturn.FALSE;


public class ActivePower {
    private final PowerEnums.PowerType type;
    private final PowerCard card;
    private final double chance;
    private final double scalar;

    public ActivePower(PowerCard card, PowerEnums.PowerType type, double chance, double scalar) {
        this.card = card;
        this.type = type;
        this.chance = chance;
        this.scalar = scalar;
    }

    public PowerRecord getPowerRecord() {
        return new PowerRecord(
                type,
                chance,
                scalar
        );
    }

    public EnumMap<PowerEnums.PowerReturn, Double> getActionDefense(ActionType actionType, int luck) {
        EnumMap<PowerEnums.PowerReturn, Double> returnMap = new EnumMap<>(PowerEnums.PowerReturn.class);
        var rtn = type.getDamageDefense(actionType);
        if (rtn == FALSE) {
            return returnMap;
        }

        if (LuckModifier.chanceCalc(chance, luck)) {
            returnMap.put(rtn, scalar);
        }
        return returnMap;
    }

    public EnumMap<PowerEnums.PowerReturn, Double> getActionOffense(ActionType actionType, int luck) {
        EnumMap<PowerEnums.PowerReturn, Double> returnMap = new EnumMap<>(PowerEnums.PowerReturn.class);
        var rtn = type.getOffense(actionType);
        if (rtn == FALSE) {
            return returnMap;
        }
        if (LuckModifier.chanceCalc(chance, luck)) {
            returnMap.put(rtn, scalar);
        }
        return returnMap;
    }

    public EnumMap<PowerEnums.PowerReturn, Double> getEffectDefense(EffectType effectType, int luck) {
        EnumMap<PowerEnums.PowerReturn, Double> returnMap = new EnumMap<>(PowerEnums.PowerReturn.class);
        var rtn = type.getEffectDefense(effectType);
        if (rtn == FALSE) {
            return returnMap;
        }
        if (LuckModifier.chanceCalc(chance, luck)) {
            returnMap.put(rtn, scalar);
        }

        return returnMap;
    }

    public double getLuckMod() {
        if (this.type != PowerEnums.PowerType.INCREASE_LUCK) { return 0; }
        return scalar;
    }

    public PowerEnums.PowerType getType() {
        return type;
    }

    public PowerCard getCard() {
        return card;
    }
}



