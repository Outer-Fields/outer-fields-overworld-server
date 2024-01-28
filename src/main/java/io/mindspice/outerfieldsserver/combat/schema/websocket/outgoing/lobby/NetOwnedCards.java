package io.mindspice.outerfieldsserver.combat.schema.websocket.outgoing.lobby;

import io.mindspice.outerfieldsserver.combat.enums.CardDomain;
import io.mindspice.outerfieldsserver.combat.schema.websocket.outgoing.OutMsgType;

import java.util.List;
import java.util.Map;


public record NetOwnedCards (
    OutMsgType msg_type,
    Map<CardDomain, List<String>> owned_cards
    ) {
    public NetOwnedCards(Map<CardDomain, List<String>> owned_cards) {
        this(OutMsgType.OWNED_CARDS, owned_cards);
    }
}
