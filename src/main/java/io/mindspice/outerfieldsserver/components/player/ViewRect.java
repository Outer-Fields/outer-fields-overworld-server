package io.mindspice.outerfieldsserver.components.player;

import io.mindspice.outerfieldsserver.components.Component;
import io.mindspice.outerfieldsserver.components.dataclasses.ContainedEntity;
import io.mindspice.outerfieldsserver.components.logic.PredicateLib;
import io.mindspice.outerfieldsserver.entities.Entity;
import io.mindspice.outerfieldsserver.enums.ComponentType;
import io.mindspice.outerfieldsserver.systems.event.EventData;
import io.mindspice.outerfieldsserver.systems.event.Event;
import io.mindspice.outerfieldsserver.systems.event.EventType;
import io.mindspice.mindlib.data.geometry.*;
import io.mindspice.mindlib.functional.consumers.BiPredicatedBiConsumer;

import java.util.ArrayList;
import java.util.List;


public class ViewRect extends Component<ViewRect> {
    public final IMutRect2 viewRect;
    public boolean emitMutable;
    public List<ContainedEntity> inView = new ArrayList<>(4);

    public ViewRect(Entity parentEntity, IVector2 size, IVector2 position, boolean emitMutable) {
        super(parentEntity, ComponentType.VIEW_RECT,
                List.of(EventType.ENTITY_VIEW_RECT_CHANGED, EventType.ENTITY_VIEW_RECT_ENTERED, EventType.Entity_VIEW_RECT_EXITED)
        );
        viewRect = IRect2.fromCenterMutable(position, size);

        registerListener(EventType.ENTITY_POSITION_CHANGED, BiPredicatedBiConsumer.of(
                (ViewRect vr, Event<EventData.EntityPositionChanged> event) -> {
                    if (containsEntity(event.issuerEntityId())) { return true; }
                    return event.eventArea() == areaId();
                },
                ViewRect::onEntityPositionChanged)
        );
        registerListener(EventType.NEW_ENTITY, BiPredicatedBiConsumer.of(PredicateLib::isSameAreaEvent, ViewRect::onNewEntity));
        registerListener(EventType.ENTITY_DESTROYED, BiPredicatedBiConsumer.of(
                PredicateLib::isSameAreaEvent, (ViewRect vr, Event<Integer> event) -> onExitView(event.issuerEntityId()))
        );
    }

    public boolean containsEntity(int id) {
        for (int i = 0; i < inView.size(); ++i) {
            if (inView.get(i).id() == id) {
                return true;
            }
        }
        return false;
    }

    public void removeEntity(int id) {
        inView.removeIf(e -> e.id() == id);
    }

    public void updateEntity(int id, IVector2 position) {
        for (int i = 0; i < inView.size(); ++i) {
            if (inView.get(i).id() == id) {
                inView.get(i).position().setXY(position);
            }
        }
    }

    public void onNewEntity(Event<EventData.NewEntity> event) {
        if (viewRect.contains(event.data().position())) {
            onEnterView(event.issuerEntityId(), event.data().position());
        }
    }

    public void onSelfPositionChanged(Event<EventData.EntityPositionChanged> event) {
        viewRect.reCenter(event.data().newPosition());
    }

    public void onEntityPositionChanged(Event<EventData.EntityPositionChanged> event) {
        if (viewRect.contains(event.data().newPosition()) && !containsEntity(event.issuerEntityId())) {
            onEnterView(event.issuerEntityId(), event.data().newPosition());
        } else if (containsEntity(event.issuerEntityId()) && !viewRect.contains(event.data().newPosition())) {
            onExitView(event.issuerEntityId());
        } else if (viewRect.contains(event.data().newPosition())) {
            updateEntity(event.issuerEntityId(), event.data().newPosition());
        }
    }

//    public void updateWithEntityQuery(int[] entityIds) {
//        for (var known : inView.toArray()) {
//            boolean stillInView = false;
//            for (var id : entityIds) {
//                if (!inView.contains(id)) { onEnterView(id); }
//                if (id == known) { stillInView = true; }
//            }
//            if (!stillInView) { onExitView(known); }
//        }
//    }

    public void onExitView(int id) {
        removeEntity(id);
        emitEvent(Event.entityViewRectExited(this, id));
    }

    public void onEnterView(int id, IVector2 pos) {
        inView.add(ContainedEntity.of(id, pos));
        emitEvent(Event.entityViewRectEntered(this, id));
    }

//    private void onTickConsumer(Tick tick) {
//        if (tick.tickTime() - lastTickProc > 1000) {
//            if (inView.isEmpty()) { return; }
//            final IRect2 view = IRect2.of(viewRect);
//
//        }
//    }

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
