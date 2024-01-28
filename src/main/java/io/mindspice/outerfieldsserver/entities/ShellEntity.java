package io.mindspice.outerfieldsserver.entities;

import io.mindspice.mindlib.data.geometry.IVector2;
import io.mindspice.outerfieldsserver.components.Component;
import io.mindspice.outerfieldsserver.enums.AreaId;
import io.mindspice.outerfieldsserver.enums.EntityType;

import java.util.List;


public class ShellEntity extends Entity{
    public ShellEntity(int id) {
        super(id, EntityType.ANY, AreaId.NONE);
    }
}
