package io.mindspce.outerfieldsserver.entities.quest;

import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.enums.AreaId;
import io.mindspce.outerfieldsserver.enums.EntityType;
import io.mindspce.outerfieldsserver.systems.event.Event;
import io.mindspce.outerfieldsserver.systems.event.EventType;
import io.mindspce.outerfieldsserver.systems.event.TickListener;
import io.mindspice.mindlib.data.geometry.IVector2;

import java.util.function.Consumer;


public class QuestEntity extends Entity implements TickListener {
    Consumer<QuestEntity> onTickMethod;
    protected boolean test = false;
    public boolean test2;

    public QuestEntity(int id, EntityType entityType, Consumer<QuestEntity> questEntityConsumer) {
        super(id, entityType);
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


    @Override
    public void onTick(long tickTime, double delta) {
        onTickMethod.accept(this);
    }
}
