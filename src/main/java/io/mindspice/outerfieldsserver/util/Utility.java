package io.mindspice.outerfieldsserver.util;

import io.mindspice.outerfieldsserver.core.networking.incoming.NetPlayerActionMsg;
import io.mindspice.outerfieldsserver.enums.EntityType;
import io.mindspice.outerfieldsserver.enums.NetPlayerAction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


public class Utility {
    public static long msToNano(int ms) {
        return ms * 1000000L;
    }

    public static <T> List<T> mapEntities(List<?> entities, EntityType type) {
        List<T> rtnList = new ArrayList<>(entities.size());
        entities.forEach(e -> {
            T casted = type.castOrNull(e);
            if (casted != null) {
                rtnList.add(casted);
            }
        });
        return rtnList;
    }


}
