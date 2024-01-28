package io.mindspice.outerfieldsserver.networking.outgoing;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.mindspice.outerfieldsserver.entities.Entity;

import java.util.ArrayList;
import java.util.List;


public class NetEntityUpdate {
    @JsonProperty("d") public List<Entity> entities;

    public NetEntityUpdate(List<Entity> entities) {
        this.entities = entities;
    }

    public NetEntityUpdate() {
        entities = new ArrayList<>(50);
    }

}