package io.mindspice.outerfieldsserver.combat.schema.websocket.incoming;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.mindspice.outerfieldsserver.combat.enums.PawnIndex;
import io.mindspice.outerfieldsserver.combat.enums.PlayerAction;


public record NetGameAction(
        PlayerAction action,
        @JsonProperty("player_pawn") PawnIndex playerPawn,
        @JsonProperty("target_pawn") PawnIndex targetPawn
        // PotionCard potionCard,
) { }



