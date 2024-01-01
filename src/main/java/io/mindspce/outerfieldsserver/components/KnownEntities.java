package io.mindspce.outerfieldsserver.components;

import io.mindspce.outerfieldsserver.core.singletons.EntityManager;
import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.enums.ComponentType;
import io.mindspce.outerfieldsserver.systems.event.Event;
import io.mindspce.outerfieldsserver.systems.event.EventType;
import io.mindspice.mindlib.data.geometry.IMutVector2;
import io.mindspice.mindlib.data.geometry.IRect2;
import io.mindspice.mindlib.data.geometry.IVector2;

import java.util.BitSet;
import java.util.List;
import java.util.Vector;


public class KnownEntities extends Component<KnownEntities> {

    BitSet knownEntities = new BitSet(EntityManager.GET().entityCount());
    IVector2[] corners = new IVector2[]{
            IVector2.ofMutable(0, 0), IVector2.ofMutable(0, 0),
            IVector2.ofMutable(0, 0), IVector2.ofMutable(0, 0)
    };


    public KnownEntities(Entity parentEntity,
            ComponentType componentType,
            List<EventType> emittedEvents) {
        super(parentEntity, componentType, emittedEvents);
    }

    public void onSelfViewRectChanged(Event<IRect2> event) {
        if (corners[0] != event.data().topLeft()) {

        }
    }


}
