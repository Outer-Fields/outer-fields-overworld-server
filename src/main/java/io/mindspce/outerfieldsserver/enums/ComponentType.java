package io.mindspce.outerfieldsserver.enums;

import io.mindspce.outerfieldsserver.components.*;
import io.mindspce.outerfieldsserver.components.ChunkMap;
import jakarta.annotation.Nullable;


public enum ComponentType {
    AREA_MONITOR(AreaMonitor.class),
    CONTROLLER(SubSystem.class),
    VIEW_RECT(ViewRect.class),
    LOCAL_TILE_GRID(LocalTileGrid.class),
    PLAYER_MOVEMENT(PlayerMovement.class),
    GLOBAL_POSITION(GlobalPosition.class),
    SIMPLER_LISTENER(SimpleListener.class),
    SIMPLE_EMITTER(SimpleEmitter.class),
    SIMPLE_OBJECT(SimpleObject.class),
    CHUNK_MAP(ChunkMap.class),
    ACTIVE_ENTITIES((ActiveEntitiesGrid.class)),
    COLLISION_GRID(CollisionGrid.class);

    private final Class<?> componentClass;

    ComponentType(Class<?> componentClass) {
        this.componentClass = componentClass;
    }

    @Nullable
    public <T extends Component<?>> T castComponent(Component<?> component) {
        if (componentClass.isInstance(component)) {
            @SuppressWarnings("unchecked") // Safe cast after isInstance check
            T castedComponent = (T) component;
            return castedComponent;
        }
        return null;
    }

}
