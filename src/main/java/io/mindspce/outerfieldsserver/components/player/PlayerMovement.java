package io.mindspce.outerfieldsserver.components.player;

import io.mindspce.outerfieldsserver.components.Component;
import io.mindspce.outerfieldsserver.core.authority.PlayerAuthority;
import io.mindspce.outerfieldsserver.core.networking.incoming.NetInPlayerPosition;
import io.mindspce.outerfieldsserver.data.wrappers.DynamicTileRef;
import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.enums.ComponentType;
import io.mindspce.outerfieldsserver.systems.event.Event;
import io.mindspce.outerfieldsserver.systems.event.EventType;
import io.mindspice.mindlib.data.collections.other.GridArray;
import io.mindspice.mindlib.data.geometry.*;

import java.util.List;
import java.util.function.Consumer;


public class PlayerMovement extends Component<PlayerMovement> {
    private final GridArray<DynamicTileRef> localGrid;
    private final IRect2 viewRect;
    private final IMutLine2 mVector;
    private long lastUpdateTime = System.currentTimeMillis();
    private long timeout = 0;
    private final Consumer<IVector2> correctionConsumer;
    private final Consumer<IVector2> validatedConsumer;
    private final IMutLine2 testVector;

    public PlayerMovement(Entity parentEntity, IVector2 startPos, GridArray<DynamicTileRef> localGrid, IRect2 viewRect,
            Consumer<IVector2> correctionConsumer, Consumer<IVector2> validatedConsumer) {
        super(parentEntity, ComponentType.NET_PLAYER_POSITION, List.of(EventType.PLAYER_VALID_MOVEMENT, EventType.PLAYER_INVALID_MOVEMENT));
        this.localGrid = localGrid;
        this.viewRect = viewRect;
        this.correctionConsumer = correctionConsumer;
        this.validatedConsumer = validatedConsumer;
        mVector = ILine2.ofMutable(startPos, startPos);
        testVector = ILine2.ofMutable(startPos, startPos);

        registerListener(EventType.NETWORK_IN_PLAYER_POSITION, PlayerMovement::onPlayerMovementIn);
    }

    public void onPlayerMovementIn(Event<NetInPlayerPosition> event) {
        if (timeout != -1) {
            if (System.currentTimeMillis() < timeout) {
                lastUpdateTime = event.data().timestamp();
                correctionConsumer.accept(mVector.end());
                return;
            }
        }
        //mVector.shiftLine(event.data().x(), event.data().y()
        // );
        testVector.shiftLine(event.data().x(), event.data().y());
        boolean validColl = PlayerAuthority.validateCollision(localGrid.get(2, 2).getAreaRef(), localGrid, viewRect, testVector);

        if (!validColl) {
            mVector.setEnd(mVector.start());
            correctionConsumer.accept(mVector.end());
            lastUpdateTime = event.data().timestamp();
            timeout = System.currentTimeMillis() + 250;
            return;
        }
        mVector.shiftLine(event.data().x(), event.data().y());
        boolean validDist = PlayerAuthority.validateDistance(mVector, lastUpdateTime, event.data().timestamp());

        if (!validDist) {
            correctionConsumer.accept(mVector.end());
        }
        timeout = -1;
        validatedConsumer.accept(mVector.end());
        lastUpdateTime = event.data().timestamp();

//        if (!mVector.start().equals(mVector.end())) { // Sending mutable tileData since this get intercepted at the controller component level
//           // emitEvent(Event.playerValidMovement(this, mVector.end()));
//            validatedConsumer.accept(mVector.end());
//        }
//        System.out.println("Processed player movement");

    }
}
