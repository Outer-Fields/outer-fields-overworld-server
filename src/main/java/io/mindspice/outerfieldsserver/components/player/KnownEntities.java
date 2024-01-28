package io.mindspice.outerfieldsserver.components.player;

import io.mindspice.outerfieldsserver.entities.AreaEntity;
import io.mindspice.outerfieldsserver.components.world.EntityGrid;
import io.mindspice.outerfieldsserver.components.Component;
import io.mindspice.outerfieldsserver.core.singletons.EntityManager;
import io.mindspice.outerfieldsserver.entities.Entity;
import io.mindspice.outerfieldsserver.enums.ComponentType;
import io.mindspice.outerfieldsserver.systems.event.EventData;
import io.mindspice.outerfieldsserver.systems.event.Event;
import io.mindspice.mindlib.data.geometry.ILine2;
import io.mindspice.mindlib.data.geometry.IMutLine2;
import io.mindspice.mindlib.data.geometry.IRect2;
import io.mindspice.mindlib.data.geometry.IVector2;

import java.util.BitSet;
import java.util.List;


public class KnownEntities extends Component<KnownEntities> {

    BitSet knownEntities = new BitSet(EntityManager.GET().entityCount());
    private final IMutLine2 tLeftChunk = ILine2.ofMutable(-1, -1, -1, -1);
    private final IMutLine2 tRightChunk = ILine2.ofMutable(-1, -1, -1, -1);
    private final IMutLine2 bLeftChunk = ILine2.ofMutable(-1, -1, -1, -1);
    private final IMutLine2 bRightChunk = ILine2.ofMutable(-1, -1, -1, -1);
    private final IMutLine2[] chunkList = {tLeftChunk, tRightChunk, bLeftChunk, bRightChunk};
    AreaEntity currArea;
    long activeEntitiesId = -2;

    public KnownEntities(Entity parentEntity) {
        super(parentEntity, ComponentType.KNOWN_ENTITIES, List.of());
    }

    public void onPlayerAreaChanged(Event<EventData.EntityAreaChanged> event) {
        knownEntities.clear();
        for (int i = 0; i < chunkList.length; ++i) {
            chunkList[i].setStart(-1, -1);
            chunkList[i].setEnd(-1, -1);
        }
        currArea = EntityManager.GET().areaById(event.eventArea());
        if (currArea == null) {
            throw new IllegalStateException("Area should never be null, error state entered");
        }

        long[] activeEntIds = currArea.getComponentTypeIds(ComponentType.ACTIVE_ENTITIES);
        if (activeEntIds == null) {
            throw new IllegalStateException("Area should always return an active entities component");
        }
        activeEntitiesId = activeEntIds[0];
    }

    public void onSelfViewRectChanged(Event<IRect2> event) {
        tLeftChunk.shiftLine(event.data().topLeft());
        tRightChunk.shiftLine(event.data().topRight());
        bLeftChunk.shiftLine(event.data().bottomLeft());
        bRightChunk.shiftLine(event.data().bottomRight());

        for (int i = 0; i < 4; ++i) {
            if (!newChunkContain(chunkList[i].start())) {
                emitEvent(Event.directComponentCallback(this, areaId(), ComponentType.ACTIVE_ENTITIES, currArea.entityId(), activeEntitiesId,
                        (EntityGrid ae) -> {
                            int[] activeEntities = ae.activeEntities.toArray();
                            emitEvent(Event.directComponentCallback(ae, ae.areaId(), ComponentType.KNOWN_ENTITIES, event.issuerEntityId(), event.issuerComponentId(),
                                    (KnownEntities ke) -> ke.removeKnownEntities(activeEntities)
                            ));
                        }
                ));
            }
        }
    }

    private boolean oldChunkContain(IVector2 vector) {
        for (int i = 0; i < 4; i++) {
            if (chunkList[i].start().equals(vector)) { return true; }
        }
        return false;
    }

    private boolean newChunkContain(IVector2 vector) {
        for (int i = 0; i < 4; i++) {
            if (chunkList[i].end().equals(vector)) { return true; }
        }
        return false;
    }

    public void removeKnownEntities(int[] entityIds) {
        for (int entityId : entityIds) {
            knownEntities.set(entityId, false);
        }
    }

    public void addKnownEntities(int[] entityIds) {
        for (int id : entityIds) {
            knownEntities.set(id);
        }
    }


}
