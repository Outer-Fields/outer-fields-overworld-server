package io.mindspice.outerfieldsserver.factory;

import io.mindspice.outerfieldsserver.ai.decisiongraph.DecisionEventGraph;
import io.mindspice.outerfieldsserver.ai.decisiongraph.NewActionTask;
import io.mindspice.outerfieldsserver.ai.decisiongraph.RootNode;
import io.mindspice.outerfieldsserver.ai.decisiongraph.actions.ActionTask;
import io.mindspice.outerfieldsserver.ai.decisiongraph.decisions.PredicateNode;
import io.mindspice.outerfieldsserver.ai.task.Task;
import io.mindspice.outerfieldsserver.ai.thought.data.AttackFocus;
import io.mindspice.outerfieldsserver.area.TileData;
import io.mindspice.outerfieldsserver.components.ai.ThoughtModule;
import io.mindspice.outerfieldsserver.components.dataclasses.ContainedEntity;
import io.mindspice.outerfieldsserver.components.npc.NPCMovement;
import io.mindspice.outerfieldsserver.components.player.ViewRect;
import io.mindspice.outerfieldsserver.core.singletons.EntityManager;
import io.mindspice.outerfieldsserver.entities.*;
import io.mindspice.outerfieldsserver.entities.NonPlayerEntity;
import io.mindspice.outerfieldsserver.entities.PlayerEntity;
import io.mindspice.outerfieldsserver.enums.ComponentType;
import io.mindspice.outerfieldsserver.enums.EntityType;
import io.mindspice.outerfieldsserver.enums.FactionType;
import io.mindspice.outerfieldsserver.systems.event.EventData;
import io.mindspice.outerfieldsserver.systems.event.Event;
import io.mindspice.outerfieldsserver.systems.event.EventType;
import io.mindspice.mindlib.data.collections.lists.CyclicList;
import io.mindspice.mindlib.data.geometry.IVector2;
import io.mindspice.outerfieldsserver.entities.CharacterEntity;
import io.mindspice.outerfieldsserver.entities.Entity;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
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
                if (entity.entityType() != EntityType.PLAYER && entity.entityType() != EntityType.NON_PLAYER) {
                    continue;
                }
                if (!attackFocus.isAggressiveTowards((CharacterEntity) entity)) {
                    System.out.println("Not aggressive towards");
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
                boolean canAttack = attackFocus.viewRect.viewRect.getCenter()
                        .distanceTo(tracked.get().position()) <= attackFocus.attackDistance;

                if (canAttack) {
                    attackFocus.movementController.resetMovement();
                    return true;
                }
            }
            return false;

        };
    }

    public static Predicate<AttackFocus> canWander() {
        return (AttackFocus attackFocus) -> {
            if (attackFocus.movementController.isMoving()) {
                attackFocus.nextWanderTime = System.currentTimeMillis() +
                        ThreadLocalRandom.current().nextInt(
                                attackFocus.wanderInterval.x(),
                                attackFocus.wanderInterval.y()
                        );
                return false;
            }
            if (System.currentTimeMillis() < attackFocus.nextWanderTime) { return false; }
            return attackFocus.wanderArea != null && !attackFocus.wanderArea.size().equals(IVector2.zero());
        };
    }

    public static ThoughtModule<AttackFocus> aggressiveEnemyThought(NonPlayerEntity entity) {

        record AttackData(boolean isPlayer, int entityId, int playerId) { }
        ThoughtModule<AttackFocus> thoughtModule = new ThoughtModule<>(entity, 5);

        Function<AttackFocus, NewActionTask<AttackData>> doAttack = (af) -> {
            CharacterEntity targetEntity = (CharacterEntity) EntityManager.GET().entityById(af.trackedEntity.id());
            AttackData data = new AttackData(
                    targetEntity.entityType() == EntityType.PLAYER,
                    targetEntity.entityId(),
                    targetEntity.entityType() == EntityType.PLAYER
                            ? ((PlayerEntity) targetEntity).playerId()
                            : targetEntity.entityId()
            );

            Task<AttackData> task = Task.builder("AttackEntity", data)
                    .addSubEventTask(EventType.CHARACTER_OUTFIT_CHANGED,
                            (aData, event) -> true,
                            (ignored) -> System.out.println("attacking"),
                            false)
                    .build();
            return NewActionTask.of(task, true);
        };

        Function<AttackFocus, NewActionTask<IVector2>> trackPlayer = (af) -> {
            IVector2 pos = IVector2.of(af.trackedEntity.position());

            Task<IVector2> task = Task.builder("TrackTarget", pos)
                    .addSubEventTask(EventType.NPC_ARRIVED_AT_LOC,
                            (p, d) -> true,
                            (p) -> {
                                System.out.println("Tracking Player To: " + p);
                                thoughtModule.emitEvent(Event.npcTravelTo(thoughtModule, thoughtModule.areaId(),
                                        new EventData.NPCTravelTo(p, -1, -1, 200, true)));
                            },
                            true)
                    .build();
            af.trackedEntity = null;
            return NewActionTask.of(task, true);
        };

        Function<AttackFocus, NewActionTask<IVector2>> wander = (af) -> {
            var wa = af.wanderArea;
            IVector2 pos = IVector2.ofMutable(
                    ThreadLocalRandom.current().nextInt(wa.start().x(), wa.start().x() + wa.size().x()),
                    ThreadLocalRandom.current().nextInt(wa.start().y(), wa.start().y() + wa.size().y())
            );
            TileData tiledata;
            for (int i = 0; i < 400; ++i) {
                tiledata = af.area.getTileByGlobalPosition(pos);
                if (tiledata != null && tiledata.isNavigable()) {
                    break;
                } else {
                    pos.setXY(
                            ThreadLocalRandom.current().nextInt(wa.start().x(), wa.start().x() + wa.size().x()),
                            ThreadLocalRandom.current().nextInt(wa.start().y(), wa.start().y() + wa.size().y())
                    );
                }
            }

            Task<IVector2> task = Task.builder("Wandering", pos)
                    .addSubEventTask(EventType.NPC_ARRIVED_AT_LOC,
                            (p, d) -> true,
                            (p) -> {
                                System.out.println("Wandering To: " + p);
                                thoughtModule.emitEvent(Event.npcTravelTo(thoughtModule, thoughtModule.areaId(),
                                        new EventData.NPCTravelTo(p, -1, -1, 50, false)));
                            },
                            true)
                    .build();
            return new NewActionTask<>(task, true);
        };
        // @formatter:off
        DecisionEventGraph<AttackFocus> graph = new DecisionEventGraph<AttackFocus> (new RootNode<>())
                .addChild(PredicateNode.of("CheckForTarget", List.of(haveTarget())))
                    .addChild(PredicateNode.of("CheckInAttackDistance", List.of(inAttackDistance())))
                        .addLeaf(ActionTask.of("AttackTarget", doAttack))
                    .addSibling(PredicateNode.of("NotInIdstance", (ignored) -> true))
                        .addLeaf(ActionTask.of("MoveToTarget", trackPlayer))
                    .stepBack()
                .addSibling(PredicateNode.of("WanderCheck", canWander()))
                    .addLeaf(ActionTask.of("Wandering", wander));
        // @formatter:on
        System.out.println(graph.getRoot().printGraph(1));

        ViewRect npcView = ComponentType.VIEW_RECT.castOrNull(
                entity.getComponent(ComponentType.VIEW_RECT).getFirst()
        );
        NPCMovement npcMovement = ComponentType.NPC_MOVEMENT.castOrNull(
                entity.getComponent(ComponentType.NPC_MOVEMENT).getFirst()
        );

        if (npcView == null || npcMovement == null) {
            System.out.println("Null NPCMovement or ViewRect");
        }

        AttackFocus attackFocus = new AttackFocus(
                npcMovement,
                entity.areaId().entity,
                npcView,
                List.of(FactionType.PLAYER),
                IVector2.of(672, 960),
                IVector2.of(1000, 1000),
                2000,
                32,
                IVector2.of(1000, 10_000));

        thoughtModule.addDecisionGraph(graph, attackFocus);

        NPCMovement NPCMovement = ComponentType.NPC_MOVEMENT.castOrNull(
                entity.getComponent(ComponentType.NPC_MOVEMENT).getFirst()
        );

        if (NPCMovement == null) {
            System.out.println("Null controller");
        } else {
            thoughtModule.registerOutputHook(EventType.NPC_TRAVEL_TO, NPCMovement::onMoveTo, true);
            NPCMovement.registerOutputHook(EventType.NPC_ARRIVED_AT_LOC, thoughtModule::onEvent, false);
        }
        return thoughtModule;
    }

    public static ThoughtModule<BasicFocus> testThought(Entity entity) {

        ThoughtModule<BasicFocus> thoughtModule = new ThoughtModule<>(entity, 100);
        BasicFocus bfoc = new BasicFocus(entity, new CyclicList<>(List.of(IVector2.of(784, 766), IVector2.of(672, 960))));

        Function<BasicFocus, NewActionTask<BasicTaskData>> travel = (BasicFocus focus) -> {
            var taskData = new BasicTaskData(focus.locations.getNext());
            Task<BasicTaskData> task = Task.builder("MoveToLocation", taskData)
                    .addSubEventTask(EventType.NPC_ARRIVED_AT_LOC,
                            (BasicTaskData td, Event<?> event) -> {
                                EventData.NPCLocationArrival data = EventType.NPC_ARRIVED_AT_LOC.castOrNull(td);
                                return true;
                            },
                            (BasicTaskData data) -> {
                                thoughtModule.emitEvent(Event.npcTravelTo(thoughtModule, thoughtModule.areaId(),
                                        new EventData.NPCTravelTo(data.location, -1, -1, 50, false)
                                ));
                            },
                            false)
                    .build();

            return NewActionTask.of(task, false);
        };

        // @formatter:off
        DecisionEventGraph<BasicFocus> graph =
                new DecisionEventGraph<BasicFocus>(new RootNode<>())
                .addChild(PredicateNode.of("PredicateNode", List.of((BasicFocus bf) -> bf.entity != null)))
                .addLeaf(ActionTask.of("Travel", travel));
        // @formatter:on

        thoughtModule.addDecisionGraph(graph, bfoc);

        NPCMovement NPCMovement = ComponentType.NPC_MOVEMENT.castOrNull(
                entity.getComponent(ComponentType.NPC_MOVEMENT).getFirst()
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
