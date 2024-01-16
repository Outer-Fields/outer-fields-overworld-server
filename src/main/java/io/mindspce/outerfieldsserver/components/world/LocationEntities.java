package io.mindspce.outerfieldsserver.components.world;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import io.mindspce.outerfieldsserver.components.Component;
import io.mindspce.outerfieldsserver.components.logic.PredicateLib;
import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.entities.LocationEntity;
import io.mindspce.outerfieldsserver.enums.ComponentType;
import io.mindspce.outerfieldsserver.systems.event.Event;
import io.mindspce.outerfieldsserver.systems.event.EventType;
import io.mindspice.mindlib.functional.consumers.BiPredicatedBiConsumer;

import java.util.List;


public class LocationEntities extends Component<LocationEntities> {
    TIntObjectMap<LocationEntity> locationMap = new TIntObjectHashMap<>();

    public LocationEntities(Entity parentEntity) {
        super(parentEntity, ComponentType.LOCATION_ENTITIES, List.of());
        registerListener(EventType.LOCATION_NEW, BiPredicatedBiConsumer.of(
                PredicateLib::isSameAreaEvent, LocationEntities::onNewLocation)
        );
        registerListener(EventType.LOCATION_REMOVE, BiPredicatedBiConsumer.of(
                PredicateLib::isSameAreaEvent, LocationEntities::onRemoveLocation)
        );
    }

    public void onNewLocation(Event<LocationEntity> event) {
        locationMap.put(event.issuerEntityId(), event.data());
    }

    public void onRemoveLocation(Event<Integer> event) {
        locationMap.remove(event.data());
    }

}
