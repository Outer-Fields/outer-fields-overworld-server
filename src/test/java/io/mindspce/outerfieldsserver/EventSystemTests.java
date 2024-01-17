package io.mindspce.outerfieldsserver;

import io.mindspce.outerfieldsserver.components.Component;
import io.mindspce.outerfieldsserver.components.primatives.SimpleListener;
import io.mindspce.outerfieldsserver.core.singletons.EntityManager;
import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.enums.AreaId;
import io.mindspce.outerfieldsserver.enums.ComponentType;
import io.mindspce.outerfieldsserver.enums.EntityType;
import io.mindspce.outerfieldsserver.enums.SystemType;
import io.mindspce.outerfieldsserver.systems.event.Event;
import io.mindspce.outerfieldsserver.systems.event.EventType;
import io.mindspce.outerfieldsserver.systems.event.SystemListener;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.Assert.*;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;


public class EventSystemTests {

    public static class TestSystem extends SystemListener {
        public TestSystem(SystemType systemType, boolean doStart) {
            super(systemType, doStart);
            EntityManager.GET().registerSystem(this);
        }
    }


    public static class TestComponent extends Component<TestComponent> {
        int testInt;
        List<Integer> testList = new CopyOnWriteArrayList<>();

        public TestComponent(Entity parentEntity,
                ComponentType componentType,
                List<EventType> emittedEvents) {
            super(parentEntity, componentType, emittedEvents);
        }
    }


    public static class TestEntity extends Entity {

        public TestEntity(int id, EntityType entityType, AreaId areaId) {
            super(id, entityType, areaId);
        }
    }

    @BeforeEach
    public void reset() {
        EntityManager.GET().eventListeners().clear();
    }

    @Test
    public void parameterGetterTests() {
        EntityManager.GET().eventListeners().clear();
        var system = new TestSystem(SystemType.WORLD, true);
        var entity = new TestEntity(42, EntityType.PLAYER, AreaId.TEST);
        var testComp = new TestComponent(entity, ComponentType.SIMPLE_OBJECT, List.of(EventType.PONG));
        var testComp2 = new TestComponent(entity, ComponentType.SIMPLE_OBJECT, List.of(EventType.PONG));
        testComp.registerListener(EventType.PING, null);
        testComp.registerListener(EventType.ENTITY_POSITION_CHANGED, null);
        testComp2.registerInputHook(EventType.ENTITY_STATE_CHANGED, null, false);
        testComp2.registerOutputHook(EventType.ENTITY_POSITION_CHANGED, null, false);
        system.registerComponents(List.of(testComp, testComp2));
        entity.addComponents(List.of(testComp, testComp2));

        entity.addComponent(new SimpleListener(entity));
        assertEquals(1, EntityManager.GET().eventListeners().size());

        // Assert system properties
        assert system.isListeningFor(EventType.PING);
        assert system.isListeningFor(EventType.ENTITY_POSITION_CHANGED);
        assert system.hasListeningEntity(42);
        assert system.systemType() == SystemType.WORLD;

        // Assert Component properties
        assert !testComp.isOnTick();
        assert testComp.isListenerFor(EventType.PING);
        assert testComp.isListenerFor(EventType.ENTITY_POSITION_CHANGED);
        assert testComp.isListening();
        assert testComp.getAllListeningFor().contains(EventType.PING);
        assert testComp.emittedEvents().contains(EventType.PONG);
        assert testComp.componentType() == ComponentType.SIMPLE_OBJECT;

        // Assert Hooks properties
        assert testComp2.hasInputHooksFor().contains(EventType.ENTITY_STATE_CHANGED);
        assert testComp2.hasOutputHooksFor().contains(EventType.ENTITY_POSITION_CHANGED);

        assert entity.hasAttachedComponent(ComponentType.SIMPLE_OBJECT);
        assert entity.hasAttachedComponent(ComponentType.SIMPLER_LISTENER);
        assert entity.getComponent(ComponentType.SIMPLER_LISTENER).getFirst() != null;
        Component<SimpleListener> fetched =
                ComponentType.SIMPLER_LISTENER.castOrNull(entity.getComponent(ComponentType.SIMPLER_LISTENER)
                        .getFirst());

        assert fetched != null;
        assert ComponentType.SIMPLER_LISTENER.validate(fetched);

        boolean nonCastValid = ComponentType.SIMPLER_LISTENER.validate(entity.getComponent(ComponentType.SIMPLER_LISTENER)
                .getFirst());

        assert nonCastValid;
    }

