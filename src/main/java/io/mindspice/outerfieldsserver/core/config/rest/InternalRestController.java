package io.mindspice.outerfieldsserver.core.config.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.mindspice.mindlib.util.JsonUtils;
import io.mindspice.outerfieldsserver.core.Settings;
import io.mindspice.outerfieldsserver.core.systems.CombatSystem;
import io.mindspice.outerfieldsserver.entities.PlayerEntity;
import io.mindspice.outerfieldsserver.util.Log;
import org.jctools.maps.NonBlockingHashMapLong;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;


@CrossOrigin(origins = "*", allowedHeaders = {"token", "user-agent", "content-type"}, exposedHeaders = {"token"})
@RestController
@RequestMapping("/internal")
public class InternalRestController {
    private final CombatSystem server;
    private final NonBlockingHashMapLong<PlayerEntity> playerTable;
    private final ScheduledThreadPoolExecutor gameExec;
    private final ScheduledThreadPoolExecutor botExec;

    public InternalRestController(CombatSystem CombatSystem, NonBlockingHashMapLong<PlayerEntity> playerTable,
            ScheduledExecutorService gameExecutor, ScheduledExecutorService botExecutor) {
        this.server = CombatSystem;
        this.playerTable = playerTable;
        this.gameExec = (ScheduledThreadPoolExecutor) gameExecutor;
        this.botExec = (ScheduledThreadPoolExecutor) botExecutor;
    }

    @GetMapping("/get_status")
    public ResponseEntity<String> getStatus() throws JsonProcessingException {
        try {
            String info = new JsonUtils.ObjectBuilder()
                    .put("game_server_info", server.getStatusJson())
                    .put("player_table", playerTable.values().stream().toList())
                    .put("game_exec_active_count", gameExec.getActiveCount())
                    .put("game_exec_queue_size", gameExec.getQueue().size())
                    .put("bot_exec_active_count", botExec.getActiveCount())
                    .put("bot_exec_queue_size", botExec.getQueue().size())
                    .buildString();

            return new ResponseEntity<>(info, HttpStatus.OK);
        } catch (Exception e) {
            Log.SERVER.error(this.getClass(), "get_status threw:", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/get_room_status")
    public ResponseEntity<String> getRoomInfo(@RequestBody String uuid) throws JsonProcessingException {
        try {
            JsonNode roomInfo = server.getGameRoomStatusJson(uuid);
            return new ResponseEntity<>(JsonUtils.writeString(roomInfo), HttpStatus.OK);
        } catch (Exception e) {
            Log.SERVER.error(this.getClass(), "get_room_status threw:", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("reload_config")
    public ResponseEntity<Integer> reloadConfig(@RequestBody String req) throws JsonProcessingException {
        try {
            if (JsonUtils.readTree(req).get("do_reload").asBoolean()) { Settings.reload(); }
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            Log.SERVER.error(this.getClass(), "/do_reload threw exception:", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("set_paused")
    public ResponseEntity<Integer> setPaused(@RequestBody String req) throws JsonProcessingException {
        try {
            Settings.GET().isPaused = JsonUtils.readTree(req).get("is_paused").asBoolean();
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            Log.SERVER.error(this.getClass(), "/set_paused threw exception:", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/health")
    public ResponseEntity<String> health(@RequestBody String req) throws JsonProcessingException {
        try {
            String ping = JsonUtils.readTree(req).get("ping").asText();
            return new ResponseEntity<>(JsonUtils.writeString(JsonUtils.newSingleNode("pong", ping)), HttpStatus.OK);
        } catch (Exception e) {
            Log.SERVER.error(this.getClass(), "/health threw exception:", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
