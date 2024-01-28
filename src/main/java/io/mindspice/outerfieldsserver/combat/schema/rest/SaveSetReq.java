package io.mindspice.outerfieldsserver.combat.schema.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.mindspice.outerfieldsserver.combat.schema.PawnLoadOut;


public record SaveSetReq(
        @JsonProperty("set_num") int setNum,
        @JsonProperty("set_name") String setName,
        @JsonProperty("pawn_loadouts") PawnLoadOut[] pawnLoadOut
) {

    public SaveSetReq {
        if (pawnLoadOut.length != 3) { throw new IllegalStateException(); }
    }
}
