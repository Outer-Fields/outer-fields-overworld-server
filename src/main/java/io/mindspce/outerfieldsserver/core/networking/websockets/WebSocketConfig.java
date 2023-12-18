package io.mindspce.outerfieldsserver.core.networking.websockets;

import io.mindspce.outerfieldsserver.entities.player.PlayerState;
import org.jctools.maps.NonBlockingHashMapLong;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;


@Configuration
@EnableWebSocket
//@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketConfigurer {

    private final GameServerSocketHandler gameServerSocketHandler;
    //private final TokenHandshakeInterceptor tokenHandshakeInterceptor;

    public WebSocketConfig(GameServerSocketHandler gameServerSocketHandler) {
        this.gameServerSocketHandler = gameServerSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(gameServerSocketHandler, "/ws")
                .setAllowedOrigins("*");
             //   .addInterceptors(tokenHandshakeInterceptor);
        System.out.println("Started Websockets");
    }

}