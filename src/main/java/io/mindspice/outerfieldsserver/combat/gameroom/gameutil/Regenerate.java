package io.mindspice.outerfieldsserver.combat.gameroom.gameutil;

import io.mindspice.outerfieldsserver.combat.enums.StatType;

import java.util.EnumMap;

public interface Regenerate {
        EnumMap<StatType, Integer> regeneration(EnumMap<StatType, Integer> stats);
}
