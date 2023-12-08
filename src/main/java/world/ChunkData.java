package world;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.mindspice.mindlib.util.JsonUtils;


public record ChunkData(
        String id,
        String n,
        String s,
        String e,
        String w,
        String ne,
        String nw,
        String se,
        String sw
) {

    public byte[] asJsonBytes() throws JsonProcessingException {
        return JsonUtils.writeBytes(this);
    }

    public String asJsonString() throws JsonProcessingException {
        return JsonUtils.writeString(this);
    }

}
