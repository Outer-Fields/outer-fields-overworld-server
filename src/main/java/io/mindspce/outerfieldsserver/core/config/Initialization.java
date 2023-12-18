package io.mindspce.outerfieldsserver.core.config;

import io.mindspce.outerfieldsserver.core.GameServer;
import io.mindspce.outerfieldsserver.core.WorldState;
import io.mindspce.outerfieldsserver.core.networking.SocketInQueue;
import io.mindspce.outerfieldsserver.entities.player.PlayerSession;
import io.mindspce.outerfieldsserver.entities.player.PlayerState;
import org.jctools.maps.NonBlockingHashMapLong;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class Initialization {

    @Bean
    WorldState worldStateInstance() {
        return new WorldState(new HashMap<>());
    }

    @Bean
    NonBlockingHashMapLong<PlayerState> playerTableInstance() {
        return new NonBlockingHashMapLong<>(50);
    }

    @Bean
    GameServer gameServerInstance(
            @Qualifier("worldStateInstance") WorldState worldState,
            @Qualifier("playerTableInstance") NonBlockingHashMapLong<PlayerState> playerTable
    ) {
        return new GameServer(worldState, playerTable);
    }

    @Bean
    SocketInQueue socketInQueue(
            @Qualifier("playerTableInstance")  NonBlockingHashMapLong<PlayerState> playerTable
    ) {
        return new SocketInQueue(playerTable);
    }
}
