package io.mindspice.outerfieldsserver.enums;

public enum GrowStage {
    UN_PLANTED,
    PLANTED,
    SPROUTED,
    MID_GROWTH,
    LATE_GROWTH,
    HARVESTABLE;


    public GrowStage getNextStage() {
        if (this == HARVESTABLE) { return HARVESTABLE; }
        return GrowStage.values()[this.ordinal() + 1];
    }

}
