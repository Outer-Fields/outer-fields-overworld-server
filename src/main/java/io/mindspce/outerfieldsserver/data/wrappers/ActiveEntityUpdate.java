package io.mindspce.outerfieldsserver.data.wrappers;

import io.mindspce.outerfieldsserver.enums.EntityType;
import io.mindspice.mindlib.data.geometry.IVector2;

import java.util.ArrayList;
import java.util.List;


public record ActiveEntityUpdate(
        EntityType entityType,
        int entityId,
        IVector2 oldChunk,
        IVector2 newChunk
) { }
