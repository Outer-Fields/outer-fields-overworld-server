package io.mindspice.outerfieldsserver.combat.gameroom.effect;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.mindspice.outerfieldsserver.combat.cards.AbilityCard;
import io.mindspice.outerfieldsserver.combat.cards.ActionCard;
import io.mindspice.outerfieldsserver.combat.cards.PowerCard;
import io.mindspice.outerfieldsserver.combat.enums.EffectType;
import io.mindspice.outerfieldsserver.combat.enums.PawnIndex;
import io.mindspice.outerfieldsserver.combat.schema.websocket.outgoing.game.CardHand;
import io.mindspice.outerfieldsserver.combat.schema.websocket.outgoing.game.EffectStats;

import java.util.List;


public class Insight {
    @JsonProperty("pawn_index") public PawnIndex pawnIndex;
    @JsonProperty("insight_type") public EffectType insightType;
    @JsonProperty("card_hand") public CardHand cardHand;
    @JsonProperty("effects") public List<EffectStats> effects;
    @JsonProperty("action_deck")public List<ActionCard> actionDeck;
    @JsonProperty("ability_deck")public List<AbilityCard> abilityDeck;
    @JsonProperty("power_deck")public List<PowerCard> powerDeck;
}
