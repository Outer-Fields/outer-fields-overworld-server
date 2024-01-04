package io.mindspce.outerfieldsserver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.mindspce.outerfieldsserver.area.ChunkJson;
import io.mindspce.outerfieldsserver.components.AreaMonitor;
import io.mindspce.outerfieldsserver.core.GameSettings;
import io.mindspce.outerfieldsserver.core.ServerConst;
import io.mindspce.outerfieldsserver.core.singletons.EntityManager;
import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.enums.Direction;
import io.mindspce.outerfieldsserver.networking.outgoing.NetMessage;
import io.mindspce.outerfieldsserver.systems.event.Event;
import io.mindspce.outerfieldsserver.util.GridUtils;
import io.mindspice.mindlib.data.geometry.IRect2;
import io.mindspice.mindlib.data.geometry.IVector2;
import io.mindspice.mindlib.util.JsonUtils;
import org.junit.jupiter.api.Test;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;


//@SpringBootTest
class OuterFieldsServerApplicationTests {






    @Test
    void eventTest() {

    }


    @Test
    void parseTest() throws IOException {

        ChunkJson chunkJson = JsonUtils.getMapper()
                .readValue(new File("/home/mindspice/code/Java/Okra/outer-fields-overworld-server/src/main/resources/chunkdata/chunk_0_0.json"), ChunkJson.class);

        System.out.println(chunkJson);

    }


    @Test
    void gridTest() {
        IVector2[][] grid = new IVector2[3][3];
        for (int x = 0; x < 3; ++x) {
            for (int y = 0; y < 3; ++y) {
                grid[x][y] = IVector2.of(x, y);
            }
        }
        var newX = Direction.WEST;
        var newY = Direction.NORTH;
        int x = newX == Direction.EAST ? 0 : 2;
        for (int y = 0; y < 3; ++y) {
            grid[x][y] = IVector2.of(-9, -9);
        }

        int y = newY == Direction.SOUTH ? 0 : 2;
        for (int j = 0; j < 3; ++j) {
            grid[j][y] = IVector2.of(-9, -9);
        }

        GridUtils.printGrid(grid);


    }

    @Test
    void sleepTest() {
        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();

        List<Long> arr = new ArrayList<>(600);
        AtomicLong lastTime = new AtomicLong(System.nanoTime());
        Runnable run = () -> {
            var time = System.nanoTime();
            arr.add(time - lastTime.get());
            lastTime.set(time);
            if (arr.size() % 100 == 0) {
                System.out.println(arr.stream().mapToLong(Long::longValue).average());
            }
        };
        exec.scheduleAtFixedRate(run, 0, ServerConst.NANOS_IN_SEC / GameSettings.GET().tickRate(), TimeUnit.NANOSECONDS);

        while (true) {

        }
    }

    @Test
    void jacksonTest() throws JsonProcessingException, InterruptedException {
        JsonUtils.setFailOnUnknownProperties(true);
        var writer = new ObjectMapper().writerFor(NetMessage.class);
        List<Long> times = new ArrayList<>(1000000);
        List<byte[]> json = new ArrayList<>(1000000);
        for (int i = 0; i < 100000; ++i) {

//            PlayerEntity e = new PlayerEntity();
//            e.test = String.valueOf(ThreadLocalRandom.current().nextInt());
//            NetEntityUpdate neu = new NetEntityUpdate(List.of(e));
//            NetMessage<?> nm = new NetMessage<>(NetMsgType.EntityUpdate, neu);
//            long t = System.nanoTime();
//            var j = JsonUtils.writePretty((Entity)e);
//            System.out.println(j);
            // json.add(j);
        }
        Thread.sleep(10000);
        for (int i = 0; i < 100; ++i) {
//
//            PlayerEntity e = new PlayerEntity();
//            e.test = String.valueOf(ThreadLocalRandom.current().nextInt());
//            e.id = ThreadLocalRandom.current().nextInt();
//            e.globalPos = IVector2.of(ThreadLocalRandom.current().nextInt(), ThreadLocalRandom.current().nextInt());
//            NetEntityUpdate<PlayerEntity> neu = new NetEntityUpdate<>(EntityType.PLAYER, List.of(e));
//            NetMessage<?> nm = new NetMessage<>(NetMsgType.EntityUpdate, neu);
//            long t = System.nanoTime();
//            var j = JsonUtils.writeBytes(nm);
//            times.add(System.nanoTime() - t);
//            json.add(j);
        }
        System.out.println(times.stream().mapToLong(Long::longValue).average());
        System.out.println(json.size());
    }

}
