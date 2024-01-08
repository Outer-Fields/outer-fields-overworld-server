package io.mindspce.outerfieldsserver.core.systems;

import io.mindspce.outerfieldsserver.components.Component;
import io.mindspce.outerfieldsserver.components.ComponentSystem;
import io.mindspce.outerfieldsserver.core.networking.SocketService;
import io.mindspce.outerfieldsserver.entities.player.PlayerEntity;
import io.mindspce.outerfieldsserver.enums.ComponentType;
import io.mindspce.outerfieldsserver.enums.SystemType;
import io.mindspce.outerfieldsserver.systems.event.SystemListener;

import java.util.Objects;


public class PlayerStateSystem extends SystemListener {

    public PlayerStateSystem(SocketService socketService) {
        super(SystemType.PLAYER, true);
    }

    public void registerEntity(PlayerEntity playerEntity) {
        playerEntity.registerComponents(this);

        var playerSystem = playerEntity.getComponent(ComponentType.SUB_SYSTEM).stream()
                .filter(c -> Objects.equals(c.componentName(), "PlayerNetworkController")).findFirst()
                .orElseThrow(() -> new IllegalStateException("Failed to detect PlayerNetworkController on added entity"));

        Component<ComponentSystem> castedPlayerSystem = ComponentType.SUB_SYSTEM.castOrNull(playerSystem);

        if (castedPlayerSystem == null) {
            throw new IllegalStateException("Casting exception on player SubSystem");
        }
    }
}


