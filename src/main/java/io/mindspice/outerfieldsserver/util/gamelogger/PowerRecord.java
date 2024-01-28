package io.mindspice.outerfieldsserver.util.gamelogger;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.mindspice.outerfieldsserver.combat.enums.PowerEnums;


public record PowerRecord(
        @JsonProperty("power_type") PowerEnums.PowerType powerType,
        @JsonProperty("chance") double chance,
        @JsonProperty("scalar") double scalar
) { }
