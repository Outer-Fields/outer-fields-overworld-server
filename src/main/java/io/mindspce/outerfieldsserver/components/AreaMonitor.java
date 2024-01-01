package io.mindspce.outerfieldsserver.components;

import io.mindspce.outerfieldsserver.components.logic.PredicateLib;
import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.enums.ComponentType;
import io.mindspce.outerfieldsserver.enums.EntityType;
import io.mindspce.outerfieldsserver.systems.EventData;
import io.mindspce.outerfieldsserver.systems.event.*;
import io.mindspice.mindlib.data.geometry.IRect2;
import io.mindspice.mindlib.functional.consumers.BiPredicatedBiConsumer;

import java.util.List;


public class AreaMonitor extends Component<AreaMonitor> {
    private final IRect2 monitoredArea;

    protected AreaMonitor(Entity parent, IRect2 monitoredArea) {
        super(parent, ComponentType.AREA_MONITOR, List.of(EventType.AREA_MONITORED_ENTERED));
        this.monitoredArea = monitoredArea;
        registerListener(EventType.ENTITY_POSITION_CHANGED, BiPredicatedBiConsumer.of(
                PredicateLib::isSameAreaEvent, AreaMonitor::onEntityPositionChanged
        ));
        registerListener(EventType.ENTITY_POSITION_CHANGED, BiPredicatedBiConsumer.of(
                PredicateLib::isSameAreaEvent, AreaMonitor::onNewEntity
        ));
    }

    public void onEntityPositionChanged(Event<EventData.EntityPositionChanged> event) {
        if (monitoredArea.contains(event.data().newPosition())) {
            emitEvent(Event.Factory.newAreaEntered(
                    this, new EventData.AreaEntered(event.issuerEntityType() == EntityType.PLAYER, event.issuerEntityId())
            ));
        }
    }

    public void onNewEntity(Event<EventData.NewEntity> event) {
        if (monitoredArea.contains(event.data().position())) {
            emitEvent(Event.Factory.newAreaEntered(
                    this, new EventData.AreaEntered(event.issuerEntityType() == EntityType.PLAYER, event.issuerEntityId())
            ));
        }
    }


}