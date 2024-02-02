package io.mindspice.outerfieldsserver.components.serialization;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import io.mindspice.mindlib.data.collections.lists.primative.IntList;
import io.mindspice.mindlib.data.tuples.Pair;
import io.mindspice.mindlib.functional.consumers.BiPredicatedBiConsumer;
import io.mindspice.outerfieldsserver.components.Component;
import io.mindspice.outerfieldsserver.components.logic.PredicateLib;
import io.mindspice.outerfieldsserver.core.singletons.EntityManager;
import io.mindspice.outerfieldsserver.entities.Entity;
import io.mindspice.outerfieldsserver.enums.ComponentType;
import io.mindspice.outerfieldsserver.systems.event.Event;
import io.mindspice.outerfieldsserver.systems.event.EventData;
import io.mindspice.outerfieldsserver.systems.event.EventType;

import java.util.List;


public class Visibility extends Component<Visibility> {
    private TIntSet visibleTo;
    private TIntSet invisibleTo;
    private boolean isActive = true;
    private boolean isVisibleToAll = true;
    private boolean isInvisibleToAll = false;

    public Visibility(Entity parentEntity) {
        super(parentEntity, ComponentType.VISIBILITY, List.of(
                EventType.ENTITY_IS_ACTIVE_CHANGED, EventType.ENTITY_IS_ACTIVE_RESP,
                EventType.ENTITY_VISIBILITY_RESP, EventType.ENTITY_VISIBILITY_CHANGED,
                EventType.ENTITY_VISIBLE_TO_RESPONSE
        ));

        registerListener(EventType.ENTITY_SET_ACTIVE, BiPredicatedBiConsumer.of(
                PredicateLib::isRecEntitySame, Visibility::onSetActive
        ));
        registerListener(EventType.ENTITY_VISIBILITY_UPDATE, BiPredicatedBiConsumer.of(
                PredicateLib::isRecEntitySame, Visibility::onVisibilityUpdate
        ));
        registerListener(EventType.ENTITY_IS_ACTIVE_QUERY, BiPredicatedBiConsumer.of(
                PredicateLib::isRecEntitySame, Visibility::onIsActiveQuery
        ));
        registerListener(EventType.ENTITY_VISIBILITY_QUERY, BiPredicatedBiConsumer.of(
                PredicateLib::isRecEntitySame, Visibility::onVisibilityQuery
        ));
        registerListener(EventType.ENTITY_VISIBLE_TO_QUERY, BiPredicatedBiConsumer.of(
                PredicateLib::isRecEntitySame, Visibility::onIsVisibleTo
        ));

    }

    public void onVisibilityUpdate(Event<EventData.VisibilityUpdate> event) {
        updateVisibility(event.data());
    }

    public void onSetActive(Event<Boolean> event) {
        setActive(event.data());
    }

    public void onIsVisibleTo(Event<Integer> event) {
        emitEvent(Event.responseEvent(
                this, event, EventType.ENTITY_VISIBLE_TO_RESPONSE, isVisibleToEntity(event.data()))
        );
    }

    public void onIsActiveQuery(Event<Boolean> event) {
        EntityManager.GET().emitEvent(
                Event.responseEvent(this, event, EventType.ENTITY_IS_ACTIVE_RESP, isActive)
        );
    }

    private void onVisibilityQuery(Event<Boolean> event) {
        EntityManager.GET().emitEvent(Event.responseEvent(this, event, EventType.ENTITY_VISIBILITY_RESP, getAsData()));
    }

    public EventData.EntityVisibility getAsData() {
        return new EventData.EntityVisibility(
                isActive,
                isInvisibleToAll,
                visibleTo == null ? new IntList(0) : IntList.ofNonCopy(visibleTo.toArray()),
                invisibleTo == null ? new IntList(0) : IntList.ofNonCopy(invisibleTo.toArray())
        );
    }

    public void emitVisibilityChange() {
        emitEvent(Event.entityVisibilityChange(this, areaId(), getAsData()));
    }

    public void updateVisibility(EventData.VisibilityUpdate update) {
        isVisibleToAll = update.visibleToAll();
        isInvisibleToAll = update.invisibleToAll();
        if (isInvisibleToAll) { invisibleTo.clear(); }
        if (isVisibleToAll || isInvisibleToAll) {
            visibleTo.clear();
            invisibleTo.clear();
            return;
        }
        update.visibleIds().forEach(i -> setVisibleTo(i, !update.visibleIsRemoval()));
        update.inVisibleIds().forEach(i -> setInVisibleTo(i, !update.inVisibleIsRemoval()));
        emitVisibilityChange();
    }

    public void initInActive() {
        isActive = false;
    }

    public void initInvisible() {
        isInvisibleToAll = true;
    }

    public void initVisibleIds(List<Integer> ids) {
        if (visibleTo == null) { visibleTo = new TIntHashSet(); }
        visibleTo.addAll(ids);
    }

    public void initVisibleIds(int id) {
        if (visibleTo == null) { visibleTo = new TIntHashSet(); }
        visibleTo.add(id);
    }

    public void initInvisibleIds(int id) {
        if (invisibleTo == null) { invisibleTo = new TIntHashSet(); }
        invisibleTo.add(id);
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
        EntityManager.GET().emitEvent(Event.entityIsActiveChanged(this, areaId(), Pair.of(entityId(), isActive)));

    }

    private void setVisibleTo(int entityId, boolean isVisible) {
        if (visibleTo == null) { visibleTo = new TIntHashSet(); }
        if (isVisible) {
            visibleTo.add(entityId);
            invisibleTo.remove(entityId);
        } else {
            visibleTo.remove(entityId);
            new IntList(visibleTo.toArray());
        }
    }

    private void setInVisibleTo(int entityId, boolean isInVisible) {
        if (invisibleTo == null) { invisibleTo = new TIntHashSet(); }
        if (isInVisible) {
            invisibleTo.add(entityId);
            visibleTo.remove(entityId);
        } else {
            invisibleTo.remove(entityId);
        }
    }

    public boolean isActive() {
        return isActive;
    }

    public boolean isVisibleToEntity(int entityId) {
        if (!isActive || isInvisibleToAll) {
            return false;
        }
        if (isVisibleToAll) { return true; }

        if (invisibleTo != null && invisibleTo.contains(entityId)) {
            return false;
        }
        return visibleTo == null || visibleTo.isEmpty() || visibleTo.contains(entityId);
    }

}
