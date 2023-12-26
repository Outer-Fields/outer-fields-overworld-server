package io.mindspce.outerfieldsserver.entities.nonplayer;

import io.mindspce.outerfieldsserver.enums.AreaId;
import io.mindspice.mindlib.data.geometry.IVector2;


public class EnemyState extends NonPlayerEntity {
    public EnemyState(int id) {
        super(id);
    }

    @Override
    public IVector2 globalPosition() {
        return null;
    }

    @Override
    public IVector2 priorPosition() {
        return null;
    }

    @Override
    public AreaId currentArea() {
        return null;
    }

    @Override
    public IVector2 chunkIndex() {
        return null;
    }
}
