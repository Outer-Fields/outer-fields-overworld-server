package io.mindspice.outerfieldsserver.enums;

import jakarta.annotation.Nullable;

import java.util.function.Predicate;


public enum ItemType {
    TOKEN(TokenType.class, x -> x instanceof TokenType);

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
