package io.mindspce.outerfieldsserver.networking.outgoing;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.mindspce.outerfieldsserver.enums.EntityType;
import io.mindspce.outerfieldsserver.enums.NetMsgType;
import io.mindspce.outerfieldsserver.entities.Entity;

import java.util.List;


public class NetEntityUpdate<T extends Entity<T>> {
    @JsonProperty("t") int netMsgType = NetMsgType.EntityUpdate.value;
    @JsonProperty("dt") int dataType;
    @JsonProperty("d") List<T> entities;

    public NetEntityUpdate(EntityType entityType, List<T > entities) {
        this.dataType = entityType.value;
        this.entities = entities;
    }

    public void clear() {
        entities.clear();
    }

    public void add(T entity) {
        entities.add(entity);
    }

    @JsonProperty("t")
    public int getNetMsgType() {
        return netMsgType;
    }

    @JsonProperty("dt")
    public int getDataType() {
        return dataType;
    }

    @JsonProperty("t")
    public List<T> getEntities() {
        return entities;
    }
}