package io.mindspice.outerfieldsserver.components.monitors;

import io.mindspice.outerfieldsserver.components.Component;
import io.mindspice.outerfieldsserver.components.dataclasses.ContainedEntity;
import io.mindspice.outerfieldsserver.components.logic.PredicateLib;
import io.mindspice.outerfieldsserver.entities.Entity;
import io.mindspice.outerfieldsserver.enums.ComponentType;
import io.mindspice.outerfieldsserver.enums.EntityType;
import io.mindspice.outerfieldsserver.systems.event.EventData;
import io.mindspice.mindlib.data.geometry.IRect2;
import io.mindspice.mindlib.data.geometry.IVector2;
import io.mindspice.mindlib.data.tuples.Pair;
import io.mindspice.mindlib.functional.consumers.BiPredicatedBiConsumer;
import io.mindspice.outerfieldsserver.systems.event.Event;
import io.mindspice.outerfieldsserver.systems.event.EventType;

import java.util.ArrayList;
import java.util.List;


public class AreaMonitor extends Component<AreaMonitor> {
    private final IRect2 monitoredArea;
    public List<ContainedEntity> inArea = new ArrayList<>(4);

    public AreaMonitor(Entity parent, IRect2 monitoredArea) {
        super(parent, ComponentType.AREA_MONITOR, List.of(EventType.AREA_MONITOR_ENTERED));
        this.monitoredArea = monitoredArea;

        registerListener(EventType.ENTITY_POSITION_CHANGED, BiPredicatedBiConsumer.of(
                (AreaMonitor am, Event<EventData.EntityPositionChanged> event) -> {
                    if (containsEntity(event.issuerEntityId())) { return true; }
                    return event.eventArea() == areaId();
                },
                AreaMonitor::onEntityPositionChanged)
        );
        registerListener(EventType.NEW_POSITIONAL_ENTITY, BiPredicatedBiConsumer.of(PredicateLib::isSameAreaEvent, AreaMonitor::onNewEntity));
        registerListener(EventType.ENTITY_DESTROY, BiPredicatedBiConsumer.of(
                PredicateLib::isSameAreaEvent, (AreaMonitor am, Event<Integer> event) -> onExitView(event.issuerEntityId()))
        );
        registerListener(EventType.AREA_MONITOR_QUERY, BiPredicatedBiConsumer.of(PredicateLib::isSameAreaEvent, AreaMonitor::onQuery));
    }

    public boolean containsEntity(int id) {
        for (int i = 0; i < inArea.size(); ++i) {
            if (inArea.get(i).id() == id) {
                return true;
            }
        }
        return false;
    }

    public void removeEntity(int id) {
        inArea.removeIf(e -> e.id() == id);
    }

    public void updateEntity(int id, IVector2 position) {
        for (int i = 0; i < inArea.size(); ++i) {
            if (inArea.get(i).id() == id) {
                inArea.get(i).position().setXY(position);
            }
        }
    }

    public void onEntityPositionChanged(Event<EventData.EntityPositionChanged> event) {
        if (monitoredArea.contains(event.data().newPosition()) && !containsEntity(event.issuerEntityId())) {
            onEnterView(event.issuerEntityId(), event.data().newPosition());
        } else if (containsEntity(event.issuerEntityId()) && !monitoredArea.contains(event.data().newPosition())) {
            onExitView(event.issuerEntityId());
        } else {
            updateEntity(event.issuerEntityId(), event.data().newPosition());
        }
    }

    public void onNewEntity(Event<EventData.NewPositionalEntity> event) {
        if (monitoredArea.contains(event.data().position())) {
            emitEvent(Event.areaEntered(
                    this, new EventData.AreaEntered(event.issuerEntityType() == EntityType.PLAYER, event.issuerEntityId())
            ));
        }
    }

    public void onExitView(int id) {
        removeEntity(id);
        emitEvent(Event.entityViewRectExited(this, id));
    }

    public void onEnterView(int id, IVector2 pos) {
        inArea.add(ContainedEntity.of(id, pos));
        emitEvent(Event.entityViewRectEntered(this, id));
    }

    public void onQuery(Event<List<Pair<IVector2, Integer>>> event) {
        List<Integer> contains = event.data().stream()
                .filter(e -> monitoredArea.contains(e.first()))
                .map(Pair::second).toList();
        emitEvent(Event.responseEvent(this, event, EventType.AREA_MONITOR_RESP, contains));
    }


}