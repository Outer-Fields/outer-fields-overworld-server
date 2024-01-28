package io.mindspice.outerfieldsserver.combat.bot.data;

import io.mindspice.outerfieldsserver.combat.enums.StatType;

import java.util.Map;


public record AttackPotential(
        int hpDamage,
        int otherDamage,
        Map<StatType, Integer> costThreshold
) { }
