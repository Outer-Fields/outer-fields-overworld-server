package io.mindspce.outerfieldsserver.components.npc;

import io.mindspce.outerfieldsserver.components.Component;
import io.mindspce.outerfieldsserver.components.logic.PredicateLib;
import io.mindspce.outerfieldsserver.core.Tick;
import io.mindspce.outerfieldsserver.core.calculators.NavCalc;
import io.mindspce.outerfieldsserver.core.singletons.EntityManager;
import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.enums.ComponentType;
import io.mindspce.outerfieldsserver.systems.EventData;
import io.mindspce.outerfieldsserver.systems.event.Event;
import io.mindspce.outerfieldsserver.systems.event.EventType;
import io.mindspce.outerfieldsserver.util.GridUtils;
import io.mindspice.mindlib.data.geometry.IVector2;
import io.mindspice.mindlib.functional.consumers.BiPredicatedBiConsumer;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;


public class TravelController extends Component<TravelController> {
    public IVector2 position;
    public Supplier<IVector2> positionSupplier;
    public EventData.NPCTravelTo currMovement;
    public List<EventData.NPCTravelTo> queuedMovement = new LinkedList<>();
    public List<IVector2> path;

    public TravelController(Entity parentEntity, IVector2 startingPos, Supplier<IVector2> positionSupplier) {
        super(parentEntity, ComponentType.TRAVEL_CONTROLLER,
                List.of(EventType.ENTITY_POSITION_UPDATE, EventType.NPC_ARRIVED_AT_LOC)
        );
        this.position = startingPos;
        this.positionSupplier = positionSupplier;

        registerListener(EventType.NPC_TRAVEL_TO, BiPredicatedBiConsumer.of(
                PredicateLib::isRecEntitySame, TravelController::onMoveTo
        ));
        setOnTickConsumer(TravelController::onTickConsumer);
    }

    public void onMoveTo(Event<EventData.NPCTravelTo> event) {
        moveTo(event.data());
    }

    public void moveTo(EventData.NPCTravelTo data) {
        if (currMovement != null && !data.overRideExisting()) {
            queuedMovement.add(data);
            return;
        }
        position = positionSupplier.get();
        currMovement = data;
        List<IVector2> rawPath = NavCalc.getPathTo(
                EntityManager.GET().areaById(areaId()),
                GridUtils.globalToChunkTile(position),
                GridUtils.globalToChunkTile(currMovement.locationPos())
        );
        path = NavCalc.interpolatePath(rawPath, data.speed());
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
                    this, areaId(), new EventData.NpcLocationArrival(currMovement.locationKey(), currMovement.locationId()))
            );
            currMovement = null;
            return;
        }
        position = path.removeFirst();
        emitEvent(Event.entityPositionUpdate(this, this.entityId(), position));
    }
}
