package io.mindspce.outerfieldsserver.factory;

import io.mindspce.outerfieldsserver.ai.decisiongraph.DecisionEventGraph;
import io.mindspce.outerfieldsserver.ai.decisiongraph.RootNode;
import io.mindspce.outerfieldsserver.ai.decisiongraph.actions.ActionTask;
import io.mindspce.outerfieldsserver.ai.decisiongraph.decisions.PredicateNode;
import io.mindspce.outerfieldsserver.ai.task.Task;
import io.mindspce.outerfieldsserver.ai.thought.data.AttackFocus;
import io.mindspce.outerfieldsserver.components.ai.ThoughtModule;
import io.mindspce.outerfieldsserver.components.dataclasses.ContainedEntity;
import io.mindspce.outerfieldsserver.components.npc.NPCMovement;
import io.mindspce.outerfieldsserver.core.Tick;
import io.mindspce.outerfieldsserver.core.singletons.EntityManager;
import io.mindspce.outerfieldsserver.entities.CharacterEntity;
import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.entities.NonPlayerEntity;
import io.mindspce.outerfieldsserver.enums.ComponentType;
import io.mindspce.outerfieldsserver.enums.EntityType;
import io.mindspce.outerfieldsserver.enums.TaskType;
import io.mindspce.outerfieldsserver.systems.EventData;
import io.mindspce.outerfieldsserver.systems.event.Event;
import io.mindspce.outerfieldsserver.systems.event.EventType;
import io.mindspice.mindlib.data.collections.lists.CyclicList;
import io.mindspice.mindlib.data.geometry.IVector2;
import io.mindspice.mindlib.data.tuples.Pair;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Predicate;


public class ThoughtFactory {

    public record BasicFocus(
            Entity entity,
            CyclicList<IVector2> locations
    ) { }


    public record BasicTaskData(
            IVector2 location
    ) { }


    public enum BasicEnum {
        TRAVEL
    }

    public static Predicate<AttackFocus> haveTarget() {
        return (AttackFocus attackFocus) -> {
            if (System.currentTimeMillis() - attackFocus.lastTargetCheck < attackFocus.checkTimeInterval) {
                return attackFocus.trackedEntity != null;
            }
            if (attackFocus.viewRect.inView.isEmpty()) { return false; }

            ContainedEntity focusEntity = null;
            int focusDistance = Integer.MAX_VALUE;
            for (var ent : attackFocus.viewRect.inView) {
                Entity entity = EntityManager.GET().entityById(ent.id());
                if (entity.entityType() != EntityType.PLAYER || entity.entityType() != EntityType.NON_PLAYER) {
                    continue;
                }
                CharacterEntity castedEntity = (CharacterEntity) entity;
                if (attackFocus.aggressiveTowards.stream().noneMatch(castedEntity.factions()::contains)) {
                    continue;
                }
                int distanceTo = ent.position().distanceToInt(attackFocus.viewRect.viewRect.getCenter());
                if (distanceTo < focusDistance) {
                    focusEntity = ent;
                    focusDistance = distanceTo;
                }
            }
            attackFocus.trackedEntity = focusEntity;
            return focusEntity != null;
        };
    }

    public static Predicate<AttackFocus> inAttackDistance() {
        return (AttackFocus attackFocus) -> {
            var tracked = attackFocus.viewRect.inView.stream().filter(e -> e.id() == attackFocus.trackedEntity.id())
                    .findFirst();

            if (tracked.isPresent()) {
                return attackFocus.viewRect.viewRect.getCenter().distanceTo(tracked.get().position()) <= attackFocus.attackDistance;
            } else {
                return false;
            }
        };
    }

    public static ThoughtModule<AttackFocus> aggressiveEnemyThought(NonPlayerEntity entity, AttackFocus focus) {
        return null;
//        // @formatter:off
//        DecisionEventGraph<AttackFocus, ThoughtType> graph = new DecisionEventGraph<AttackFocus, ThoughtType> (new RootNode<>())
//                .addChild(PredicateNode.of("CheckForTarget", List.of(haveTarget())))
//                    .addChild(PredicateNode.of("CheckInAttackDistance", List.of(inAttackDistance())))
//                        .addLeaf(ActionEvent.of(ThoughtType.ATTACK_TARGET))
//                    .addLeaf(ActionEvent.of(ThoughtType.MOVE_TO_TARGET))
//                .addSibling(PredicateNode.of("WanderCheck", List.of((af) -> af.wanderArea != null && !af.wanderArea.size().equals(IVector2.zero()))))
//                    .addLeaf(ActionEvent.of(ThoughtType.WANDER));
//        // @formatter:on
//
//        ThoughtModule<ThoughtType, AttackFocus> thoughtModule = new ThoughtModule<>(entity, graph, focus, 5);
//
//        // Change type from object once data is defined
//        Task<ThoughtType, Object> attackTarget = Task.builder(ThoughtType.ATTACK_TARGET, new Object(), thoughtModule.getEventHooks())
//                .add
//

    }

    public static ThoughtModule< BasicFocus> testThought(Entity entity) {

        ThoughtModule <BasicFocus> thoughtModule = new ThoughtModule<>(entity, 100);
        BasicFocus bfoc = new BasicFocus(entity, new CyclicList<>(List.of(IVector2.of(784, 766), IVector2.of(672, 960))));

        Function<BasicFocus, Pair<Task<BasicTaskData>, Boolean>> travel = (BasicFocus focus) -> {
            var taskData = new BasicTaskData(focus.locations.getNext());
            Task<BasicTaskData> task = Task.builder("MoveToLocation", taskData)
                    .addSubEventTask(EventType.NPC_ARRIVED_AT_LOC,
                            (BasicTaskData td, Event<?> event) -> {
                                EventData.NpcLocationArrival data = EventType.NPC_ARRIVED_AT_LOC.castOrNull(td);
                                return true;
                            },
                            (BasicTaskData data) -> {
                                thoughtModule.emitEvent(Event.npcTravelTo(thoughtModule, thoughtModule.areaId(),
                                        new EventData.NPCTravelTo(data.location, -1, -1, 300, false)
                                ));
                            },
                            false)
                    .build();

            return Pair.of(task, false);
        };

        // @formatter:off
        DecisionEventGraph<BasicFocus> graph =
                new DecisionEventGraph<BasicFocus>(new RootNode<>())
                .addChild(PredicateNode.of("PredicateNode", List.of((BasicFocus bf) -> bf.entity != null)))
                .addLeaf(ActionTask.of("Travel", travel));
        // @formatter:on

        thoughtModule.addDecisionGraph(graph, bfoc);

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
