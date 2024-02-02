package io.mindspice.outerfieldsserver.components.player;

import io.mindspice.outerfieldsserver.components.Component;
import io.mindspice.outerfieldsserver.core.networking.incoming.NetInPlayerAction;
import io.mindspice.outerfieldsserver.entities.Entity;
import io.mindspice.outerfieldsserver.enums.ComponentType;
import io.mindspice.outerfieldsserver.systems.event.Event;
import io.mindspice.outerfieldsserver.systems.event.EventType;

import java.util.List;


public class PlayerActions extends Component<PlayerActions> {
    public PlayerActions(Entity parentEntity) {
        super(parentEntity, ComponentType.PLAYER_ACTIONS, List.of());
        registerListener(EventType.NETWORK_IN_PLAYER_ACTION, PlayerActions::onPlayerActions);
    }

    public void onPlayerActions(Event<List<NetInPlayerAction>> event) {

    }


}
