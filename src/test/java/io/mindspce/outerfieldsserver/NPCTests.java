package io.mindspce.outerfieldsserver;

import io.mindspce.outerfieldsserver.ai.decisiongraph.DecisionEventGraph;
import io.mindspce.outerfieldsserver.ai.decisiongraph.RootNode;
import io.mindspce.outerfieldsserver.ai.decisiongraph.actions.ActionEvent;
import io.mindspce.outerfieldsserver.ai.decisiongraph.decisions.PredicateNode;
import io.mindspce.outerfieldsserver.ai.task.Task;

import io.mindspce.outerfieldsserver.area.AreaEntity;
import io.mindspce.outerfieldsserver.area.ChunkEntity;
import io.mindspce.outerfieldsserver.area.ChunkJson;
import io.mindspce.outerfieldsserver.area.TileData;
import io.mindspce.outerfieldsserver.components.Component;
import io.mindspce.outerfieldsserver.components.ai.ThoughtModule;
import io.mindspce.outerfieldsserver.components.npc.TravelController;
import io.mindspce.outerfieldsserver.core.Tick;
import io.mindspce.outerfieldsserver.core.singletons.EntityManager;
import io.mindspce.outerfieldsserver.core.systems.WorldSystem;
import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.enums.*;
import io.mindspce.outerfieldsserver.systems.EventData;
import io.mindspce.outerfieldsserver.systems.event.Event;
import io.mindspce.outerfieldsserver.systems.event.EventType;
import io.mindspce.outerfieldsserver.systems.event.SystemListener;
import io.mindspce.outerfieldsserver.util.GridUtils;
import io.mindspice.mindlib.data.geometry.IRect2;
import io.mindspice.mindlib.data.geometry.IVector2;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


public class NPCTests {

    public static class TestComponent extends Component<EventSystemTests.TestComponent> {
        int testInt;
        List<Integer> testList = new CopyOnWriteArrayList<>();

        public TestComponent(Entity parentEntity,
                ComponentType componentType,
                List<EventType> emittedEvents) {
            super(parentEntity, componentType, emittedEvents);
        }
    }


    public static class TestSystem extends SystemListener {
        public TestSystem(SystemType systemType, boolean doStart) {
            super(systemType, doStart);
            EntityManager.GET().registerSystem(this);
        }
    }

    @Bean
    public WorldSystem worldSystem() throws IOException {

        ChunkJson chunkJson = GridUtils.parseChunkJson(new File(
                "/home/mindspice/code/Java/Okra/outer-fields-overworld-server/src/main/resources/chunkdata/chunk_0_0.json")
        );

        AreaEntity area = EntityManager.GET().newAreaEntity(
                AreaId.TEST,
                IRect2.of(0, 0, 1920, 1920),
                IVector2.of(1920, 1920),
                List.of()
        );

        Map<IVector2, TileData> chunkData = ChunkEntity.loadFromJson(chunkJson);
        ChunkEntity[][] chunkMap = new ChunkEntity[1][1];
        chunkMap[0][0] = EntityManager.GET().newChunkEntity(AreaId.TEST, IVector2.of(0, 0), chunkJson);
        area.setChunkMap(chunkMap);

        area.addCollisionToGrid(chunkJson.collisionPolys());
        //   System.out.println(EntityManager.GET().areaById(AreaId.TEST));

        return new WorldSystem(true, Map.of(AreaId.TEST, area));

    }

    public record BasicFocus(
            Entity entity
    ) { }


    public record BasicTaskData(
            IVector2 location,
            AtomicBoolean bool
    ) { }


    public enum BasicEnum {
        TRAVEL
    }

    public static AtomicInteger testThought1(Entity entity) {
        AtomicInteger testCount = new AtomicInteger(0);
        // @formatter:off
        DecisionEventGraph<BasicFocus, ThoughtType> graph =
                new DecisionEventGraph<BasicFocus, ThoughtType>(new RootNode<>())
                        .addChild(PredicateNode.of("PredicateNode", List.of((BasicFocus bf) -> bf.entity != null)))
                        .addLeaf(ActionEvent.of(ThoughtType.TEST_TRAVEL));
        // @formatter:on

        ThoughtModule<ThoughtType, BasicFocus> thoughtModule = new ThoughtModule<>(entity, graph, new BasicFocus(entity), 100);

        Task<ThoughtType, BasicTaskData> moveInside = Task.builder(ThoughtType.TEST_TRAVEL, new BasicTaskData(IVector2.of(784, 766), new AtomicBoolean(false)), thoughtModule.getEventHooks())
                .addSubEventTask(EventType.NPC_ARRIVED_AT_LOC,
                        (BasicTaskData td, Event<?> event) -> {
                            EventData.NpcLocationArrival data = EventType.NPC_ARRIVED_AT_LOC.castOrNull(td);
                            testCount.getAndIncrement();
                            return true;
                        },
                        (BasicTaskData data) -> {
                            thoughtModule.emitEvent(Event.npcTravelTo(thoughtModule, thoughtModule.areaId(),
                                    new EventData.NPCTravelTo(data.location, -1, -1, 2000, false)
                            ));
                        },
                        false)
                .build();

        Task<ThoughtType, BasicTaskData> moveOutside = Task.builder(ThoughtType.TEST_TRAVEL, new BasicTaskData(IVector2.of(672, 960), new AtomicBoolean(false)), thoughtModule.getEventHooks())
                .addSubEventTask(
                        EventType.NPC_ARRIVED_AT_LOC,
                        (BasicTaskData td, Event<?> event) -> {
                            EventData.NpcLocationArrival data = EventType.NPC_ARRIVED_AT_LOC.castOrNull(td);
                            testCount.getAndIncrement();
                            return true;
                        },
                        (BasicTaskData data) -> {
                            thoughtModule.emitEvent(Event.npcTravelTo(thoughtModule, thoughtModule.areaId(),
                                    new EventData.NPCTravelTo(data.location, -1, -1, 2000, false)
                            ));
                        },
                        false)
                .build();

        thoughtModule.addWantingToDo(
                ThoughtType.TEST_TRAVEL,
                false,
                (Tick t) -> true,
                moveInside
        );

        thoughtModule.addWantingToDo(
                ThoughtType.TEST_TRAVEL,
                false,
                (Tick t) -> true,
                moveOutside
        );

        TravelController travelController = ComponentType.TRAVEL_CONTROLLER.castOrNull(
                entity.getComponent(ComponentType.TRAVEL_CONTROLLER).getFirst()
        );

        if (travelController == null) {
            System.out.println("Null controller");
        } else {
            thoughtModule.registerOutputHook(EventType.NPC_TRAVEL_TO, travelController::onMoveTo, true);
            travelController.registerOutputHook(EventType.NPC_ARRIVED_AT_LOC, thoughtModule::onEvent, false);
        }

        entity.addComponent(thoughtModule);
        return testCount;

    }

