package io.mindspice.outerfieldsserver.enums;

import io.mindspice.outerfieldsserver.components.Component;
import io.mindspice.outerfieldsserver.components.environment.FarmPlot;
import io.mindspice.outerfieldsserver.components.items.ContainedItems;
import io.mindspice.outerfieldsserver.components.player.PlayerItemsAndFunds;
import io.mindspice.outerfieldsserver.components.item.LootDrop;
import io.mindspice.outerfieldsserver.components.ai.DecisionTree;
import io.mindspice.outerfieldsserver.components.ai.ThoughtModule;
import io.mindspice.outerfieldsserver.components.monitors.AreaMonitor;
import io.mindspice.outerfieldsserver.components.npc.NPCMovement;
import io.mindspice.outerfieldsserver.components.npc.CharSpawnController;
import io.mindspice.outerfieldsserver.components.player.*;
import io.mindspice.outerfieldsserver.components.primatives.ComponentSystem;
import io.mindspice.outerfieldsserver.components.primatives.SimpleEmitter;
import io.mindspice.outerfieldsserver.components.primatives.SimpleListener;
import io.mindspice.outerfieldsserver.components.primatives.SimpleObject;
import io.mindspice.outerfieldsserver.components.quest.QuestModule;
import io.mindspice.outerfieldsserver.components.serialization.CharacterSerializer;
import io.mindspice.outerfieldsserver.components.serialization.Visibility;
import io.mindspice.outerfieldsserver.components.world.*;
import io.mindspice.outerfieldsserver.components.entity.EntityProperties;
import io.mindspice.outerfieldsserver.components.entity.EntityStateComp;
import io.mindspice.outerfieldsserver.components.entity.GlobalPosition;
import jakarta.annotation.Nullable;

import java.util.Objects;
import java.util.function.Predicate;


public enum ComponentType {
    ANY(Object.class, Objects::nonNull),
    AREA_MONITOR(AreaMonitor.class, x -> x instanceof AreaMonitor),
    SUB_SYSTEM(ComponentSystem.class, x -> x instanceof ComponentSystem),
    VIEW_RECT(ViewRect.class, x -> x instanceof ViewRect),
    LOCAL_TILE_GRID(LocalTileGrid.class, x -> x instanceof LocalTileGrid),
    NET_PLAYER_POSITION(NetPlayerPosition.class, x -> x instanceof NetPlayerPosition),
    GLOBAL_POSITION(GlobalPosition.class, x -> x instanceof GlobalPosition),
    SIMPLER_LISTENER(SimpleListener.class, x -> x instanceof SimpleListener),
    SIMPLE_EMITTER(SimpleEmitter.class, x -> x instanceof SimpleEmitter),
    SIMPLE_OBJECT(SimpleObject.class, x -> x instanceof SimpleObject<?>),
    CHUNK_MAP(ChunkMap.class, x -> x instanceof ChunkMap),
    ACTIVE_ENTITIES(ActiveEntities.class, x -> x instanceof ActiveEntities),
    COLLISION_GRID(CollisionGrid.class, x -> x instanceof CollisionGrid),
    KNOWN_ENTITIES(KnownEntities.class, x -> x instanceof KnownEntities),
    PLAYER_ENTITY_UPDATE_DATA(PlayerEntityUpdateData.class, x -> x instanceof PlayerEntityUpdateData),
    PLAYER_SESSION(PlayerSession.class, x -> x instanceof PlayerSession),
    PLAYER_NET_OUT(PlayerNetOut.class, x -> x instanceof PlayerNetOut),
    ENTITY_STATE(EntityStateComp.class, x -> x instanceof EntityStateComp),
    OUTFIT(CharacterOutfit.class, x -> x instanceof CharacterOutfit),
    EntityProperties(EntityProperties.class, x -> x instanceof EntityProperties),
    LOCATION_ENTITIES(LocationEntities.class, x -> x instanceof LocationEntities),
    AREA_ENTITIES(AreaEntities.class, x -> x instanceof AreaEntities),
    THOUGHT_MODULE(ThoughtModule.class, x -> x instanceof ThoughtModule<?>),
    DECISION_TREE(DecisionTree.class, x -> x instanceof DecisionTree),
    CHARACTER_SERIALIZER(CharacterSerializer.class, x -> x instanceof CharacterSerializer),
    NPC_MOVEMENT(NPCMovement.class, x -> x instanceof NPCMovement),
    QUEST_MODULE(QuestModule.class, x -> x instanceof QuestModule),
    PLAYER_ACTIONS(NetPlayerAction.class, x -> x instanceof NetPlayerAction),
    SPAWN_CONTROLLER(CharSpawnController.class, x -> x instanceof CharSpawnController),
    LOOT_DROP(LootDrop.class, x -> x instanceof LootDrop),
    PLAYER_ITEMS_AND_FUNDS(PlayerItemsAndFunds.class, x -> x instanceof PlayerItemsAndFunds),
    CONTAINED_ITEMS(ContainedItems.class, x -> x instanceof ContainedItems),
    VISIBILITY(Visibility.class, x -> x instanceof Visibility),
    ENTITY_GRID(EntityGrid.class, x -> x instanceof EntityGrid),
    FARM_PLOT(FarmPlot.class, x -> x instanceof FarmPlot);

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
