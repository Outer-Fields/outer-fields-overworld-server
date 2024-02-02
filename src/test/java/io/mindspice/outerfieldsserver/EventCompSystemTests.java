package io.mindspice.outerfieldsserver;

import io.mindspice.outerfieldsserver.components.Component;
import io.mindspice.outerfieldsserver.components.primatives.SimpleListener;
import io.mindspice.outerfieldsserver.core.singletons.EntityManager;
import io.mindspice.outerfieldsserver.entities.Entity;
import io.mindspice.outerfieldsserver.entities.TestEntity;
import io.mindspice.outerfieldsserver.core.systems.TestSystem;
import io.mindspice.outerfieldsserver.enums.AreaId;
import io.mindspice.outerfieldsserver.enums.ComponentType;
import io.mindspice.outerfieldsserver.enums.EntityType;
import io.mindspice.outerfieldsserver.enums.SystemType;
import io.mindspice.outerfieldsserver.systems.event.Event;
import io.mindspice.outerfieldsserver.systems.event.EventType;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.Assert.*;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;


public class EventCompSystemTests {

    public static class TestComponent extends Component<TestComponent> {
        int testInt;
        List<Integer> testList = new CopyOnWriteArrayList<>();

        public TestComponent(Entity parentEntity,
                ComponentType componentType,
                List<EventType> emittedEvents) {
            super(parentEntity, componentType, emittedEvents);
        }
    }

    @BeforeEach
    public void reset() {
        EntityManager.GET().eventListeners().clear();
    }

    @Test
    public void parameterGetterTests() throws InterruptedException {
        EntityManager.GET().eventListeners().clear();
        var system = EntityManager.GET().newTestSystem(SystemType.TEST1);
        var entity = EntityManager.GET().newTestEntity(SystemType.TEST1);
        var testComp = new TestComponent(entity, ComponentType.SIMPLE_OBJECT, List.of(EventType.PONG));
        var testComp2 = new TestComponent(entity, ComponentType.SIMPLE_OBJECT, List.of(EventType.PONG));
        testComp.registerListener(EventType.PING, null);
        testComp.registerListener(EventType.ENTITY_POSITION_CHANGED, null);
        testComp2.registerInputHook(EventType.ENTITY_STATE_CHANGED, null, false);
        testComp2.registerOutputHook(EventType.ENTITY_POSITION_CHANGED, null, false);

        // system.registerComponents(List.of(testComp, testComp2));

        Thread.sleep(10);
        entity.addComponents(List.of(testComp, testComp2));

        entity.addComponent(new SimpleListener(entity));
        assertEquals(1, EntityManager.GET().eventListeners().size());

        // Assert system properties
        assert system.isListeningFor(EventType.PING);
        assert system.isListeningFor(EventType.ENTITY_POSITION_CHANGED);
        assert system.hasListeningEntity(entity.entityId());
        assert system.systemType() == SystemType.TEST1;

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
        assert entity.getComponent(ComponentType.SIMPLER_LISTENER) != null;
        Component<SimpleListener> fetched = ComponentType.SIMPLER_LISTENER.castOrNull(entity.getComponent(ComponentType.SIMPLER_LISTENER));

        assert fetched != null;
        assert ComponentType.SIMPLER_LISTENER.validate(fetched);

        boolean nonCastValid = ComponentType.SIMPLER_LISTENER.validate(entity.getComponent(ComponentType.SIMPLER_LISTENER));

        assert nonCastValid;
    }

