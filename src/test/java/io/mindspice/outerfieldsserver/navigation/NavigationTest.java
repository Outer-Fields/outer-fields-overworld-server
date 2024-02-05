package io.mindspice.outerfieldsserver.navigation;

import io.mindspice.outerfieldsserver.area.*;
import io.mindspice.outerfieldsserver.area.ChunkJson;
import io.mindspice.outerfieldsserver.area.TileData;
import io.mindspice.outerfieldsserver.core.singletons.EntityManager;
import io.mindspice.outerfieldsserver.core.systems.WorldSystem;
import io.mindspice.outerfieldsserver.entities.AreaEntity;
import io.mindspice.outerfieldsserver.entities.ChunkEntity;
import io.mindspice.outerfieldsserver.enums.AreaId;
import io.mindspice.outerfieldsserver.util.GridUtils;
import io.mindspice.mindlib.data.geometry.IRect2;
import io.mindspice.mindlib.data.geometry.IVector2;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;


public class NavigationTest {

    public WorldSystem worldSystem() throws IOException {

        ChunkJson chunkJson = GridUtils.parseChunkJson(new File(
                "/home/mindspice/code/Java/Okra/outer-fields-overworld-server/src/main/resources/chunkdata/chunk_0_0.json")
        );

        AreaEntity area = EntityManager.GET().newAreaEntity(
                AreaId.TEST,
                IRect2.of(0, 0, 1920, 1920),
                IVector2.of(1920, 1920),
                List.of(),
                10
        );

        Map<IVector2, TileData> chunkData = ChunkEntity.loadFromJson(chunkJson);
        ChunkEntity[][] chunkMap = new ChunkEntity[1][1];
        chunkMap[0][0] = EntityManager.GET().newChunkEntity(AreaId.TEST, IVector2.of(0, 0), chunkJson);
        area.setChunkMap(chunkMap);

        area.addCollisionToGrid(chunkJson.collisionPolys());
        //   System.out.println(EntityManager.GET().areaById(AreaId.TEST));
        return new WorldSystem(3423, Map.of(AreaId.TEST, area));

    }

    public static List<IVector2> interpolatePath(List<IVector2> originalPath, float speed, float deltaTime) {
        List<IVector2> interpolatedPath = new LinkedList<>();
        float stepSize = speed * deltaTime;

        for (int i = 0; i < originalPath.size() - 1; i++) {
            IVector2 start = originalPath.get(i);
            IVector2 end = originalPath.get(i + 1);

            // Add the start position
            interpolatedPath.add(start);

            float distance = start.distanceTo(end);
            int steps = Math.max(1, (int) (distance / stepSize));

            for (int step = 1; step < steps; step++) {
                float t = step / (float) steps;
                interpolatedPath.add(IVector2.lerp(start, end, t));
            }
        }

        // Add the last position
        interpolatedPath.add(originalPath.get(originalPath.size() - 1));

        return interpolatedPath;
    }

    @Test
    public void testNavigation() throws IOException, InterruptedException {
//        WorldSystem worldSystem = worldSystem();
//       List<IVector2> path =  NavCalc.getPathTo(
//                EntityManager.GET().areaById(AreaId.TEST),
//                ChunkTileIndex.of(IVector2.of(0, 0), IVector2.of(21, 30)),
//                ChunkTileIndex.of(IVector2.of(0, 0), IVector2.of(24, 23))
//        );
//
//        System.out.println(path);
//        System.out.println(interpolatePath(path, 300, 1.0f / 20));

    }
}
