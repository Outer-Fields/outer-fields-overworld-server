package io.mindspce.outerfieldsserver.components.entity;

import io.mindspce.outerfieldsserver.components.Component;
import io.mindspce.outerfieldsserver.components.logic.PredicateLib;
import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.enums.ComponentType;
import io.mindspce.outerfieldsserver.enums.EntityState;
import io.mindspce.outerfieldsserver.networking.NetSerializable;
import io.mindspce.outerfieldsserver.systems.EventData;
import io.mindspce.outerfieldsserver.systems.event.Event;
import io.mindspce.outerfieldsserver.systems.event.EventType;
import io.mindspice.mindlib.functional.consumers.BiPredicatedBiConsumer;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class EntityStateComp extends Component<EntityStateComp> implements NetSerializable {
    private Set<EntityState> states;

    public EntityStateComp(Entity parentEntity, List<EntityState> states) {
        super(parentEntity, ComponentType.ENTITY_STATE, List.of(EventType.ENTITY_STATE_CHANGED));
        this.states = states == null ? new HashSet<>(0) : new HashSet<>(states);
        registerListener(EventType.ENTITY_STATE_UPDATE, BiPredicatedBiConsumer.of(
                PredicateLib::isAreaEventAndRecEntitySame,
                EntityStateComp::onStateUpdate)
        );
    }

    public void onStateUpdate(Event<EventData.EntityStateUpdate> event) {
        var data = event.data();
        if (data.clearExisting()) { states.clear(); }

        if (data.statesRemovals() != null) {
            data.statesRemovals().forEach(states::remove);
        }
        if (data.stateAdditions() != null) {
            states.addAll(data.stateAdditions());
        }
        broadcastState();
    }

    public void addState(boolean clear, EntityState state) {
        if (clear) { this.states.clear(); }
        states.add(state);
        broadcastState();
    }

    public void addStates(boolean clear, List<EntityState> states) {
        if (clear) { this.states.clear(); }
        this.states.addAll(states);
        broadcastState();
    }

    public void removeStates(boolean clear, List<EntityState> states) {
        if (clear) { this.states.clear(); }
        states.forEach(this.states::remove);
        broadcastState();
    }

    public int[] currStates() {
        return states.stream().mapToInt(EntityState::value).toArray();
    }

    public void removeState(boolean clear, EntityState state) {
        if (clear) { this.states.clear(); }
        states.remove(state);
        broadcastState();
    }

    public void broadcastState() {
        emitEvent(Event.entityStateChanged(this, List.copyOf(states)));
    }

    @Override
    public int byteSize() {
        return states == null ? 0 : states.size();
    }
//
//    @Override
//    public byte[] getBytes() {
//        ByteBuffer buffer = NetSerializable.getEmptyBuffer(byteSize());
//        buffer.put((byte) byteSize());
//        states.forEach(s -> buffer.put(s.value));
//        return buffer.array();
//    }

    @Override
    public void addBytesToBuffer(ByteBuffer buffer) {
//        buffer.put((byte) byteSize());
//        states.forEach(s -> buffer.put(s.value));
    }
}
