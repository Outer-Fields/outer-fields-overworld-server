package io.mindspice.outerfieldsserver.components.world;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import io.mindspice.outerfieldsserver.components.Component;
import io.mindspice.outerfieldsserver.components.logic.PredicateLib;
import io.mindspice.outerfieldsserver.entities.Entity;
import io.mindspice.outerfieldsserver.entities.LocationEntity;
import io.mindspice.outerfieldsserver.enums.ComponentType;
import io.mindspice.outerfieldsserver.systems.event.Event;
import io.mindspice.outerfieldsserver.systems.event.EventType;
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