    @Test
    public void listenerAndHooks() throws InterruptedException {
        EntityManager.GET().eventListeners().clear();
        var system = new TestSystem(SystemType.TEST1, true);
        var system2 = new TestSystem(SystemType.TEST2, true);
        var entity = new TestEntity(42, EntityType.PLAYER, AreaId.TEST);
        var entity2 = new TestEntity(422, EntityType.PLAYER, AreaId.TEST);
        var testComp = new TestComponent(entity, ComponentType.SIMPLE_OBJECT, List.of(EventType.PING));
        var testComp2 = new TestComponent(entity2, ComponentType.SIMPLE_OBJECT, List.of(EventType.PONG));

        AtomicInteger tc = new AtomicInteger(0);
        AtomicInteger tc2 = new AtomicInteger(0);
        AtomicInteger tc2InHook = new AtomicInteger(0);
        AtomicInteger tc2OutHook = new AtomicInteger(0);

        testComp.registerListener(EventType.PONG, ((testComponent, event) -> {
            if (testComponent.testInt == 10000) { return; }
            ;
            testComponent.testInt += 1;
            tc.set(testComponent.testInt);
            testComponent.emitEvent(new Event<>(EventType.PING, AreaId.GLOBAL, testComponent, new Object()));
        }));

        testComp2.registerListener(EventType.PING, ((testComponent, event) -> {
            if (testComponent.testInt == 10000) { return; }

            testComponent.testList.add(testComp.testInt);
            testComponent.testInt += 1;
            tc2.set(testComponent.testInt);
            testComponent.emitEvent(new Event<>(EventType.PONG, AreaId.GLOBAL, testComponent, new Object()));
        }));

        testComp2.registerInputHook(EventType.PING, e -> tc2InHook.incrementAndGet(), false);
        testComp2.registerOutputHook(EventType.PONG, e -> tc2OutHook.incrementAndGet(), false);
        system.registerComponents(List.of(testComp2));
        system2.registerComponents(List.of(testComp));
        assertTrue(system2.hasListeningEntity(testComp.entityId()));
        assertEquals(2, EntityManager.GET().eventListeners().size());

        // seed event
        EntityManager.GET().emitEvent(new Event<>(EventType.PING, AreaId.GLOBAL, testComp, new Object()));

        Thread.sleep(1000);
        // assert plain listeners
        assertEquals(10000, tc.get());
        assertEquals(10000, tc2.get());
        // assert input/output hooks
        assertEquals(10001, tc2InHook.get()); // extra 1 for the seeding ping
        assertEquals(10000, tc2OutHook.get());
    }

    @Test
    public void interruptHookTest() throws InterruptedException {
        EntityManager.GET().eventListeners().clear();
        var system = new TestSystem(SystemType.TEST1, true);
        var system2 = new TestSystem(SystemType.TEST2, true);
        var entity = new TestEntity(42, EntityType.PLAYER, AreaId.TEST);
        var entity2 = new TestEntity(422, EntityType.PLAYER, AreaId.TEST);
        var testComp = new TestComponent(entity, ComponentType.SIMPLE_OBJECT, List.of(EventType.PING));
        var testComp2 = new TestComponent(entity2, ComponentType.SIMPLE_OBJECT, List.of(EventType.PONG));

        AtomicInteger tc = new AtomicInteger(0);
        AtomicInteger tc2 = new AtomicInteger(0);
        AtomicInteger tc2InHook = new AtomicInteger(0);
        AtomicInteger tc2OutHook = new AtomicInteger(0);

        testComp.registerListener(EventType.PONG, ((testComponent, event) -> {
            if (testComponent.testInt == 10000) { return; }
            testComponent.testInt += 1;
            tc.set(testComponent.testInt);
            testComponent.emitEvent(new Event<>(EventType.PING, AreaId.GLOBAL, testComponent, new Object()));

        }));

        testComp.registerOutputHook(EventType.PING, e -> {
            tc2OutHook.incrementAndGet();
            EntityManager.GET().emitEvent(new Event<>(EventType.PING, AreaId.GLOBAL, testComp, new Object()));
        }, true);

        testComp2.registerListener(EventType.PING, ((testComponent, event) -> {
            if (testComponent.testInt == 10000) { return; }

            testComponent.testInt += 1;
            tc2.set(testComponent.testInt);
            testComponent.emitEvent(new Event<>(EventType.PONG, AreaId.GLOBAL, testComponent, new Object()));
        }));

        testComp2.registerInputHook(EventType.PING, e -> {
            tc2InHook.incrementAndGet();
            EntityManager.GET().emitEvent(new Event<>(EventType.PONG, AreaId.GLOBAL, testComp2, new Object()));

        }, true);

        system.registerComponents(List.of(testComp2));
        system2.registerComponents(List.of(testComp));



        // seed event
        EntityManager.GET().emitEvent(new Event<>(EventType.PING, AreaId.GLOBAL, testComp, new Object()));
        assertEquals(2, EntityManager.GET().eventListeners().size());
        Thread.sleep(1000);
        // assert plain listeners
        assertEquals(10000, tc.get());
        assertEquals(0, tc2.get());
        // assert input/output hook interrupts
        assertEquals(10001, tc2InHook.get()); // extra 1 for the seeding ping
        assertEquals(10000, tc2OutHook.get());
    }

