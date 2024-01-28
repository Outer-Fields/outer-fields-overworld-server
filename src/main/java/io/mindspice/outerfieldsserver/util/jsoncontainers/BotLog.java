package io.mindspice.outerfieldsserver.util.jsoncontainers;

import io.mindspice.outerfieldsserver.combat.cards.AbilityCard;
import io.mindspice.outerfieldsserver.combat.cards.ActionCard;
import io.mindspice.outerfieldsserver.combat.cards.PowerCard;
import io.mindspice.outerfieldsserver.combat.enums.EffectType;
import io.mindspice.outerfieldsserver.combat.enums.PawnIndex;
import io.mindspice.outerfieldsserver.combat.enums.PowerEnums;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class BotLog {
    public static class Log {
        public List<String> exceptions;
        public final int botId;
        public final List<TurnStartStats> turnStartStats = new ArrayList<>();
        public final List<TurnActionStats> turnActionStats = new ArrayList<>();

        public Log(int botId) {
            this.botId = botId;
        }
    }

    public static class TurnStartStats {
        public int round;
        public List<PawnData> playerPawns = new ArrayList<>();
        public List<PawnData> enemyPawns = new ArrayList<>();

    }

    public static class TurnActionStats {
        public int round;
        public List<String> decisions = new ArrayList<>();
        public String action;
        public PawnData playerPawn;
        public PawnData enemyPawn;
    }

    public static class PawnData {
        public PawnIndex index;
        public HashMap<String, Integer> stats = new HashMap<String, Integer>();
        public List<EffectType> activeEffects = new ArrayList<>();
        public List<PowerEnums.PowerType> activePowers = new ArrayList<>();
        public ActionCard actionCard1;
        public ActionCard actionCard2;
        public AbilityCard abilityCard1;
        public AbilityCard abilityCard2;
        public PowerCard powerCard;
    }
}
