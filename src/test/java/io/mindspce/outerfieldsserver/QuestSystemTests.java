package io.mindspce.outerfieldsserver;

import io.mindspce.outerfieldsserver.components.quest.QuestModule;
import io.mindspce.outerfieldsserver.core.singletons.EntityManager;
import io.mindspce.outerfieldsserver.core.systems.QuestSystem;
import io.mindspce.outerfieldsserver.entities.PlayerEntity;
import io.mindspce.outerfieldsserver.enums.*;
import io.mindspce.outerfieldsserver.systems.event.Event;
import io.mindspce.outerfieldsserver.systems.event.EventType;
import io.mindspice.mindlib.data.geometry.IVector2;
import io.mindspice.mindlib.data.tuples.Pair;
import org.junit.Test;

import static org.junit.Assert.*;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


public class QuestSystemTests {

    public QuestSystem questSystem() {
        return new QuestSystem(true);
    }

    public static class TestData {
        public AtomicInteger testVar = new AtomicInteger(0);
        public AtomicBoolean completed = new AtomicBoolean(false);
        public AtomicBoolean tickBool = new AtomicBoolean(false);
    }

    public TestData testEvent(PlayerEntity playerEntity, boolean concurrent) {

        var questEntity = EntityManager.GET().newPlayerQuestEntity(PlayerQuests.TEST, playerEntity.playerId(), false);
        var td = new TestData();

        var qm = new QuestModule<>(questEntity, List.of(EventType.PING, EventType.PONG), td,
                concurrent, (module, data) -> data.completed.set(true), false);

        qm.addSubEventTask(
                EventType.KEY_VALUE_EVENT,
                ((testData, event) -> {
                    @SuppressWarnings("unchecked")
                    var eventData = (Pair<Integer, Integer>) event.data();
                    if (eventData.first() + eventData.second() == 2) {
                        testData.testVar.incrementAndGet();
                        return true;
                    }
                    return false;
                }),
                (testData -> {
                    testData.testVar.incrementAndGet();
                    System.out.println("started key-value");
                }),
                false
        );

        qm.addSubEventTask(
                EventType.PING,
                ((testData, event) -> {
                    testData.testVar.incrementAndGet();
                    return true;
                }),
                (testData -> {
                    testData.testVar.incrementAndGet();
                    System.out.println("started ping");
                }),
                false
        );

        questEntity.addComponent(qm);
        Event.emitAndRegisterEntity(SystemType.QUEST, AreaId.TEST, IVector2.negOne(), questEntity);
        EntityManager.GET().emitEvent(Event.newPlayerQuest(questEntity));

        return td;
    }

    public TestData testTickEvent(PlayerEntity playerEntity) {

        var questEntity = EntityManager.GET().newPlayerQuestEntity(PlayerQuests.TEST, playerEntity.playerId(), false);
        var td = new TestData();

        var qm = new QuestModule<>(questEntity, List.of(EventType.PING, EventType.PONG), td,
                false, (module, data) -> data.completed.set(true), false);

        qm.addSubEventTask(
                EventType.PING,
                ((testData, event) -> true),
                (testData -> {
                    testData.testVar.incrementAndGet();
                    System.out.println("started key-value");
                }),
                false
        );

        qm.addSubEventTickTask(
                EventType.KEY_VALUE_EVENT,
                ((testData, event) -> {
                    @SuppressWarnings("unchecked")
                    var eventData = (Pair<Integer, Integer>) event.data();
                    if (eventData.first() + eventData.second() == 2) {
                        testData.testVar.incrementAndGet();
                        return true;
                    }
                    return false;
                }),
                (data, tick) -> {
                    if (data.testVar.getAndIncrement() == 12) {
                        return true;
                    }
                    return false;
                },
                (testData -> {
                    testData.testVar.incrementAndGet();
                    System.out.println("started key-value");
                }),
                false
        );

        questEntity.addComponent(qm);
        Event.emitAndRegisterEntity(SystemType.QUEST, AreaId.TEST, IVector2.negOne(), questEntity);
        EntityManager.GET().emitEvent(Event.newPlayerQuest(questEntity));

        return td;
    }

    @Test
    public void testTickAndEvent() throws InterruptedException {
        var qs = questSystem();
        var td = testTickEvent(new PlayerEntity(0, -1, "test_player", List.of(),
                new ClothingItem[6], AreaId.TEST, IVector2.zero(), null)
        );

        Thread.sleep(50);


        EntityManager.GET().emitEvent(new Event<>(EventType.KEY_VALUE_EVENT, AreaId.NONE, -1, -1,
                ComponentType.ANY, EntityType.ANY, -1, -1, ComponentType.ANY, Pair.of(1, 1))
        );


        EntityManager.GET().emitEvent(new Event<>(EventType.PING, AreaId.TEST, -1, -1,
                ComponentType.ANY, EntityType.ANY, -1, -1, ComponentType.ANY, new Object())
        );


        Thread.sleep(1000);
        assertFalse(td.completed.get());

        EntityManager.GET().emitEvent(new Event<>(EventType.KEY_VALUE_EVENT, AreaId.NONE, -1, -1,
                ComponentType.ANY, EntityType.ANY, -1, -1, ComponentType.ANY, Pair.of(1, 1))
        );


        Thread.sleep(1000);

        System.out.println(td.testVar);
        assertTrue(td.completed.get());

    }

    @Test
    public void questTestConcurrent() throws InterruptedException {
        var qs = questSystem();
        var td = testEvent(new PlayerEntity(0, -1, "test_player", List.of(),
                new ClothingItem[6], AreaId.TEST, IVector2.zero(), null), true
        );

        Thread.sleep(50);
        EntityManager.GET().emitEvent(new Event<>(EventType.KEY_VALUE_EVENT, AreaId.NONE, -1, -1,
                ComponentType.ANY, EntityType.ANY, -1, -1, ComponentType.ANY, Pair.of(1, 1))
        );

        EntityManager.GET().emitEvent(new Event<>(EventType.PING, AreaId.TEST, -1, -1,
                ComponentType.ANY, EntityType.ANY, -1, -1, ComponentType.ANY, new Object())
        );

        Thread.sleep(50);

        assertTrue(td.completed.get());
        assertEquals(4, td.testVar.get());
    }

    @Test
    public void questTestSequential() throws InterruptedException {
        var qs = questSystem();
        var td = testEvent(new PlayerEntity(0, -1, "test_player", List.of(),
                new ClothingItem[6], AreaId.TEST, IVector2.zero(), null), false
        );

        Thread.sleep(50);

        EntityManager.GET().emitEvent(new Event<>(EventType.PING, AreaId.TEST, -1, -1,
                ComponentType.ANY, EntityType.ANY, -1, -1, ComponentType.ANY, new Object())
        );

        EntityManager.GET().emitEvent(new Event<>(EventType.KEY_VALUE_EVENT, AreaId.NONE, -1, -1,
                ComponentType.ANY, EntityType.ANY, -1, -1, ComponentType.ANY, Pair.of(1, 1))
        );

        Thread.sleep(50);

        assertFalse(td.completed.get());
        assertNotEquals(4, td.testVar.get());

        Thread.sleep(50);

        EntityManager.GET().emitEvent(new Event<>(EventType.PING, AreaId.TEST, -1, -1,
                ComponentType.ANY, EntityType.ANY, -1, -1, ComponentType.ANY, new Object())
        );

        Thread.sleep(50);

        assertTrue(td.completed.get());
        assertEquals(4, td.testVar.get());
    }


}
