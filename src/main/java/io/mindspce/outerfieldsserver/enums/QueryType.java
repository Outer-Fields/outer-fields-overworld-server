package io.mindspce.outerfieldsserver.enums;

import gnu.trove.set.TIntSet;
import io.mindspice.mindlib.data.geometry.IVector2;
import jakarta.annotation.Nullable;


public enum QueryType {
    CHUNK_ACTIVE_PLAYERS(TIntSet.class, IVector2.class),
    AREA_ACTIVE_PLAYERS(TIntSet.class, AreaId.class)
    ;

    private final Class<?> responseDataClass;
    private final Class<?> queryDataClass;

    QueryType(Class<?> responseDataClass, Class<?> queryDataClass) {
        this.responseDataClass = responseDataClass;
        this.queryDataClass = queryDataClass;
    }

    public Class<?> responseDataClass() {
        return responseDataClass;
    }

    public Class<?> queryDataClass() {
        return queryDataClass;
    }

    @Nullable
    public <T> T castResponseData(T response) {
        if (responseDataClass.isInstance(response)) {
            @SuppressWarnings("unchecked") // Safe cast after isInstance check
            T castedComponent = (T) response;
            return castedComponent;
        }
        return null;
    }


}