    @Test
    public void listenerAndHooks() throws InterruptedException {
        EntityManager.GET().eventListeners().clear();
        var system1 = EntityManager.GET().newTestSystem(SystemType.TEST1);
        var system2 = EntityManager.GET().newTestSystem(SystemType.TEST2);
        var entity = EntityManager.GET().newTestEntity(SystemType.TEST1);
        var entity2 = EntityManager.GET().newTestEntity(SystemType.TEST2);
        var testComp = new TestComponent(entity, ComponentType.SIMPLE_OBJECT, List.of(EventType.PING));
        var testComp2 = new TestComponent(entity2, ComponentType.SIMPLE_OBJECT, List.of(EventType.PONG));
        entity.addComponent(testComp);
        entity2.addComponent(testComp2);

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

        Thread.sleep(100);

        testComp2.registerInputHook(EventType.PING, e -> tc2InHook.incrementAndGet(), false);
        testComp2.registerOutputHook(EventType.PONG, e -> tc2OutHook.incrementAndGet(), false);

        assertTrue(system1.hasListeningEntity(testComp.entityId()));
        assertTrue(system2.hasListeningEntity(testComp2.entityId()));
        assertEquals(2, EntityManager.GET().eventListeners().size());
        assertTrue(system1.isListeningFor(EventType.PONG));
        assertTrue(system2.isListeningFor(EventType.PING));

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
        var system1 = EntityManager.GET().newTestSystem(SystemType.TEST1);
        var system2 = EntityManager.GET().newTestSystem(SystemType.TEST2);
        var entity = EntityManager.GET().newTestEntity(SystemType.TEST1);
        var entity2 = EntityManager.GET().newTestEntity(SystemType.TEST2);
        var testComp = new TestComponent(entity, ComponentType.SIMPLE_OBJECT, List.of(EventType.PING));
        var testComp2 = new TestComponent(entity2, ComponentType.SIMPLE_OBJECT, List.of(EventType.PONG));
        entity.addComponent(testComp);
        entity2.addComponent(testComp2);

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

    //
    @Test
    public void directTests() throws InterruptedException {
        EntityManager.GET().eventListeners().clear();
        var system1 = EntityManager.GET().newTestSystem(SystemType.TEST1);
        var system2 = EntityManager.GET().newTestSystem(SystemType.TEST2);
        var entity = EntityManager.GET().newTestEntity(SystemType.TEST1);
        var entity2 = EntityManager.GET().newTestEntity(SystemType.TEST2);
        var entity3 = EntityManager.GET().newTestEntity(SystemType.TEST2);
        var testComp = new TestComponent(entity, ComponentType.SIMPLE_OBJECT, List.of(EventType.PING));
        var testComp3 = new TestComponent(entity3, ComponentType.SIMPLE_OBJECT, List.of(EventType.PING));
        var testComp2 = new TestComponent(entity2, ComponentType.SIMPLE_OBJECT, List.of(EventType.PONG));
        entity.addComponent(testComp);
        entity2.addComponent(testComp2);
        entity3.addComponent(testComp3);

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

        assertTrue(system1.hasListeningEntity(testComp.entityId()));
        assertTrue(system2.hasListeningEntity(testComp2.entityId()));
        assertTrue(system2.hasListeningEntity(testComp3.entityId()));
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

    @Test
    // This test can fail/hand depending on GC behavior but seems to work given the content
    public void destructionTest() throws InterruptedException {
        EntityManager.GET().eventListeners().clear();
        var system1 = EntityManager.GET().newTestSystem(SystemType.TEST1);
        var entity = EntityManager.GET().newTestEntity(SystemType.TEST1);
        var testComp = new TestComponent(entity, ComponentType.SIMPLER_LISTENER, List.of(EventType.PING));
        testComp.registerListener(EventType.ENTITY_POSITION_UPDATE, (self, event) -> { });
        testComp.registerListener(EventType.QUEST_PLAYER_NEW, (self, event) -> { });

        ReferenceQueue<Object> queue = new ReferenceQueue<>();
        PhantomReference<Object> phantomReference = new PhantomReference<>(entity, queue);

        entity.addComponent(testComp);
        Thread.sleep(1000);
        assertTrue(system1.hasListeningEntity(entity.entityId()));
        assertTrue(system1.isListenerFor(EventType.ENTITY_POSITION_UPDATE));
        assertTrue(system1.isListenerFor(EventType.QUEST_PLAYER_NEW));
        assertNotNull(EntityManager.GET().entityById(entity.entityId()));

        int entityId = entity.entityId();

        assertNull(queue.poll());
        testComp = null;
        entity = null;
        EntityManager.GET().destroyEntity(entityId);
        Thread.sleep(10);

        assertNull(EntityManager.GET().entityById(entityId));
        assertFalse(system1.hasListeningEntity(entityId));
        assertFalse(system1.isListenerFor(EventType.ENTITY_POSITION_UPDATE));
        assertFalse(system1.isListenerFor(EventType.QUEST_PLAYER_NEW));

        System.gc();
        Thread.sleep(1000);
        byte[] allocation;
        while(queue.poll() == null) {
            System.out.println("waiting for clean up");
            Thread.sleep(1000);
           allocation = new byte[1000 * 1024 * 1024]; // Allocate 1000 MB to attempt to force gc

        }
    }

}
