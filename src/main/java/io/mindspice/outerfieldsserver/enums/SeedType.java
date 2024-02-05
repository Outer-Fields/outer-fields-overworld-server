package io.mindspice.outerfieldsserver.enums;

import io.mindspice.mindlib.data.geometry.IVector2;
import io.mindspice.mindlib.data.tuples.Pair;
import io.mindspice.mindlib.data.tuples.Triple;

import javax.swing.plaf.basic.BasicGraphicsUtils;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;


public enum SeedType {
    NONE(0, "SEED_NONE", List.of(), 0),
    CHIA(1, "SEED_CHIA", List.of(Triple.of("LEAF_CHIA", 5f, 10f), Triple.of("SEED_CHIA", 0.25f, 2f)), 60 * 60 * 4);

    public final int value;
    public final String key;
    public final List<Triple<String, Float, Float>> harvest;
    public final int stageLengthSec;

    SeedType(int value, String key, List<Triple<String, Float, Float>> harvest, int stageLengthSec) {
        this.value = value;
        this.key = key;
        this.harvest = harvest;
        this.stageLengthSec = stageLengthSec;
    }

    public static SeedType fromValue(int value) {
        for (var seed : SeedType.values()) {
            if (seed.value == value) {
                return seed;
            }
        }
        return null;
    }

}
