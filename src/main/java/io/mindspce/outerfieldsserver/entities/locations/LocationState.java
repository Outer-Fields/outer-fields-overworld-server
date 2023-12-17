package io.mindspce.outerfieldsserver.entities.locations;

import io.mindspice.mindlib.data.geometry.IVector2;


public class LocationState extends LocationEntity{
    public LocationState(int key) {
        super(key);
    }

    @Override
    public IVector2 globalPosition() {
        return null;
    }

    @Override
    public IVector2 priorPosition() {
        return null;
    }
}
