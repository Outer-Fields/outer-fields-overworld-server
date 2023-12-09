package io.mindspce.outerfieldsserver.datacontainers;

import io.mindspce.outerfieldsserver.area.ChunkData;
import io.mindspice.mindlib.data.geometry.IVector2;

import java.util.HashSet;


public class ActiveAreaUpdate{
    public HashSet<IVector2> removals = new HashSet<>(5);
    public HashSet<IVector2> additions  = new HashSet<>(5);;
}
