package io.mindspce.outerfieldsserver.core.config;

import io.mindspce.outerfieldsserver.area.AreaInstance;
import io.mindspce.outerfieldsserver.area.ChunkData;
import io.mindspce.outerfieldsserver.area.ChunkJson;
import io.mindspce.outerfieldsserver.core.GameServer;
import io.mindspce.outerfieldsserver.core.WorldState;
import io.mindspce.outerfieldsserver.core.networking.SocketQueue;
import io.mindspce.outerfieldsserver.core.singletons.EntityManager;
import io.mindspce.outerfieldsserver.core.statemanagers.EnemyStateManager;
import io.mindspce.outerfieldsserver.entities.locations.LocationEntity;
import io.mindspce.outerfieldsserver.entities.player.PlayerState;
import io.mindspce.outerfieldsserver.enums.AreaId;
import io.mindspce.outerfieldsserver.util.GridUtils;
import io.mindspice.mindlib.data.tuples.Pair;
import jakarta.annotation.PostConstruct;
import org.jctools.maps.NonBlockingHashMapLong;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.awt.geom.Area;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;


@Configuration
public class Initialization {

    @Bean
    public WorldState worldState(@Qualifier("areaInstance") AreaInstance areaInstance) {
        WorldState worldState = WorldState.GET();
        worldState.init(Map.of(AreaId.TEST, areaInstance));
        return worldState;
    }

    @Bean
    AreaInstance areaInstance() {
        try {
            // would loop through all chunks here
            ChunkJson chunkJson = GridUtils.parseChunkJson(new File(
                    "/home/mindspice/code/Java/Okra/outer-fields-overworld-server/src/main/resources/chunkdata/chunk_0_0.json")
            );
            ChunkData chunkData = ChunkData.loadFromJson(chunkJson);
            ChunkData[][] chunkMap = new ChunkData[1][1];
            chunkMap[0][0] = chunkData;

            AreaInstance area = new AreaInstance(AreaId.TEST, chunkMap);
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
