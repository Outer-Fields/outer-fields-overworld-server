package io.mindspce.outerfieldsserver.components;

import io.mindspce.outerfieldsserver.core.Tick;
import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.enums.ComponentType;
import io.mindspce.outerfieldsserver.enums.EventProcMode;
import io.mindspce.outerfieldsserver.systems.event.Event;
import io.mindspce.outerfieldsserver.systems.event.EventType;

import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;


// TODO FIXME NOTE need to pass through inner component queries
public class SubSystem extends Component<SubSystem> {
    protected final Component<?>[] componentList;
    protected final BitSet listeningFor = new BitSet(EventType.values().length);
    protected Consumer<Event<?>> onEventConsumer;
    protected BiConsumer<SubSystem, Tick> onTickConsumer;
    protected EventProcMode currentMode;

    public SubSystem(Entity parentEntity, List<Component<?>> components, EventProcMode mode) {
        super(
                parentEntity,
                ComponentType.CONTROLLER,
                components.stream().flatMap(c -> c.emittedEvents().stream()).distinct().toList()
        );

        for (var comp : components) {
            for (EventType event : EventType.values()) {
                if (comp.isListenerFor(event)) {
                    listeningFor.set(event.ordinal());
                }
            }
        }
        componentList = components.toArray(Component<?>[]::new);
        switchMode(mode);
        setOnTickConsumer(SubSystem::doTick);
    }

    @Override
    public void onEvent(Event<?> event) {
        onEventConsumer.accept(event);
    }



    private void doTick(Tick tick) {
        for (int i = 0; i < componentList.length; ++i) {
            componentList[i].onTick(tick);
        }
    }

    public void passThroughHandler(Event<?> event) {
        for (int i = 0; i < componentList.length; i++) {
            if (componentList[i].isListenerFor(event.eventType())) {
                componentList[i].onEvent(event);
            }
        }
    }

    @Override
    public boolean isListenerFor(EventType eventType) {
        switch (currentMode) {
            case PASS_THROUGH -> {
                for (int i = 0; i < componentList.length; ++i) {
                    if (componentList[i].isListenerFor(eventType)) {
                        return true;
                    }
                }
            }
            case INTERCEPT -> {
                return super.isListenerFor(eventType);
            }
            case INTERCEPT_OR_PASS, INTERCEPT_AND_PASS -> {
                if (super.isListenerFor(eventType)) {
                    return true;
                }
                for (int i = 0; i < componentList.length; ++i) {
                    if (componentList[i].isListenerFor(eventType)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public List<EventType> getAllListeningFor() {
        switch (currentMode) {
            case PASS_THROUGH -> {
                return Arrays.stream(componentList)
                        .flatMap(component -> component.getAllListeningFor().stream())
                        .toList();
            }
            case INTERCEPT -> {
                return super.getAllListeningFor();
            }
            case INTERCEPT_OR_PASS, INTERCEPT_AND_PASS -> {
                List<EventType> eventList = Arrays.stream(componentList)
                        .flatMap(component -> component.getAllListeningFor().stream())
                        .collect(Collectors.toList());

                eventList.addAll(super.getAllListeningFor());
                return eventList;
            }
        }
        return List.of();
    }

    public void interceptHandler(Event<?> event) {
        handleEvent(this, event);
    }

    public void interceptOrPassHandler(Event<?> event) {
        if (super.isListenerFor(event.eventType())) {
            handleEvent(this, event);
        } else {
            passThroughHandler(event);
        }
    }

    public void interceptAndPassHandler(Event<?> event) {
        handleEvent(this, event);
        passThroughHandler(event);
    }

    public void setMode(EventProcMode mode) {
        switchMode(mode);
    }

    public EventProcMode currentMode() {
        return currentMode;
    }

    private void switchMode(EventProcMode mode) {
        switch (mode) {
            case PASS_THROUGH -> onEventConsumer = this::passThroughHandler;
            case INTERCEPT -> onEventConsumer = this::interceptHandler;
            case INTERCEPT_OR_PASS -> onEventConsumer = this::interceptOrPassHandler;
            case INTERCEPT_AND_PASS -> onEventConsumer = this::interceptAndPassHandler;
        }
        currentMode = mode;
    }


}
