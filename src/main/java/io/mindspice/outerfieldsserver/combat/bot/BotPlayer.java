package io.mindspice.outerfieldsserver.combat.bot;

import io.mindspice.outerfieldsserver.entities.PlayerEntity;

import java.util.concurrent.ThreadLocalRandom;


public class BotPlayer extends PlayerEntity {
    public BotPlayer() {
        super(
                ThreadLocalRandom.current().nextInt(Integer.MIN_VALUE, -1),
                ThreadLocalRandom.current().nextInt(Integer.MIN_VALUE, -1),
                "Botplayer",
                null, null, null, null, null);

    }

    @Override
    public void sendJson(Object obj) {
        // Not used for the bot player
    }

    @Override
    public boolean isConnected() {
        return true;
    }
}
