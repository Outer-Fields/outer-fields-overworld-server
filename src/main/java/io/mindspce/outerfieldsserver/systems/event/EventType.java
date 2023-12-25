package io.mindspce.outerfieldsserver.systems.event;

public enum EventType {
    CHARACTER_POSITION(EventDomain.GLOBAL),
    PLAYER_POSITION(EventDomain.GLOBAL),
    PLAYER_CONNECTED(EventDomain.PLAYER),
    AREA_ENTERED(EventDomain.WORLD);

    public final EventDomain domain;

    EventType(EventDomain domain) { this.domain = domain; }
    }


