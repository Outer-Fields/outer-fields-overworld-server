package io.mindspce.outerfieldsserver.enums;

import io.mindspce.outerfieldsserver.area.AreaEntity;
import io.mindspce.outerfieldsserver.area.ChunkEntity;
import io.mindspce.outerfieldsserver.entities.LocationEntity;
import io.mindspce.outerfieldsserver.entities.PlayerEntity;
import io.mindspce.outerfieldsserver.entities.SystemEntity;
import jakarta.annotation.Nullable;

import java.util.Objects;
import java.util.function.Predicate;


public enum EntityType{
    ANY( -1, Object.class, Objects::nonNull),
    PLAYER_ENTITY( 0, Object.class, x -> x instanceof PlayerEntity),
    NON_PLAYER_ENTITY( 1, Object.class, Objects::nonNull),
    AREA_ENTITY( 2, AreaEntity.class, x -> x instanceof AreaEntity),
    LOCATION_ENTITY( 3, LocationEntity.class, x -> x instanceof LocationEntity),
    CHUNK_ENTITY( 4, ChunkEntity.class, x -> x instanceof ChunkEntity), 
    SYSTEM_ENTITY(5, SystemEntity.class , x -> x instanceof SystemEntity );

    public final int value;
    public final Class<?> dataClass;
    private final Predicate<Object> validator;

    EntityType(int value, Class<?> dataClass, Predicate<Object> validator) {
        this.value = value;
        this.dataClass = dataClass;
        this.validator = validator;
    }

    public boolean validate(Object dataObj) {
        return validator.test(dataObj);
    }

    @Nullable
    public <T> T castOrNull(Object dataObj) {
        if (validate(dataObj)) {
            @SuppressWarnings("unchecked")
            T casted = (T) dataObj;
            return casted;
        }
        return null;
    }

}
