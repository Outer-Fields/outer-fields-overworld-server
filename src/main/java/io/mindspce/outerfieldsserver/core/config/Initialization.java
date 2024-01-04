package io.mindspce.outerfieldsserver.core.config;

import io.mindspce.outerfieldsserver.area.AreaEntity;
import io.mindspce.outerfieldsserver.area.ChunkEntity;
import io.mindspce.outerfieldsserver.area.ChunkJson;
import io.mindspce.outerfieldsserver.area.TileData;
import io.mindspce.outerfieldsserver.core.singletons.EntityManager;
import io.mindspce.outerfieldsserver.core.systems.WorldSystem;
import io.mindspce.outerfieldsserver.enums.AreaId;
import io.mindspce.outerfieldsserver.util.GridUtils;
import io.mindspice.mindlib.data.geometry.IRect2;
import io.mindspice.mindlib.data.geometry.IVector2;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.util.Map;


@Configuration
public class Initialization {
    //
    EntityManager entityManager = EntityManager.GET();

    public WorldSystem worldSystem() throws IOException {

        ChunkJson chunkJson = GridUtils.parseChunkJson(new File(
                "/home/mindspice/code/Java/Okra/outer-fields-overworld-server/src/main/resources/chunkdata/chunk_0_0.json")
        );
        Map<IVector2, TileData> chunkData = ChunkEntity.loadFromJson(chunkJson);
        ChunkEntity[][] chunkMap = new ChunkEntity[1][1];
        chunkMap[0][0] = EntityManager.GET().newChunkEntity(AreaId.TEST, IVector2.of(1, 1), chunkJson);

        AreaEntity area = EntityManager.GET().newAreaEntity(
                AreaId.TEST,
                chunkMap,
                IRect2.of(0, 0, 1920, 1920),
                IVector2.of(1920, 1920)
        );

        area.addCollisionToGrid(chunkJson.collisionPolys());

        WorldSystem worldSystem = new WorldSystem(true, Map.of(AreaId.TEST, area));
        entityManager.registerSystem(worldSystem);
        return worldSystem;
    }
}
