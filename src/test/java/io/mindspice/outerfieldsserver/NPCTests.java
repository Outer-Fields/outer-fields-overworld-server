package io.mindspice.outerfieldsserver;

import io.mindspice.outerfieldsserver.ai.decisiongraph.DecisionEventGraph;
import io.mindspice.outerfieldsserver.ai.decisiongraph.NewActionTask;
import io.mindspice.outerfieldsserver.ai.decisiongraph.RootNode;
import io.mindspice.outerfieldsserver.ai.decisiongraph.actions.ActionTask;
import io.mindspice.outerfieldsserver.ai.decisiongraph.decisions.PredicateNode;
import io.mindspice.outerfieldsserver.ai.task.Task;

import io.mindspice.outerfieldsserver.entities.AreaEntity;
import io.mindspice.outerfieldsserver.entities.ChunkEntity;
import io.mindspice.outerfieldsserver.area.ChunkJson;
import io.mindspice.outerfieldsserver.area.TileData;
import io.mindspice.outerfieldsserver.components.Component;
import io.mindspice.outerfieldsserver.components.ai.ThoughtModule;
import io.mindspice.outerfieldsserver.components.npc.NPCMovement;
import io.mindspice.outerfieldsserver.core.singletons.EntityManager;
import io.mindspice.outerfieldsserver.core.systems.WorldSystem;
import io.mindspice.outerfieldsserver.entities.Entity;
import io.mindspice.outerfieldsserver.entities.NonPlayerEntity;
import io.mindspice.outerfieldsserver.entities.PositionalEntity;
import io.mindspice.outerfieldsserver.enums.*;
import io.mindspice.outerfieldsserver.systems.event.EventData;
import io.mindspice.outerfieldsserver.systems.event.Event;
import io.mindspice.outerfieldsserver.systems.event.EventType;
import io.mindspice.outerfieldsserver.systems.event.SystemListener;
import io.mindspice.outerfieldsserver.util.GridUtils;
import io.mindspice.mindlib.data.collections.lists.CyclicList;
import io.mindspice.mindlib.data.geometry.IRect2;
import io.mindspice.mindlib.data.geometry.IVector2;
import org.junit.Test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;


public class NPCTests {

