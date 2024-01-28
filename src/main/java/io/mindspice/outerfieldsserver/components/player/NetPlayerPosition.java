package io.mindspice.outerfieldsserver.components.player;

import io.mindspice.outerfieldsserver.components.Component;
import io.mindspice.outerfieldsserver.core.authority.PlayerAuthority;
import io.mindspice.outerfieldsserver.core.networking.incoming.NetInPlayerPosition;
import io.mindspice.outerfieldsserver.data.wrappers.DynamicTileRef;
import io.mindspice.outerfieldsserver.entities.Entity;
import io.mindspice.outerfieldsserver.enums.ComponentType;
import io.mindspice.outerfieldsserver.systems.event.Event;
import io.mindspice.outerfieldsserver.systems.event.EventType;
import io.mindspice.mindlib.data.collections.other.GridArray;
import io.mindspice.mindlib.data.geometry.*;

import java.util.List;
import java.util.function.Consumer;


public class NetPlayerPosition extends Component<NetPlayerPosition> {
    private final GridArray<DynamicTileRef> localGrid;
    private final IRect2 viewRect;
    private final IMutLine2 mVector;
    private long lastUpdateTime = System.currentTimeMillis();
    private long timeout = 0;
    private final Consumer<IVector2> correctionConsumer;
    private final Consumer<IVector2> validatedConsumer;
    private final IMutLine2 testVector;

    public NetPlayerPosition(Entity parentEntity, IVector2 startPos, GridArray<DynamicTileRef> localGrid, IRect2 viewRect,
            Consumer<IVector2> correctionConsumer, Consumer<IVector2> validatedConsumer) {
        super(parentEntity, ComponentType.NET_PLAYER_POSITION, List.of(EventType.PLAYER_VALID_MOVEMENT, EventType.PLAYER_INVALID_MOVEMENT));
        this.localGrid = localGrid;
        this.viewRect = viewRect;
        this.correctionConsumer = correctionConsumer;
        this.validatedConsumer = validatedConsumer;
        mVector = ILine2.ofMutable(startPos, startPos);
        testVector = ILine2.ofMutable(startPos, startPos);

        registerListener(EventType.NETWORK_IN_PLAYER_POSITION, NetPlayerPosition::onPlayerMovementIn);
    }

    public void onPlayerMovementIn(Event<NetInPlayerPosition> event) {
        if (event.data().y() == mVector.end().x() && event.data().y() == mVector.end().y()) {
            lastUpdateTime = event.data().timestamp();
            return;
        }
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
        if (!mVector.start().equals(mVector.end())) {
            validatedConsumer.accept(mVector.end());
        }
        lastUpdateTime = event.data().timestamp();


//        if (!mVector.start().equals(mVector.end())) { // Sending mutable tileData since this get intercepted at the controller component level
//           // emitEvent(Event.playerValidMovement(this, mVector.end()));
//            validatedConsumer.accept(mVector.end());
//        }
//        System.out.println("Processed player movement");

    }
}
