package io.mindspice.outerfieldsserver.combat.schema.websocket.outgoing.lobby;

import io.mindspice.outerfieldsserver.combat.cards.PotionCard;
import io.mindspice.outerfieldsserver.combat.schema.websocket.outgoing.OutMsgType;

import java.util.HashMap;

public class NetPotionUpdate {
    public final OutMsgType msg_type = OutMsgType.POTIONS_UPDATE;
    public HashMap<PotionCard, Integer> owned_potions;
}
