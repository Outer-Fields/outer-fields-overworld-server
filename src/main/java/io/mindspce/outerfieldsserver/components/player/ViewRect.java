package io.mindspce.outerfieldsserver.components.player;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import io.mindspce.outerfieldsserver.components.Component;
import io.mindspce.outerfieldsserver.components.entity.GlobalPosition;
import io.mindspce.outerfieldsserver.components.logic.PredicateLib;
import io.mindspce.outerfieldsserver.core.Tick;
import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.enums.ComponentType;
import io.mindspce.outerfieldsserver.enums.EntityType;
import io.mindspce.outerfieldsserver.systems.EventData;
import io.mindspce.outerfieldsserver.systems.event.Event;
import io.mindspce.outerfieldsserver.systems.event.EventType;
import io.mindspice.mindlib.data.geometry.IConstRect2;
import io.mindspice.mindlib.data.geometry.IMutRect2;
import io.mindspice.mindlib.data.geometry.IRect2;
import io.mindspice.mindlib.data.geometry.IVector2;
import io.mindspice.mindlib.functional.consumers.BiPredicatedBiConsumer;
import org.springframework.security.core.parameters.P;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.function.BiPredicate;


public class ViewRect extends Component<ViewRect> {
    public final IMutRect2 viewRect;
    public boolean emitMutable;
    public TIntSet inView = new TIntHashSet(10);
    public long lastTickProc = 0;

    public ViewRect(Entity parentEntity, IVector2 size, IVector2 position, boolean emitMutable) {
        super(parentEntity, ComponentType.VIEW_RECT,
                List.of(EventType.ENTITY_VIEW_RECT_CHANGED, EventType.ENTITY_VIEW_RECT_ENTERED)
        );
        viewRect = IRect2.fromCenterMutable(position, size);

        registerListener(EventType.ENTITY_POSITION_CHANGED, BiPredicatedBiConsumer.of(
                (ViewRect vr, Event<EventData.EntityPositionChanged> event) ->
                        event.eventArea() == areaId() && event.issuerEntityId() != entityId(),
                ViewRect::onEntityPositionChanged));
    }

    public void onSelfPositionChanged(Event<EventData.EntityPositionChanged> event) {
        viewRect.reCenter(event.data().newPosition());
    }

    public void onEntityPositionChanged(Event<EventData.EntityPositionChanged> event) {
        if (viewRect.contains(event.data().newPosition()) && !inView.contains(event.issuerEntityId())) {
            onEnterView(event.issuerEntityId());
        }
    }

    public void updateWithEntityQuery(int[] entityIds) {
        for (var known : inView.toArray()) {
            boolean stillInView = false;
            for (var id : entityIds) {
                if (!inView.contains(id)) { onEnterView(id); }
                if (id == known) { stillInView = true; }
            }
            if (!stillInView) { onExitView(known); }
        }
    }

    public void onExitView(int id) {
        inView.remove(id);
        emitEvent(Event.entityViewRectExited(this, id));
    }

    public void onEnterView(int id) {
        inView.add(id);
        emitEvent(Event.entityViewRectEntered(this, id));
    }

    private void onTickConsumer(Tick tick) {
        if (tick.tickTime() - lastTickProc > 1000) {
            if (inView.isEmpty()) { return; }
            final IRect2 view = IRect2.of(viewRect);

        }
    }

    public void onQueryResponse(Event<int[]> event) {

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
