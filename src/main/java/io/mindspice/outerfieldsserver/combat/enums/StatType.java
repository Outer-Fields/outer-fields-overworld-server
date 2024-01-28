package io.mindspice.outerfieldsserver.combat.enums;

import java.util.List;


public enum StatType {
    HP,
    DP,
    SP,
    MP,
    LP,
    WP;

    public static List<StatType> costStats() { return List.of(HP, SP, MP, DP); }
}
