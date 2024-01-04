package io.mindspce.outerfieldsserver.components;

import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.enums.AreaId;
import io.mindspce.outerfieldsserver.enums.ComponentType;
import io.mindspce.outerfieldsserver.enums.EntityType;
import io.mindspce.outerfieldsserver.systems.event.*;
import io.mindspce.outerfieldsserver.systems.event.EventListener;

import java.util.*;


public abstract class Component<T extends Component<T>> extends ListenerCache<T> implements EventListener<T> {
    protected final Entity parentEntity;
    protected final ComponentType componentType;
    protected final long id = UUID.randomUUID().getLeastSignificantBits();
    protected String componenetName;

    public Component(Entity parentEntity, ComponentType componentType,
            List<EventType> emittedEvents) {
        super(emittedEvents);
        this.parentEntity = parentEntity;
        this.componentType = componentType;
        this.componenetName = parentEntity.entityType() + ":" + parentEntity.entityId() + ":" + componentType.name() + ":" + id;
    }

    public T withComponentName(String name) {
        this.componenetName += ":" + name;
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
    public String componentName() {
        return componenetName;
    }

    @Override
    public int entityId() {
        return parentEntity().entityId();
    }

    @Override
    public String entityName() {
        return parentEntity().name();
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
        return parentEntity.entityType();
    }

//    @Override
//    public void onTick(Tick tickEvent) {
//        try {
//            @SuppressWarnings("unchecked")
//            T self = (T) this;
//            handleTick(self, tickEvent);
//        } catch (Exception e) {
//            //TODO log this
//        }
//    }
//
//    @Override
//    public void onEvent(Event<?> event) {
//        try {
//            if (event.eventType() == EventType.CALLBACK) {
//                @SuppressWarnings("unchecked")
//                Event<CallBack<T>> consumer = (Event<CallBack<T>>) event.data();
//                handleCallBack(consumer);
//            } else {
//                @SuppressWarnings("unchecked")
//                T self = (T) this;
//                handleEvent(self, event);
//            }
//        } catch (Exception e) {
//            //TODO log this
//        }
//    }


    public interface callback {
        void accept();
    }


}