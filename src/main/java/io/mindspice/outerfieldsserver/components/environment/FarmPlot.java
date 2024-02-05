package io.mindspice.outerfieldsserver.components.environment;

import io.mindspice.mindlib.data.geometry.IRect2;
import io.mindspice.mindlib.data.tuples.Triple;
import io.mindspice.mindlib.functional.consumers.BiPredicatedBiConsumer;
import io.mindspice.outerfieldsserver.components.Component;
import io.mindspice.outerfieldsserver.components.logic.PredicateLib;
import io.mindspice.outerfieldsserver.components.player.PlayerItemsAndFunds;
import io.mindspice.outerfieldsserver.core.Tick;
import io.mindspice.outerfieldsserver.core.singletons.EntityManager;
import io.mindspice.outerfieldsserver.entities.Entity;
import io.mindspice.outerfieldsserver.entities.ItemEntity;
import io.mindspice.outerfieldsserver.enums.ComponentType;
import io.mindspice.outerfieldsserver.enums.GrowStage;
import io.mindspice.outerfieldsserver.enums.SeedType;
import io.mindspice.outerfieldsserver.systems.event.Event;
import io.mindspice.outerfieldsserver.systems.event.EventType;
import io.mindspice.outerfieldsserver.util.GridUtils;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;


public class FarmPlot extends Component<FarmPlot> {
    public final IRect2 plotSize;
    public final int seedsNeeded;
    public int ownerPlayerId = -1;

    public boolean isPlanted = false;
    public boolean isHarvestable = false;
    public GrowStage growthStage = GrowStage.UN_PLANTED;
    public long lastStageEntered = -1;
    public SeedType plantedSeed = SeedType.NONE;
    public int tickCount = 1200;

    public FarmPlot(Entity parentEntity, int ownerPlayerId, IRect2 plotSize) {
        super(parentEntity, ComponentType.FARM_PLOT, List.of());

        this.plotSize = plotSize;
        this.seedsNeeded = GridUtils.areaTileCount(plotSize);
        this.ownerPlayerId = ownerPlayerId;

        registerListener(EventType.FARM_PLANT_PLOT, BiPredicatedBiConsumer.of(
                (self, event) -> {
                    if (event.recipientEntityId() != entityId()) { return false; }
                    return ownerPlayerId == -1 || ownerPlayerId == EntityManager.GET().entityIdToPlayerId(event.issuerEntityId());
                },
                FarmPlot::onPlantPlot
        ));
        registerListener(EventType.FARM_HARVEST_PLOT, BiPredicatedBiConsumer.of(
                (FarmPlot self, Event<Integer> event) -> {
                    if (event.recipientEntityId() != entityId()) { return false; }
                    return ownerPlayerId == -1 || event.data() == ownerPlayerId;
                },
                FarmPlot::onHarvestPlot
        ));
        registerListener(EventType.FARM_UPDATE_PLOT_OWNER, BiPredicatedBiConsumer.of(
                PredicateLib::isRecEntitySame, FarmPlot::onUpdatePlotOwner
        ));

        setOnTickConsumer(FarmPlot::tickLogic);
    }

    public void tickLogic(Tick tick) {
        if (!isPlanted || isHarvestable) { return; }
        if (--tickCount <= 0) {
            if (tick.tickTime() - (lastStageEntered + plantedSeed.stageLengthSec) <= 0) {
                tickCount = 1200;
                GrowStage nextStage = growthStage.getNextStage();
                if (nextStage == GrowStage.HARVESTABLE) {
                    isHarvestable = true;
                } else {
                    lastStageEntered = tick.tickTime();
                }
            }
        }
    }

    private void plantPlot(SeedType seedType) {
        plantedSeed = seedType;
        growthStage = GrowStage.PLANTED;
        isPlanted = true;
        isHarvestable = false;
        lastStageEntered = Instant.now().getEpochSecond();
    }

    public void onPlantPlot(Event<SeedType> event) {
        if (isHarvestable || isPlanted) {
            // Emit auth event of needing harvest or is planted
            return;
        }
        emitEvent(Event.directEntityCallback(event.issuerEntityId(), ComponentType.PLAYER_ITEMS_AND_FUNDS,
                (PlayerItemsAndFunds items) -> {
                    ItemEntity<?> ownedSeeds = items.inventoryItems.get(event.data().key);
                    if (ownedSeeds == null || ownedSeeds.amount() < seedsNeeded) {
                        //emit authority response
                    } else {
                        ownedSeeds.setAmount(ownedSeeds.amount() - seedsNeeded);
                        items.emitEvent(Event.directComponentCallback(items, areaId(), ComponentType.FARM_PLOT, entityId(), componentId(),
                                (FarmPlot) -> plantPlot(event.data())
                        ));
                    }
                }
        ));
    }

    public void onHarvestPlot(Event<Integer> event) {
        if (!isHarvestable) {
            //emit authority event
            return;
        }
        isHarvestable = false;
        Map<String, Integer> harvest = plantedSeed.harvest.stream().collect(Collectors.toMap(
                Triple::first,
                h -> Math.round(ThreadLocalRandom.current().nextFloat(h.second(), h.third() * seedsNeeded))
        ));
        emitEvent(Event.playerAddInvItems(this, areaId(), event.issuerEntityId(), harvest));
    }

    public void onUpdatePlotOwner(Event<Integer> event) {
        ownerPlayerId = event.data();
    }


}
