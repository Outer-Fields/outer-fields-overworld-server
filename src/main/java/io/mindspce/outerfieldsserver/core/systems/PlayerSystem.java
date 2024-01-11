package io.mindspce.outerfieldsserver.core.systems;

import io.mindspce.outerfieldsserver.components.Component;
import io.mindspce.outerfieldsserver.components.primatives.ComponentSystem;
import io.mindspce.outerfieldsserver.core.networking.SocketService;
import io.mindspce.outerfieldsserver.core.singletons.EntityManager;
import io.mindspce.outerfieldsserver.entities.player.PlayerEntity;
import io.mindspce.outerfieldsserver.enums.ComponentType;
import io.mindspce.outerfieldsserver.enums.SystemType;
import io.mindspce.outerfieldsserver.systems.event.SystemListener;

import java.util.Objects;


public class PlayerSystem extends SystemListener {

    public PlayerSystem() {
        super(SystemType.PLAYER, true);
        EntityManager.GET().registerSystem(this);
    }

    public void registerEntity(PlayerEntity playerEntity) {
        playerEntity.registerComponents(this);

        var playerSystem = playerEntity.getComponent(ComponentType.SUB_SYSTEM).stream()
                .filter(c -> Objects.equals(c.componentName(), "PlayerNetworkController")).findFirst()
                .orElseThrow(() -> new IllegalStateException("Failed to detect PlayerNetworkController on added entity"));
    }
}


