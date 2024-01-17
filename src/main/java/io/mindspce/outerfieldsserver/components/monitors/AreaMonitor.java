package io.mindspce.outerfieldsserver.components.monitors;

import io.mindspce.outerfieldsserver.components.Component;
import io.mindspce.outerfieldsserver.components.logic.PredicateLib;
import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.enums.ComponentType;
import io.mindspce.outerfieldsserver.enums.EntityType;
import io.mindspce.outerfieldsserver.systems.EventData;
import io.mindspce.outerfieldsserver.systems.event.*;
import io.mindspice.mindlib.data.geometry.IRect2;
import io.mindspice.mindlib.data.geometry.IVector2;
import io.mindspice.mindlib.data.tuples.Pair;
import io.mindspice.mindlib.functional.consumers.BiPredicatedBiConsumer;

import java.util.List;


public class AreaMonitor extends Component<AreaMonitor> {
    private final IRect2 monitoredArea;

    public AreaMonitor(Entity parent, IRect2 monitoredArea) {
        super(parent, ComponentType.AREA_MONITOR, List.of(EventType.AREA_MONITOR_ENTERED));
        this.monitoredArea = monitoredArea;
        registerListener(EventType.ENTITY_POSITION_CHANGED, BiPredicatedBiConsumer.of(
                PredicateLib::isSameAreaEvent, AreaMonitor::onEntityPositionChanged
        ));
        registerListener(EventType.ENTITY_POSITION_CHANGED, BiPredicatedBiConsumer.of(
                PredicateLib::isSameAreaEvent, AreaMonitor::onNewEntity
        ));
//        registerListener(EventType.AREA_MONITOR_QUERY, BiPredicatedBiConsumer.of(
//                PredicateLib::isSameAreaEvent,
//                ));
    }

    public void onEntityPositionChanged(Event<EventData.EntityPositionChanged> event) {
        if (monitoredArea.contains(event.data().newPosition())) {
            emitEvent(Event.areaEntered(
                    this, new EventData.AreaEntered(event.issuerEntityType() == EntityType.PLAYER, event.issuerEntityId())
            ));
        }
    }

    public void onNewEntity(Event<EventData.NewEntity> event) {
        if (monitoredArea.contains(event.data().position())) {
            emitEvent(Event.areaEntered(
                    this, new EventData.AreaEntered(event.issuerEntityType() == EntityType.PLAYER, event.issuerEntityId())
            ));
        }
    }

    public void onQuery(Event<List<Pair<IVector2, Integer>>> event) {
        List<Integer> contains = event.data().stream()
                .filter(e -> monitoredArea.contains(e.first()))
                .map(Pair::second).toList();
        emitEvent(Event.responseEvent(this, event, EventType.AREA_MONITOR_RESP, contains));
    }


}