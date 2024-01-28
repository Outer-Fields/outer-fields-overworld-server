package io.mindspice.outerfieldsserver.combat.bot.state;

import io.mindspice.outerfieldsserver.combat.enums.EffectType;
import io.mindspice.outerfieldsserver.combat.enums.PawnIndex;
import io.mindspice.outerfieldsserver.combat.enums.PlayerAction;

import java.util.ArrayList;
import java.util.List;


public class TreeFocusState {
    public PawnIndex focusPawn;
    public EffectType effect;
    public PlayerAction playerAction;
    public List<String> decisions = new ArrayList<>();
    public String action;
}
