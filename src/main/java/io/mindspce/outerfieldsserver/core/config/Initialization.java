package io.mindspce.outerfieldsserver.core.config;

import io.mindspce.outerfieldsserver.area.AreaEntity;
import io.mindspce.outerfieldsserver.area.ChunkEntity;
import io.mindspce.outerfieldsserver.area.ChunkJson;
import io.mindspce.outerfieldsserver.area.TileData;
import io.mindspce.outerfieldsserver.core.networking.SocketService;
import io.mindspce.outerfieldsserver.core.networking.websockets.GameServerSocketHandler;
import io.mindspce.outerfieldsserver.core.singletons.EntityManager;
import io.mindspce.outerfieldsserver.core.systems.NPCSystem;
import io.mindspce.outerfieldsserver.core.systems.PlayerSystem;
import io.mindspce.outerfieldsserver.core.systems.WorldSystem;
import io.mindspce.outerfieldsserver.entities.PlayerEntity;
import io.mindspce.outerfieldsserver.enums.AreaId;
import io.mindspce.outerfieldsserver.util.GridUtils;
import io.mindspice.mindlib.data.geometry.IRect2;
import io.mindspice.mindlib.data.geometry.IVector2;
import org.jctools.maps.NonBlockingHashMapLong;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;


@Configuration
public class Initialization {
    EntityManager entityManager = EntityManager.GET();

    @Bean
    public NonBlockingHashMapLong<PlayerEntity> playerTable() {
        return new NonBlockingHashMapLong<>(10);
    }

    @Bean
    public SocketService socketService() {
        return new SocketService(playerTable());
    }

    @Bean
    public GameServerSocketHandler gameServerSocketHandler(
            @Qualifier("playerTable") NonBlockingHashMapLong<PlayerEntity> playerTable,
            @Qualifier("socketService") SocketService socketService) {
        return new GameServerSocketHandler(socketService, playerTable);
    }






    @Bean
    public WorldSystem worldSystem() throws IOException {

        ChunkJson chunkJson = GridUtils.parseChunkJson(new File(
                "/home/mindspice/code/Java/Okra/outer-fields-overworld-server/src/main/resources/chunkdata/chunk_0_0.json")
        );

        AreaEntity area = EntityManager.GET().newAreaEntity(
                AreaId.TEST,
                IRect2.of(0, 0, 1920, 1920),
                IVector2.of(1920, 1920),
                List.of()
        );

        Map<IVector2, TileData> chunkData = ChunkEntity.loadFromJson(chunkJson);
        ChunkEntity[][] chunkMap = new ChunkEntity[1][1];
        chunkMap[0][0] = EntityManager.GET().newChunkEntity(AreaId.TEST, IVector2.of(0, 0), chunkJson);
        area.setChunkMap(chunkMap);

        area.addCollisionToGrid(chunkJson.collisionPolys());
        //   System.out.println(EntityManager.GET().areaById(AreaId.TEST));


        return new WorldSystem(true, Map.of(AreaId.TEST, area));

    }

    @Bean
    public PlayerSystem playerSystem() {
        return new PlayerSystem();
    }

    @Bean
    public NPCSystem npcSystem() {
        return new NPCSystem();

    }




}
