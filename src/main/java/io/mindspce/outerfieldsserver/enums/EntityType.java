package io.mindspce.outerfieldsserver.enums;

import io.mindspce.outerfieldsserver.entities.AreaEntity;
import io.mindspce.outerfieldsserver.entities.ChunkEntity;
import io.mindspce.outerfieldsserver.entities.*;
import jakarta.annotation.Nullable;

import java.util.Objects;
import java.util.function.Predicate;


public enum EntityType {
    ANY(-1, Object.class, Objects::nonNull),
    PLAYER(0, Object.class, x -> x instanceof PlayerEntity),
    NON_PLAYER(1, Object.class, Objects::nonNull),
    AREA(2, AreaEntity.class, x -> x instanceof AreaEntity),
    LOCATION(3, LocationEntity.class, x -> x instanceof LocationEntity),
    CHUNK(4, ChunkEntity.class, x -> x instanceof ChunkEntity),
    SYSTEM(5, SystemEntity.class, x -> x instanceof SystemEntity),
    QUEST_PLAYER(6, PlayerQuestEntity.class, x -> x instanceof PlayerQuestEntity),
    QUEST_WORLD(7, WorldQuestEntity.class, x -> x instanceof WorldQuestEntity),
    ITEM(8, ItemEntity.class, x -> x instanceof ItemEntity),
    LOOT(9, LootEntity.class, x -> x instanceof LootEntity);

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


