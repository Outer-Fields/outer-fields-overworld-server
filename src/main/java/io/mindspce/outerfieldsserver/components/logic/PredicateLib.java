package io.mindspce.outerfieldsserver.components.logic;

import io.mindspce.outerfieldsserver.components.Component;
import io.mindspce.outerfieldsserver.systems.event.Event;


public class PredicateLib {
    public static <T extends Component<T>, U extends Event<?>> boolean isSameAreaEvent(T selfReference, U event) {
        return selfReference.areaId() == event.eventArea();
    }
}
