package io.mindspce.outerfieldsserver.datacontainers;

import io.mindspice.mindlib.data.geometry.IVector2;

import java.util.HashSet;


public class ActiveChunkUpdate {
    public HashSet<IVector2> removals = new HashSet<>(5);
    public HashSet<IVector2> additions  = new HashSet<>(5);;
}
