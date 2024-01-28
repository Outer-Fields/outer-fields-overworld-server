package io.mindspice.outerfieldsserver.components.dataclasses;

import io.mindspice.mindlib.data.geometry.IMutVector2;
import io.mindspice.mindlib.data.geometry.IVector2;


public record ContainedEntity(int id, IMutVector2 position) {

    public static ContainedEntity of(int id, IVector2 pos) {
        return new ContainedEntity(id, IVector2.ofMutable(pos));
    }
}
