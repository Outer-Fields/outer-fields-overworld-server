package io.mindspce.outerfieldsserver.components;

import io.mindspce.outerfieldsserver.components.logic.PredicateLib;
import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.enums.ComponentType;
import io.mindspce.outerfieldsserver.enums.EntityType;
import io.mindspce.outerfieldsserver.systems.EventData;
import io.mindspce.outerfieldsserver.systems.event.Event;
import io.mindspce.outerfieldsserver.systems.event.EventType;
import io.mindspice.mindlib.data.geometry.IMutRect2;
import io.mindspice.mindlib.data.geometry.IRect2;
import io.mindspice.mindlib.data.geometry.IVector2;
import io.mindspice.mindlib.functional.consumers.BiPredicatedBiConsumer;
import io.mindspice.mindlib.functional.consumers.PredicatedBiConsumer;

import java.util.List;


public class ViewRect extends Component<ViewRect> {
    public final IMutRect2 viewRect;
    public boolean emitMutable;

    public ViewRect(Entity parentEntity, IVector2 size, IVector2 position, boolean emitMutable) {
        super(parentEntity, ComponentType.VIEW_RECT,
                List.of(EventType.ENTITY_VIEW_RECT_CHANGED, EventType.ENTITY_VIEW_RECT_ENTERED)
        );
        viewRect = IRect2.fromCenterMutable(position, size);
        registerListener(EventType.ENTITY_POSITION_CHANGED,
                BiPredicatedBiConsumer.of(PredicateLib::isSameAreaEvent, ViewRect::onEntityPositionChanged));
    }

    public void onSelfPositionChanged(Event<EventData.EntityPositionChanged> event) {
        viewRect.reCenter(event.data().newPosition());
    }

    public void onEntityPositionChanged(Event<EventData.EntityPositionChanged> event) {
        if (viewRect.contains(event.data().newPosition())) {
            emitEvent(Event.entityViewRectEntered(
                    this,
                    new EventData.AreaEntered(entityType() == EntityType.PLAYER, event.issuerEntityId()))
            );
        }
    }

    public IRect2 getRect() {
        return viewRect;
    }

    public IRect2 getRectCloned() {
        return IRect2.of(viewRect);
    }

    public void recenter(IVector2 centerPos) {
        viewRect.reCenter(centerPos);
        emitEvent(Event.entityViewRectChanged(this, getData()));
    }

    public void resize(IVector2 size) {
        viewRect.setSize(size.x(), size.y());
        emitEvent(Event.entityViewRectChanged(this, getData()));
    }

    private IRect2 getData() {
        return emitMutable ? viewRect : IRect2.of(viewRect);
    }
}
