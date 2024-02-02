package io.mindspice.outerfieldsserver.systems.event;

import java.time.Instant;


public record TimedEvent(
        long time,
        Event<?> event
) {
    public static TimedEvent of(long time, Event<?> event) {
        return new TimedEvent(time, event);
    }

    public static TimedEvent ofOffsetSeconds(int offsetSecs, Event<?> event) {
        return new TimedEvent(Instant.now().getEpochSecond() + offsetSecs, event);
    }

    public static TimedEvent ofOffsetMinutes(int offsetMins, Event<?> event) {
        return new TimedEvent(Instant.now().getEpochSecond() + (60L * offsetMins), event);
    }

}
