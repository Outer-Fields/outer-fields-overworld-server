package io.mindspice.outerfieldsserver.components.npc;

import io.mindspice.mindlib.data.geometry.IRect2;
import io.mindspice.mindlib.data.geometry.IVector2;
import io.mindspice.mindlib.functional.consumers.BiPredicatedBiConsumer;
import io.mindspice.outerfieldsserver.components.Component;
import io.mindspice.outerfieldsserver.components.logic.PredicateLib;
import io.mindspice.outerfieldsserver.core.Tick;
import io.mindspice.outerfieldsserver.core.authority.PlayerAuthority;
import io.mindspice.outerfieldsserver.entities.Entity;
import io.mindspice.outerfieldsserver.enums.ComponentType;
import io.mindspice.outerfieldsserver.systems.event.Event;
import io.mindspice.outerfieldsserver.systems.event.EventType;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BooleanSupplier;


public class CharSpawnController extends Component<CharSpawnController> {
    public IRect2 spawnArea;
    public long lastDeSpawnTime = 0;
    public IVector2 respawnTimes;
    public long nextSpawnTime = 0;
    public final BooleanSupplier isActive;
    private int tickWait = 20;

    // TODO handle respawn times being null = destruction or destroying elsewhere
    public CharSpawnController(Entity parentEntity, IRect2 spawnArea,
            IVector2 respawnTimes, BooleanSupplier isActiveSupplier) {
        super(parentEntity, ComponentType.SPAWN_CONTROLLER, List.of(EventType.CHARACTER_NEW_SPAWN));
        this.spawnArea = spawnArea;
        this.respawnTimes = respawnTimes;
        this.isActive = isActiveSupplier;
        lastDeSpawnTime = Instant.now().getEpochSecond();
        if (!isActive.getAsBoolean()) {
            nextSpawnTime = lastDeSpawnTime + ThreadLocalRandom.current().nextInt(respawnTimes.x(), respawnTimes.y());
        }
        setOnTickConsumer(CharSpawnController::ticksConsumer);

        registerListener(EventType.CHARACTER_DEATH, BiPredicatedBiConsumer.of(
                PredicateLib::isRecEntitySame, CharSpawnController::onDeath)
        );
    }


    public void onDeath(Event<Integer> death) {
        emitEvent(Event.builder(EventType.ENTITY_SET_ACTIVE, this)
                .setEventArea(areaId())
                .setRecipientComponentId(entityId())
                .setData(false)
                .build()
        );
        lastDeSpawnTime = Instant.now().getEpochSecond();
        nextSpawnTime = lastDeSpawnTime + ThreadLocalRandom.current().nextInt(respawnTimes.x(), respawnTimes.y());
    }

    private void ticksConsumer(Tick tick) {
        if (--tickWait == 0) {
            tickWait = 20;
            if (isActive.getAsBoolean()) { return; }
            if (nextSpawnTime <= tick.tickTime()) {
                IVector2 position = null;
                for (int i = 0; i < 10; ++i) {
                    var pos = IVector2.of(
                            ThreadLocalRandom.current().nextInt(spawnArea.start().x(), spawnArea.start().x() + spawnArea.size().x()),
                            ThreadLocalRandom.current().nextInt(spawnArea.start().y(), spawnArea.start().y() + spawnArea.size().y())
                    );
                    if (PlayerAuthority.validateSpawn(areaId(), spawnArea, pos)) {
                        position = pos;
                        break;
                    }
                }
                if (position == null) { return; }
                emitEvent(Event.entityPositionUpdate(this, entityId(), position));
                emitEvent(Event.builder(EventType.ENTITY_SET_ACTIVE, this)
                        .setEventArea(areaId())
                        .setRecipientComponentId(entityId())
                        .setData(true)
                        .build()
                );
                emitEvent(Event.builder(EventType.CHARACTER_NEW_SPAWN, this)
                        .setEventArea(areaId())
                        .setRecipientComponentId(entityId())
                        .setData(position)
                        .build());
            }

        }
    }

}