    @Test
    public void directTests() throws InterruptedException {
        EntityManager.GET().eventListeners().clear();
        var system = new TestSystem(SystemType.TEST1, true);
        var system2 = new TestSystem(SystemType.TEST2, true);
        var entity = new TestEntity(42, EntityType.PLAYER, AreaId.TEST);
        var entity2 = new TestEntity(422, EntityType.PLAYER, AreaId.TEST);
        var entity3 = new TestEntity(1, EntityType.PLAYER, AreaId.TEST);
        var testComp = new TestComponent(entity, ComponentType.SIMPLE_OBJECT, List.of(EventType.PING));
        var testComp3 = new TestComponent(entity3, ComponentType.SIMPLE_OBJECT, List.of(EventType.PING));
        var testComp2 = new TestComponent(entity2, ComponentType.SIMPLE_OBJECT, List.of(EventType.PONG));

        AtomicInteger tc = new AtomicInteger(0);

        testComp.registerListener(EventType.PING, ((testComponent, event) -> {
            testComponent.testInt += 1;
            tc.set(testComponent.testInt);

        }));

        testComp2.registerListener(EventType.PING, ((testComponent, event) -> {
            testComponent.testInt += 1;
            tc.set(testComponent.testInt);

        }));

        testComp3.registerListener(EventType.PING, ((testComponent, event) -> {
            testComponent.testInt += 1;
            tc.set(testComponent.testInt);

        }));

        system.registerComponent(testComp);
        system2.registerComponent(testComp2);
        system.registerComponent(testComp3);

        assertTrue(system.hasListeningEntity(testComp.entityId()));
        assertTrue(system2.hasListeningEntity(testComp2.entityId()));
        assertTrue(system.hasListeningEntity(testComp3.entityId()));
        Event<Object> respEvent = new Event<>(EventType.PONG, AreaId.GLOBAL, testComp, new Object());
        testComp2.emitEvent(Event.responseEvent(testComp2, respEvent, EventType.PING, new Object()));

        Thread.sleep(25);

        assertEquals(1, tc.get());
        tc.set(0);

        testComp2.emitEvent(Event.directEntityCallback(testComp2, AreaId.GLOBAL, testComp.entityId(), testComp.componentType(),
                (TestComponent testComponent) -> {
                    tc.set(testComponent.entityId());
                }
        ));

        Thread.sleep(25);
        assertEquals(testComp.entityId(), tc.get());
        tc.set(0);

        testComp2.emitEvent(Event.directComponentCallback(testComp2, AreaId.GLOBAL, testComp.componentType(), testComp.entityId(),
                testComp.componentId(),
                (TestComponent testComponent) -> {
                    tc.set(testComponent.entityId());
                }
        ));

        Thread.sleep(25);
        assertEquals(testComp.entityId(), tc.get());
        tc.set(0);

        testComp2.emitEvent(Event.globalComponentCallback(testComp2, AreaId.GLOBAL, ComponentType.SIMPLE_OBJECT,
                (TestComponent testComponent) -> tc.addAndGet(testComponent.entityId())
        ));

        Thread.sleep(25);
        // make sure the issuing component is skipped
        assertEquals(testComp.entityId() + testComp3.entityId(), tc.get());
        tc.set(0);
    }


// TODO test for register and unregistering listeners and hooks


}
