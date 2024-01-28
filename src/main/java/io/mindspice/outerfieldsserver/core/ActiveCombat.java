package io.mindspice.outerfieldsserver.core;

import io.mindspice.outerfieldsserver.combat.gameroom.MatchInstance;

import java.time.Instant;
import java.util.concurrent.ScheduledFuture;


public record ActiveCombat(
        MatchInstance matchInstance,
        long initTime,
        boolean isMatch,
        ScheduledFuture<?> process
) {

    public ActiveCombat(MatchInstance matchInstance, boolean isMatch, ScheduledFuture<?> process) {
        this(matchInstance, Instant.now().getEpochSecond(), isMatch, process);
    }

}
