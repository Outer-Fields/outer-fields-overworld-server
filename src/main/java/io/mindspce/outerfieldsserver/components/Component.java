package io.mindspce.outerfieldsserver.components;

import io.mindspce.outerfieldsserver.core.Tick;
import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.enums.AreaId;
import io.mindspce.outerfieldsserver.enums.ComponentType;
import io.mindspce.outerfieldsserver.enums.EntityType;
import io.mindspce.outerfieldsserver.systems.EventData;
import io.mindspce.outerfieldsserver.systems.event.*;
import io.mindspce.outerfieldsserver.systems.event.EventListener;

import java.util.*;


public abstract class Component<T extends Component<T>> extends ListenerCache<T> implements EventListener<T> {
    protected final Entity parentEntity;
    protected final ComponentType componentType;
    protected final long id = UUID.randomUUID().getLeastSignificantBits();
    protected String name;

    public Component(Entity parentEntity, ComponentType componentType,
            List<EventType> emittedEvents) {
        super(emittedEvents);
        this.parentEntity = parentEntity;
        this.componentType = componentType;
        this.name = parentEntity.entityType() + ":" + parentEntity.entityId() + ":" + componentType.name() + ":" + id;
    }

    public T withName(String name) {
        this.name += ":" + name;
        @SuppressWarnings("unchecked")
        T with = (T) this;
        return with;
    }

    public Entity parentEntity() {
        return parentEntity;
    }

    public ComponentType componentType() {
        return componentType;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public int entityId() {
        return parentEntity().entityId();
    }

    @Override
    public long componentId() {
        return id;
    }

    @Override
    public AreaId areaId() {
        return parentEntity.areaId();
    }

    public EntityType entityType() {
        return entityType();
    }

    @Override
    public void onTick(Tick tickEvent) {
        try {
            @SuppressWarnings("unchecked")
            T self = (T) this;
            handleTick(self, tickEvent);
        } catch (Exception e) {
            //TODO log this
        }
    }

    @Override
    public void onEvent(Event<?> event) {
        try {
            if (event.eventType() == EventType.CALLBACK) {
                @SuppressWarnings("unchecked")
                CallBack<T> data = (CallBack<T>) event.data();
                handleCallBack(data);
            } else {
                @SuppressWarnings("unchecked")
                T self = (T) this;
                handleEvent(self, event);
            }
        } catch (Exception e) {
            //TODO log this
        }
    }

    @Override
    public void onQuery(Event<EventData.Query<?, ?, ?>> event) {
        @SuppressWarnings("unchecked")
        T self = (T) this;
        handleQuery(self, event);
    }

    public void handleCallBack(CallBack<T> callBack) {
        try {
            @SuppressWarnings("unchecked")
            T self = (T) this;
            callBack.callback().accept(self);
        } catch (Exception e) {
            //TODO log this
        }
    }

    public interface query {
        void accept();
    }

    public boolean isEventAreaSame(Event<?> event) {
        return event.eventArea() == parentEntity.areaId();
    }

}