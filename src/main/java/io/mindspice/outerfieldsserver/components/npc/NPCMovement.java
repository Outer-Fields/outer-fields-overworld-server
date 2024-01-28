package io.mindspice.outerfieldsserver.components.npc;

import io.mindspice.outerfieldsserver.components.Component;
import io.mindspice.outerfieldsserver.components.logic.PredicateLib;
import io.mindspice.outerfieldsserver.core.Tick;
import io.mindspice.outerfieldsserver.core.calculators.NavCalc;
import io.mindspice.outerfieldsserver.core.singletons.EntityManager;
import io.mindspice.outerfieldsserver.entities.Entity;
import io.mindspice.outerfieldsserver.enums.ComponentType;
import io.mindspice.outerfieldsserver.systems.event.EventData;
import io.mindspice.outerfieldsserver.systems.event.Event;
import io.mindspice.outerfieldsserver.systems.event.EventType;
import io.mindspice.mindlib.data.geometry.IVector2;
import io.mindspice.mindlib.functional.consumers.BiPredicatedBiConsumer;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;


public class NPCMovement extends Component<NPCMovement> {
    public IVector2 position;
    public Supplier<IVector2> positionSupplier;
    public EventData.NPCTravelTo currMovement;
    public List<EventData.NPCTravelTo> queuedMovement = new LinkedList<>();
    public List<IVector2> path = List.of();
    public boolean pauseMovement = false;

    public NPCMovement(Entity parentEntity, IVector2 startingPos, Supplier<IVector2> positionSupplier) {
        super(parentEntity, ComponentType.NPC_MOVEMENT,
                List.of(EventType.ENTITY_POSITION_UPDATE, EventType.NPC_ARRIVED_AT_LOC)
        );
        this.position = startingPos;
        this.positionSupplier = positionSupplier;

        registerListener(EventType.NPC_TRAVEL_TO, BiPredicatedBiConsumer.of(
                PredicateLib::isRecEntitySame, NPCMovement::onMoveTo
        ));
        setOnTickConsumer(NPCMovement::onTickConsumer);
    }

    public void onMoveTo(Event<EventData.NPCTravelTo> event) {
        moveTo(event.data());
    }

    public boolean isMoving() {
        return !pauseMovement && !path.isEmpty() && !queuedMovement.isEmpty();
    }

    public void moveTo(EventData.NPCTravelTo data) {
        if (currMovement != null && !data.overRideExisting()) {
            queuedMovement.add(data);
            return;
        }
        position = positionSupplier.get();
        currMovement = data;
        path = NavCalc.getPathTo(
                EntityManager.GET().areaById(areaId()),
                position,
                currMovement.locationPos(),
                data.speed()
        );
    }

    public void resetMovement() {
        path.clear();
        queuedMovement.clear();
    }

    public void onTickConsumer(Tick tick) {
        if (currMovement == null) {
            if (!queuedMovement.isEmpty()) {
                moveTo(queuedMovement.removeFirst());
            }
            return;
        }

        if (path.isEmpty()) {
            emitEvent(Event.npcArrivedAtLocation(
                    this, areaId(), new EventData.NPCLocationArrival(currMovement.locationKey(), currMovement.locationId()))
            );
            currMovement = null;
            return;
        }
        position = path.removeFirst();
        emitEvent(Event.entityPositionUpdate(this, this.entityId(), position));
    }
}
