package io.mindspce.outerfieldsserver.core.config;

import io.mindspce.outerfieldsserver.core.GameServer;
import io.mindspce.outerfieldsserver.core.WorldState;
import io.mindspce.outerfieldsserver.core.networking.SocketQueue;
import io.mindspce.outerfieldsserver.entities.player.PlayerState;
import org.jctools.maps.NonBlockingHashMapLong;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;


@Configuration
public class Initialization {



    @Bean
    NonBlockingHashMapLong<PlayerState> playerTableInstance() {
        return new NonBlockingHashMapLong<>(50);
    }

    @Bean
    GameServer gameServerInstance(
            @Qualifier("playerTableInstance") NonBlockingHashMapLong<PlayerState> playerTable
    ) {
        return new GameServer( playerTable);
    }

    @Bean
    SocketQueue socketInQueue(
            @Qualifier("playerTableInstance")  NonBlockingHashMapLong<PlayerState> playerTable
    ) {
        return new SocketQueue(playerTable);
    }
}
