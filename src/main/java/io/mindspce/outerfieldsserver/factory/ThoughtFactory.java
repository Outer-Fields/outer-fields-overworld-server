package io.mindspce.outerfieldsserver.factory;

import io.mindspce.outerfieldsserver.ai.decisiongraph.DecisionEventGraph;
import io.mindspce.outerfieldsserver.ai.decisiongraph.RootNode;
import io.mindspce.outerfieldsserver.ai.decisiongraph.actions.ActionEvent;
import io.mindspce.outerfieldsserver.ai.decisiongraph.decisions.PredicateNode;
import io.mindspce.outerfieldsserver.ai.task.Task;
import io.mindspce.outerfieldsserver.components.ai.ThoughtModule;
import io.mindspce.outerfieldsserver.components.npc.NPCMovement;
import io.mindspce.outerfieldsserver.core.Tick;
import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.enums.ComponentType;
import io.mindspce.outerfieldsserver.enums.ThoughtType;
import io.mindspce.outerfieldsserver.systems.EventData;
import io.mindspce.outerfieldsserver.systems.event.Event;
import io.mindspce.outerfieldsserver.systems.event.EventType;
import io.mindspice.mindlib.data.geometry.IVector2;

import java.util.List;


public class ThoughtFactory {

    public record BasicFocus(
            Entity entity
    ) { }


    public record BasicTaskData(
            IVector2 location
    ) { }


    public enum BasicEnum {
        TRAVEL
    }

    public static ThoughtModule<ThoughtType, BasicFocus> testThought(Entity entity) {

        // @formatter:off
        DecisionEventGraph<BasicFocus, ThoughtType> graph =
                new DecisionEventGraph<BasicFocus, ThoughtType>(new RootNode<>())
                .addChild(PredicateNode.of("PredicateNode", List.of((BasicFocus bf) -> bf.entity != null)))
                .addLeaf(ActionEvent.of(ThoughtType.TEST_TRAVEL));
        // @formatter:on

        ThoughtModule<ThoughtType, BasicFocus> thoughtModule = new ThoughtModule<>(entity, graph, new BasicFocus(entity), 100);

        Task<ThoughtType, BasicTaskData> moveInside = Task.builder(ThoughtType.TEST_TRAVEL, new BasicTaskData(IVector2.of(784, 766)), thoughtModule.getEventHooks())
                .addSubEventTask(EventType.NPC_ARRIVED_AT_LOC,
                        (BasicTaskData td, Event<?> event) -> {
                            EventData.NpcLocationArrival data = EventType.NPC_ARRIVED_AT_LOC.castOrNull(td);
                            return true;
                        },
                        (BasicTaskData data) -> {
                            thoughtModule.emitEvent(Event.npcTravelTo(thoughtModule, thoughtModule.areaId(),
                                    new EventData.NPCTravelTo(data.location, -1, -1, 2000, false)
                            ));
                        },
                        false)
                .build();

        Task<ThoughtType, BasicTaskData> moveOutside = Task.builder(ThoughtType.TEST_TRAVEL, new BasicTaskData(IVector2.of(672, 960)), thoughtModule.getEventHooks())
                .addSubEventTask(
                        EventType.NPC_ARRIVED_AT_LOC,
                        (BasicTaskData td, Event<?> event) -> {
                            EventData.NpcLocationArrival data = EventType.NPC_ARRIVED_AT_LOC.castOrNull(td);
                            return true;
                        },
                        (BasicTaskData data) -> {
                            thoughtModule.emitEvent(Event.npcTravelTo(thoughtModule, thoughtModule.areaId(),
                                    new EventData.NPCTravelTo(data.location, -1, -1, 2000, false)
                            ));
                        },
                        false)
                .build();

        thoughtModule.addWantingToDo(
                ThoughtType.TEST_TRAVEL,
                false,
                (Tick t) -> true,
                moveInside
        );

        thoughtModule.addWantingToDo(
                ThoughtType.TEST_TRAVEL,
                false,
                (Tick t) -> true,
                moveOutside
        );

        NPCMovement NPCMovement = ComponentType.TRAVEL_CONTROLLER.castOrNull(
                entity.getComponent(ComponentType.TRAVEL_CONTROLLER).getFirst()
        );

        if (NPCMovement == null) {
            System.out.println("Null controller");
        } else {
            thoughtModule.registerOutputHook(EventType.NPC_TRAVEL_TO, NPCMovement::onMoveTo, true);
            NPCMovement.registerOutputHook(EventType.NPC_ARRIVED_AT_LOC, thoughtModule::onEvent, false);
        }

        return thoughtModule;

    }


}
