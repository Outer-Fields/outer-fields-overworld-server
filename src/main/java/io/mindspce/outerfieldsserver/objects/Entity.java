package io.mindspce.outerfieldsserver.objects;

import io.mindspice.mindlib.data.geometry.IVector2;


public abstract class Entity<T extends Entity<T>> {
    public int id;
    public IVector2 globalPos;

    public abstract T asEntity();

    public IVector2 getGlobalPos() {
        return globalPos;
    }
}
