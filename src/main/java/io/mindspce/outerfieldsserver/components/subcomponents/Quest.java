package io.mindspce.outerfieldsserver.components.subcomponents;

import io.mindspce.outerfieldsserver.components.Component;
import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.entities.item.ItemEntity;
import io.mindspce.outerfieldsserver.entities.locations.LocationEntity;
import io.mindspce.outerfieldsserver.systems.Task;
import io.mindspce.outerfieldsserver.systems.event.Event;
import io.mindspce.outerfieldsserver.systems.event.EventType;

import java.util.List;


public class Quest extends Component<Quest> {
    List<LocationEntity> questLocations;
    List<ItemEntity> questItems;
    List<Task> questProgression;

    public Quest(Entity parentEntity) {
        super(parentEntity);
        if (questLocations != null) {
            listenerCache.addListener(EventType.AREA_ENTERED, );
        }
    }

    public void locationCheck(Event event) {
        for (var location : questLocations) {
            if (location.id() == event.issuerId()) {
                for(var task : questProgression) {
                    task.
                }
            }
        }
    }

    @Override
    public void onTick(long tickTime, double delta) {

    }
}
