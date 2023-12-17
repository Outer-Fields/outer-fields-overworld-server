package io.mindspce.outerfieldsserver.systems.event;

import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.enums.AreaId;
import io.mindspce.outerfieldsserver.enums.EntityType;
import io.mindspce.outerfieldsserver.enums.EventType;
import io.mindspice.mindlib.data.geometry.IVector2;


public record EntityEvent (
        AreaId areaId,
        IVector2 ChunkIndex,
        EntityType entityType,
        int entityId
) implements Comparable<EntityEvent>{

    @Override
    public int compareTo(EntityEvent other) {
        // Compare areaId values
        int areaIdCompare = Integer.compare(this.areaId.value, other.areaId.value);
        if (areaIdCompare != 0) {
            return areaIdCompare;
        }

        // Compare ChunkIndex
        return compareChunkIndex(this.ChunkIndex, other.ChunkIndex);
    }

    private int compareChunkIndex(IVector2 a, IVector2 b) {
        if (a.x() != b.x()) {
            return Integer.compare(a.x(), b.x());
        }
        return Integer.compare(a.y(), b.y());
    }
}