    public static BasicTaskData testThought12(Entity entity) {
        AtomicBoolean testCount = new AtomicBoolean(false);
        // @formatter:off
        DecisionEventGraph<BasicFocus, ThoughtType> graph =
                new DecisionEventGraph<BasicFocus, ThoughtType>(new RootNode<>())
                        .addChild(PredicateNode.of("PredicateNode", List.of((BasicFocus bf) -> bf.entity != null)))
                        .addLeaf(ActionEvent.of(ThoughtType.TEST_TRAVEL));
        // @formatter:on

        var btd = new BasicTaskData(IVector2.of(0, 0), new AtomicBoolean(false));
        ThoughtModule<ThoughtType, BasicFocus> thoughtModule = new ThoughtModule<>(entity, graph, new BasicFocus(entity), 100);

        Task<ThoughtType, BasicTaskData> concurrent = Task.builder(ThoughtType.TEST_TRAVEL, btd, thoughtModule.getEventHooks())
                .addSubEventTask(EventType.PING,
                        (BasicTaskData td, Event<?> event) -> {
                            System.out.println("ping in");
                            return true;
                        },
                        null,
                        false)
                .addSubEventTask(EventType.PONG,
                        (BasicTaskData td, Event<?> event) -> {
                            System.out.println("pong in");
                            return true;
                        },
                        null,
                        false)
                .setConcurrent(true)
                .setOnCompletion((td) -> td.bool.set(true))
                .build();

        thoughtModule.addWantingToDo(
                ThoughtType.TEST_TRAVEL,
                false,
                (Tick t) -> true,
                concurrent
        );

        TravelController travelController = ComponentType.TRAVEL_CONTROLLER.castOrNull(
                entity.getComponent(ComponentType.TRAVEL_CONTROLLER).getFirst()
        );

        if (travelController == null) {
            System.out.println("Null controller");
        } else {
            thoughtModule.registerOutputHook(EventType.NPC_TRAVEL_TO, travelController::onMoveTo, true);
            travelController.registerOutputHook(EventType.NPC_ARRIVED_AT_LOC, thoughtModule::onEvent, false);
        }

        entity.addComponent(thoughtModule);
        return btd;

    }

    @Test
    public void npcTest() throws InterruptedException, IOException {
        var ts = new TestSystem(SystemType.NPC, true);
        var ws = worldSystem();

        var npc = EntityManager.GET().newNonPlayerEntity(-1, "Test_NPC", List.of(EntityState.TEST),
                new ClothingItem[6], AreaId.TEST, IVector2.of(672, 960), IVector2.of(200, 200));

        var btd = testThought12(npc);



        var start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < 2000) {
            Thread.sleep(1000);
        }
        var tc = new TestComponent(npc, ComponentType.SIMPLE_OBJECT, List.of());
        EntityManager.GET().emitEvent(Event.builder(EventType.PING, tc).setData(new Object()).build());
        EntityManager.GET().emitEvent(Event.builder(EventType.PONG, tc).setData(new Object()).build());

        while (System.currentTimeMillis() - start < 2000) {
            Thread.sleep(1000);
        }

        ThoughtModule<?,?> tm = ComponentType.THOUGHT_MODULE.castOrNull(npc.getComponent(ComponentType.THOUGHT_MODULE).getFirst());
        assert  tm != null;
        assert tm.hasInputHooksFor().contains(EventType.PING);
        assert tm.hasInputHooksFor().contains(EventType.PONG);

        SystemListener npcSystem = EntityManager.GET().systemListenerByType(SystemType.NPC);
        assert npcSystem != null;
        assert npcSystem.isListeningFor(EventType.PING);
        assert npcSystem.isListeningFor(EventType.PONG);



        assert btd.bool.get();


    }
}
