package io.mindspce.outerfieldsserver.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.mindspice.mindlib.data.geometry.IVector2;


public abstract class Entity {
    @JsonProperty("i") public int id;
    @JsonProperty("p") public IVector2 globalPos;


    public IVector2 getGlobalPos() {
        return globalPos;
    }

    public int getId() {
        return id;
    }
}
