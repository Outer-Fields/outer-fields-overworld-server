package io.mindspice.outerfieldsserver.components.world;

import io.mindspice.outerfieldsserver.components.Component;
import io.mindspice.outerfieldsserver.components.logic.PredicateLib;
import io.mindspice.outerfieldsserver.entities.Entity;
import io.mindspice.outerfieldsserver.enums.ComponentType;
import io.mindspice.outerfieldsserver.systems.event.EventData;
import io.mindspice.outerfieldsserver.systems.event.Event;
import io.mindspice.outerfieldsserver.systems.event.EventType;
import io.mindspice.mindlib.data.geometry.IConcurrentPQuadTree;
import io.mindspice.mindlib.data.geometry.IPolygon2;
import io.mindspice.mindlib.functional.consumers.BiPredicatedBiConsumer;

import java.util.List;


public class CollisionGrid extends Component<CollisionGrid> {

    public final IConcurrentPQuadTree<IPolygon2> collisionGrid;

    public CollisionGrid(Entity parentEntity, IConcurrentPQuadTree<IPolygon2> collisionGrid) {
        super(parentEntity, ComponentType.COLLISION_GRID, List.of(EventType.COLLISION_CHANGE));
        this.collisionGrid = collisionGrid;
        registerListener(EventType.COLLISION_UPDATE, BiPredicatedBiConsumer.of(PredicateLib::isSameAreaEvent, CollisionGrid::onCollisionUpdate));
    }

    public void onCollisionUpdate(Event<EventData.CollisionData> event) {
        if (event.data().isRemoved()) {
            removeCollisionFromGrid(event.data().poly());
        } else {
            addCollisionToGrid(event.data().poly());
        }
    }

    public void addCollisionToGrid(List<IPolygon2> collisionPolys) {
        collisionPolys.forEach(this::addCollisionToGrid);

        
    }

    public void addCollisionToGrid(IPolygon2 collisionPoly) {
        collisionGrid.insert(collisionPoly, collisionPoly);
        emitEvent(Event.collisionChange(this, new EventData.CollisionData(false, collisionPoly)));
    }

    public void removeCollisionFromGrid(IPolygon2 collisionPoly) {
        collisionGrid.remove(collisionPoly, collisionPoly);
        emitEvent(Event.collisionChange(this, new EventData.CollisionData(true, collisionPoly)));
    }


}
