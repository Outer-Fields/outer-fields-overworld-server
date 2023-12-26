package io.mindspce.outerfieldsserver.entities.nonplayer;

import io.mindspce.outerfieldsserver.entities.State;
import io.mindspce.outerfieldsserver.enums.AreaId;
import io.mindspice.mindlib.data.geometry.*;
import jakarta.annotation.Nullable;


public class NpcState extends NonPlayerEntity implements State {
    private final IAtomicVector2 position;
    private final IMutRect2 viewRect;

    public NpcState(int id, IVector2 position) {
        super(id);
        this.position = IVector2.ofAtomic(position);
        this.viewRect = IRect2.fromCenterMutable(position, IVector2.of(500, 500));
    }

    public void onTick() {

    }

    @Override
    public IVector2 globalPosition() {
        return position;
    }

    @Override @Nullable
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

    @Override
    public void onTick(long tickTime, double deltaTime) {
        // if moving do moving,
        // calculate though//
    }


}
