package io.mindspce.outerfieldsserver.core.config;

import io.mindspce.outerfieldsserver.area.AreaEntity;
import io.mindspce.outerfieldsserver.area.ChunkEntity;
import io.mindspce.outerfieldsserver.area.ChunkJson;
import io.mindspce.outerfieldsserver.core.GameServer;
import io.mindspce.outerfieldsserver.core.WorldState;
import io.mindspce.outerfieldsserver.core.networking.SocketQueue;
import io.mindspce.outerfieldsserver.core.singletons.EntityManager;
import io.mindspce.outerfieldsserver.entities.locations.LocationEntity;
import io.mindspce.outerfieldsserver.entities.player.PlayerState;
import io.mindspce.outerfieldsserver.enums.AreaId;
import io.mindspce.outerfieldsserver.util.GridUtils;
import io.mindspice.mindlib.data.tuples.Pair;
import org.jctools.maps.NonBlockingHashMapLong;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;


@Configuration
public class Initialization {

    @Bean
    public WorldState worldState(@Qualifier("areaInstance") AreaEntity areaEntity) {
        WorldState worldState = WorldState.GET();
        worldState.init(Map.of(AreaId.TEST, areaEntity));
        return worldState;
    }

    @Bean
    AreaEntity areaInstance() {
        try {
            // would loop through all chunks here
            ChunkJson chunkJson = GridUtils.parseChunkJson(new File(
                    "/home/mindspice/code/Java/Okra/outer-fields-overworld-server/src/main/resources/chunkdata/chunk_0_0.json")
            );
            ChunkEntity chunkEntity = ChunkEntity.loadFromJson(chunkJson);
            ChunkEntity[][] chunkMap = new ChunkEntity[1][1];
            chunkMap[0][0] = chunkEntity;

            AreaEntity area = new AreaEntity(AreaId.TEST, chunkMap);
            area.addCollisionToGrid(chunkJson.collisionPolys());
            area.addLocationToGrid(chunkJson.areaRects().entrySet().stream().map(
                    e -> Pair.of(
                            e.getValue(),
                            (LocationEntity) EntityManager.GET().newLocationState(ThreadLocalRandom.current().nextInt(10), e.getKey())
                    )).toList());
            return area;
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Bean
    NonBlockingHashMapLong<PlayerState> playerTableInstance() {
        return new NonBlockingHashMapLong<>(50);
    }

    @Bean
    GameServer gameServerInstance(
            @Qualifier("playerTableInstance") NonBlockingHashMapLong<PlayerState> playerTable
    ) {
        return new GameServer(playerTable);
    }

    @Bean
    SocketQueue socketInQueue(
            @Qualifier("playerTableInstance") NonBlockingHashMapLong<PlayerState> playerTable
    ) {
        return new SocketQueue(playerTable);
    }
}
