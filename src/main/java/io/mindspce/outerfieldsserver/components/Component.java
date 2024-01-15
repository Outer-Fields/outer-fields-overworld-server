package io.mindspce.outerfieldsserver.components;

import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.enums.AreaId;
import io.mindspce.outerfieldsserver.enums.ComponentType;
import io.mindspce.outerfieldsserver.enums.EntityType;
import io.mindspce.outerfieldsserver.enums.SystemType;
import io.mindspce.outerfieldsserver.systems.event.*;
import io.mindspce.outerfieldsserver.systems.event.EventListener;
import io.mindspice.mindlib.util.DebugUtils;

import java.util.*;


public abstract class Component<T extends Component<T>> extends ListenerCache<T> implements EventListener<T> {
    protected final Entity parentEntity;
    public final ComponentType componentType;
    protected final long id = UUID.randomUUID().getMostSignificantBits();
    protected String componentName;
    protected SystemType registeredWith = SystemType.NONE;

    public Component(Entity parentEntity, ComponentType componentType,
            List<EventType> emittedEvents) {
        super(emittedEvents);
        this.parentEntity = parentEntity;
        this.componentType = componentType;
        this.componentName = parentEntity.entityType() + ":" + parentEntity.entityId() + ":" + componentType.name() + ":" + id;
    }

    public T withComponentName(String name) {
        this.componentName += ":" + name;
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
        return componentName;
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

    public void setRegisteredWith(SystemType systemType) {
        if (registeredWith != SystemType.NONE) {
            System.out.println("attempted to register component twice");
            DebugUtils.printStackTrace();
            throw new RuntimeException("Attempted to register component twice");
        }
        registeredWith = systemType;
    }

    public SystemType registeredWith() {
        return registeredWith;
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


}