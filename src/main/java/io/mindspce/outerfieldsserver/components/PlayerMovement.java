package io.mindspce.outerfieldsserver.components;

import io.mindspce.outerfieldsserver.area.AreaEntity;
import io.mindspce.outerfieldsserver.core.authority.PlayerAuthority;
import io.mindspce.outerfieldsserver.core.networking.incoming.NetInPlayerPosition;
import io.mindspce.outerfieldsserver.data.wrappers.DynamicTileRef;
import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.enums.ComponentType;
import io.mindspce.outerfieldsserver.systems.EventData;
import io.mindspce.outerfieldsserver.systems.event.Event;
import io.mindspce.outerfieldsserver.systems.event.EventType;
import io.mindspice.mindlib.data.collections.other.GridArray;
import io.mindspice.mindlib.data.geometry.ILine2;
import io.mindspice.mindlib.data.geometry.IMutLine2;
import io.mindspice.mindlib.data.geometry.IRect2;

import java.util.List;


/**
 * The type Player movement.
 */
public class PlayerMovement extends Component<PlayerMovement> {
    private final GridArray<DynamicTileRef> localGrid;
    private final IRect2 viewRect;
    private final IMutLine2 mVector = ILine2.ofMutable(0, 0, 0, 0);
    private long lastUpdateTime = System.currentTimeMillis();
    private int timeout = 0;

    private AreaEntity areaEntity;

    /**
     * Used to handle and validate players movement input, provides collision/speed authority and emits the event
     * ENTITY_POSITION_UPDATE if movement was detected, emits an event of the movement as either valid or invalid
     * if not change in position was detected no event will be emitted Should be hooked into via a GlobalPosition
     * component. Requires references to other components, ideally from the same SubSystem, this done in-place of
     * queries for performance reason.
     * @param parentEntity the parent entity
     * @param localGrid    the local grid
     * @param viewRect     the view rect
     */
    public PlayerMovement(Entity parentEntity, GridArray<DynamicTileRef> localGrid, IRect2 viewRect) {
        super(parentEntity, ComponentType.NET_PLAYER_POSITION, List.of(EventType.PLAYER_VALID_MOVEMENT, EventType.PLAYER_INVALID_MOVEMENT));
        this.localGrid = localGrid;
        this.viewRect = viewRect;

        registerListener(EventType.NETWORK_IN_PLAYER_POSITION, PlayerMovement::onPlayerMovementIn);
    }

    /**
     * On player movement in received via network in.
     * Emits mutable start and endpoint meant to be hooked into by another SubSystem component, actual movement events
     * should be broadcast from a GlobalPosition component based on the validation event emitted (via hook and intercept)
     * @param event the network event in
     */
    public void onPlayerMovementIn(Event<NetInPlayerPosition> event) {
        if (timeout > 0) {
            timeout--;
            lastUpdateTime = event.data().timestamp();
            return;
        }
        mVector.shiftLine(event.data().x(), event.data().y());
        boolean validDist = PlayerAuthority.validateDistance(mVector, lastUpdateTime, event.data().timestamp());
        boolean validColl = PlayerAuthority.validateCollision(localGrid.get(2, 2).getAreaRef(), localGrid, viewRect, mVector);
        if (!validDist || !validColl) {
            timeout = 30;
            // TODO LOG THIS, Send player correction packet
        }
        lastUpdateTime = event.data().timestamp();

        if (!mVector.start().equals(mVector.end())) { // Sending mutable tileData since this get intercepted at the controller component level
            emitEvent(Event.entityPosition(this, new EventData.EntityPositionChanged(true, mVector.start(), mVector.end())));
        }

    }
}
