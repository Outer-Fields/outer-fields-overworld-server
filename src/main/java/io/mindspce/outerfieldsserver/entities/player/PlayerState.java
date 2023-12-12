package io.mindspce.outerfieldsserver.entities.player;

import org.springframework.web.socket.WebSocketSession;


public class PlayerState {
    private WebSocketSession session;
    private PlayerCharacter character;

    public WebSocketSession getSession() {
        return session;
    }

    public void setSession(WebSocketSession session) {
        this.session = session;
    }

    public PlayerCharacter getCharacter() {
        return character;
    }

    public void setCharacter(PlayerCharacter character) {
        this.character = character;
    }
}
