package io.mindspice.outerfieldsserver.enums;

import io.mindspice.outerfieldsserver.entities.ItemEntity;
import jakarta.annotation.Nullable;

import java.util.function.Predicate;
import java.util.function.Supplier;


public enum ItemType {
    TOKEN(TokenType.class, x -> x instanceof TokenType),
    CLOTHING(ClothingItem.class, x -> x instanceof ClothingItem);

    public final Class<?> itemClass;
    public final Predicate<Object> validator;

    ItemType(Class<?> itemClass, Predicate<Object> validator) {
        this.itemClass = itemClass;
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