    public static class TestComponent extends Component<EventCompSystemTests.TestComponent> {
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
            super(98429384, systemType, doStart);
        }
    }

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

        return new WorldSystem(3423423, Map.of(AreaId.TEST, area));

    }

    public record BasicFocus(
            Entity entity,
            CyclicList<IVector2> locations
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

        BasicFocus bf = new BasicFocus(entity, new CyclicList<>(List.of(IVector2.of(784, 766), IVector2.of(672, 960))));

        ThoughtModule<BasicFocus> thoughtModule = new ThoughtModule<>(entity, 100);

        Function<BasicFocus, NewActionTask<BasicTaskData>> travel = (BasicFocus focus) -> {
            var taskData = new BasicTaskData(focus.locations.getNext(), new AtomicBoolean(false));
            Task<BasicTaskData> task = Task.builder("MoveToLocation", taskData)
                    .addSubEventTask(
                            EventType.NPC_ARRIVED_AT_LOC,
                            (BasicTaskData td, Event<?> event) -> {
                                EventData.NPCLocationArrival data = EventType.NPC_ARRIVED_AT_LOC.castOrNull(td);
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

            return NewActionTask.of(task, false);
        };

        // @formatter:off
        DecisionEventGraph<BasicFocus> graph =
                new DecisionEventGraph<BasicFocus>(new RootNode<>())
                        .addChild(PredicateNode.of("PredicateNode", (BasicFocus f) -> f.entity != null))
                            .addLeaf(ActionTask.of("Test_Travel", travel));
        // @formatter:on

        thoughtModule.addDecisionGraph(graph, bf);

        NPCMovement NPCMovement = ComponentType.NPC_MOVEMENT.castOrNull(entity.getComponent(ComponentType.NPC_MOVEMENT));

        if (NPCMovement == null) {
            System.out.println("Null controller");
        } else {
            thoughtModule.registerOutputHook(EventType.NPC_TRAVEL_TO, NPCMovement::onMoveTo, true);
            NPCMovement.registerOutputHook(EventType.NPC_ARRIVED_AT_LOC, thoughtModule::onEvent, false);
        }

        entity.addComponent(thoughtModule);
        Event.emitAndRegisterPositionalEntity(SystemType.NPC, entity.areaId(), IVector2.of(0, 0), entity);

        return testCount;

    }

    public static BasicTaskData testThought12(NonPlayerEntity entity) throws InterruptedException {
        AtomicBoolean testCount = new AtomicBoolean(false);
        ThoughtModule<BasicFocus> thoughtModule = new ThoughtModule<>(entity, 20);
        BasicFocus bf = new BasicFocus(entity, new CyclicList<>(List.of()));
        var taskData = new BasicTaskData(IVector2.of(0, 0), new AtomicBoolean(false));

        Function<BasicFocus, NewActionTask<BasicTaskData>> travel = (BasicFocus focus) -> {
            Task<BasicTaskData> task = Task.builder("PingPong", taskData)
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
                    .setOnCompletion((td) -> taskData.bool().set(true))
                    .build();
            return NewActionTask.of(task, false);
        };

        // @formatter:off
        DecisionEventGraph<BasicFocus> graph =
                new DecisionEventGraph<BasicFocus>(new RootNode<>())
                        .addChild(PredicateNode.of("PredicateNode", List.of((BasicFocus bfoc) -> bfoc.entity != null)))
                        .addLeaf(ActionTask.of("TravelAction", travel));
        // @formatter:on

        thoughtModule.addDecisionGraph(graph, bf);

        NPCMovement NPCMovement = ComponentType.NPC_MOVEMENT.castOrNull(entity.getComponent(ComponentType.NPC_MOVEMENT));

        if (NPCMovement == null) {
            System.out.println("Null controller");
        } else {
            thoughtModule.registerOutputHook(EventType.NPC_TRAVEL_TO, NPCMovement::onMoveTo, true);
            NPCMovement.registerOutputHook(EventType.NPC_ARRIVED_AT_LOC, thoughtModule::onEvent, false);
        }

        entity.addComponent(thoughtModule);
        Event.emitAndRegisterPositionalEntity(SystemType.NPC, entity.areaId(), IVector2.of(0, 0), entity);
        return taskData;

    }

    @Test
    public void npcTest1() throws IOException, InterruptedException {
        var ts = new TestSystem(SystemType.NPC, true);
        var ws = worldSystem();

        Thread.sleep(1000);
        var npc = EntityManager.GET().newNonPlayerEntity(-1, "Test_NPC", List.of(EntityState.TEST),
                new ClothingItem[6], AreaId.TEST, IVector2.of(672, 960), IVector2.of(200, 200), false);

        var count = testThought1(npc);
        Thread.sleep(15_000);

        System.out.println(count.get());
        assertTrue(count.get() >= 2);

    }

    @Test
    public void npcTest() throws InterruptedException, IOException {
        var ts = new TestSystem(SystemType.NPC, true);
        var ws = worldSystem();

        var npc = EntityManager.GET().newNonPlayerEntity(-1, "Test_NPC", List.of(EntityState.TEST),
                new ClothingItem[6], AreaId.TEST, IVector2.of(672, 960), IVector2.of(200, 200), false);

        var btd = testThought12(npc);

        Thread.sleep(1000);

        ThoughtModule<?> tm = ComponentType.THOUGHT_MODULE.castOrNull(npc.getComponent(ComponentType.THOUGHT_MODULE));
        assert tm != null;
        assert tm.hasInputHooksFor().contains(EventType.PING);
        assert tm.hasInputHooksFor().contains(EventType.PONG);

        SystemListener npcSystem = EntityManager.GET().systemListenerByType(SystemType.NPC);
        assert npcSystem != null;
        assert npcSystem.isListeningFor(EventType.PING);
        assert npcSystem.isListeningFor(EventType.PONG);

        var tc = new TestComponent(new PositionalEntity(11, EntityType.PLAYER, AreaId.TEST, IVector2.zero()), ComponentType.SIMPLE_OBJECT, List.of());
        EntityManager.GET().emitEvent(Event.builder(EventType.PING, tc).setData(new Object()).build());
        EntityManager.GET().emitEvent(Event.builder(EventType.PONG, tc).setData(new Object()).build());

        Thread.sleep(1000);

        assert btd.bool.get();


    }
}
