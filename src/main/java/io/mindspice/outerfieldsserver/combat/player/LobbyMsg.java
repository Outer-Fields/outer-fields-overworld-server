package io.mindspice.outerfieldsserver.combat.player;

import io.mindspice.outerfieldsserver.entities.PlayerEntity;


public record LobbyMsg(PlayerEntity player, String netLobbyMsg){}

