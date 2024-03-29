package io.mindspce.outerfieldsserver.enums;

import io.mindspce.outerfieldsserver.components.ai.DecisionTree;
import io.mindspce.outerfieldsserver.components.ai.ThoughtModule;
import io.mindspce.outerfieldsserver.components.*;
import io.mindspce.outerfieldsserver.components.monitors.AreaMonitor;
import io.mindspce.outerfieldsserver.components.npc.TravelController;
import io.mindspce.outerfieldsserver.components.primatives.ComponentSystem;
import io.mindspce.outerfieldsserver.components.primatives.SimpleEmitter;
import io.mindspce.outerfieldsserver.components.primatives.SimpleListener;
import io.mindspce.outerfieldsserver.components.quest.QuestModule;
import io.mindspce.outerfieldsserver.components.serialization.CharacterSerializer;
import io.mindspce.outerfieldsserver.components.serialization.NetSerializer;
import io.mindspce.outerfieldsserver.components.world.*;
import io.mindspce.outerfieldsserver.components.entity.EntityProperties;
import io.mindspce.outerfieldsserver.components.entity.EntityStateComp;
import io.mindspce.outerfieldsserver.components.entity.GlobalPosition;
import io.mindspce.outerfieldsserver.components.player.*;
import jakarta.annotation.Nullable;

import java.util.Objects;
import java.util.function.Predicate;


public enum ComponentType {
    ANY(Object.class, Objects::nonNull),
    AREA_MONITOR(AreaMonitor.class, x -> x instanceof AreaMonitor),
    SUB_SYSTEM(ComponentSystem.class, x -> x instanceof ComponentSystem),
    VIEW_RECT(ViewRect.class, x -> x instanceof ViewRect),
    LOCAL_TILE_GRID(LocalTileGrid.class, x -> x instanceof LocalTileGrid),
    NET_PLAYER_POSITION(PlayerMovement.class, x -> x instanceof PlayerMovement),
    GLOBAL_POSITION(GlobalPosition.class, x -> x instanceof GlobalPosition),
    SIMPLER_LISTENER(SimpleListener.class, x -> x instanceof SimpleListener),
    SIMPLE_EMITTER(SimpleEmitter.class, x -> x instanceof SimpleEmitter),
    SIMPLE_OBJECT(SimpleObject.class, x -> x instanceof SimpleObject<?>),
    CHUNK_MAP(ChunkMap.class, x -> x instanceof ChunkMap),
    ACTIVE_ENTITIES(EntityGrid.class, x -> x instanceof EntityGrid),
    COLLISION_GRID(CollisionGrid.class, x -> x instanceof CollisionGrid),
    KNOWN_ENTITIES(KnownEntities.class, x -> x instanceof KnownEntities),
    PLAYER_ENTITY_UPDATE_DATA(PlayerEntityUpdateData.class, x -> x instanceof PlayerEntityUpdateData),
    PLAYER_SESSION(PlayerSession.class, x -> x instanceof PlayerSession),
    PLAYER_NET_OUT(PlayerNetOut.class, x -> x instanceof PlayerNetOut),
    ENTITY_STATE(EntityStateComp.class, x -> x instanceof EntityStateComp),
    OUTFIT(CharacterOutfit.class, x -> x instanceof CharacterOutfit),
    EntityProperties(EntityProperties.class, x -> x instanceof EntityProperties),
    NET_SERIALIZER(NetSerializer.class, x -> x instanceof NetSerializer),
    LOCATION_ENTITIES(LocationEntities.class, x -> x instanceof LocationEntities),
    AREA_ENTITIES(AreaEntities.class, x -> x instanceof AreaEntities),
    THOUGHT_MODULE(ThoughtModule.class, x -> x instanceof ThoughtModule<?, ?>),
    DECISION_TREE(DecisionTree.class, x -> x instanceof DecisionTree),
    CHARACTER_SERIALIZER(CharacterSerializer.class, x -> x instanceof CharacterSerializer),
    TRAVEL_CONTROLLER(TravelController.class, x -> x instanceof TravelController),
    QUEST_MODULE(QuestModule.class, x -> x instanceof QuestModule);

    public final Class<?> componentClass;
    private final Predicate<Object> validator;

    ComponentType(Class<?> componentClass, Predicate<Object> validator) {
        this.componentClass = componentClass;
        this.validator = validator;
    }

    public boolean validate(Object dataObj) {
        return validator.test(dataObj);
    }

    @Nullable
    public <T extends Component<?>> T castOrNull(Component<?> component) {
        if (validate(component)) {
            @SuppressWarnings("unchecked")
            T casted = (T) component;
            return casted;
        }
        return null;
    }


}
