package io.mindspice.outerfieldsserver.combat.schema.rest;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;


public record LeaderBoard(
        @JsonProperty("daily_scores") List<PlayerScore> dailyScores,
        @JsonProperty("weekly_scores") List<PlayerScore> weeklyScores,
        @JsonProperty("monthly_scores") List<PlayerScore> monthlyScores
) { }
