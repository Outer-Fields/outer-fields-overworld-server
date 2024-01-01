package io.mindspce.outerfieldsserver.entities.player;

import io.mindspce.outerfieldsserver.area.AreaEntity;
import io.mindspce.outerfieldsserver.components.Component;
import io.mindspce.outerfieldsserver.components.GlobalPosition;
import io.mindspce.outerfieldsserver.core.WorldState;
import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.entities.PositionalEntity;
import io.mindspce.outerfieldsserver.enums.AreaId;
import io.mindspce.outerfieldsserver.enums.ComponentType;
import io.mindspce.outerfieldsserver.enums.EntityType;
import io.mindspce.outerfieldsserver.systems.EventData;
import io.mindspce.outerfieldsserver.systems.event.*;
import io.mindspce.outerfieldsserver.networking.NetSerializer;
import io.mindspice.mindlib.data.geometry.IVector2;
import io.mindspice.mindlib.data.geometry.QuadItem;
import io.mindspice.mindlib.util.Utils;

import java.util.BitSet;
import java.util.List;
import java.util.function.Consumer;


public class PlayerState extends PositionalEntity implements EventListener<PlayerState> {// implements EventListener<PlayerState> {
    private PlayerSession playerSession;
    private final PlayerLocalArea localArea;
    //private final ListenerCache<PlayerState> listeners = new ListenerCache<>();
    private volatile boolean isInit = false;

    public PlayerState(int entityId, int playerId) {
        super(entityId, EntityType.PLAYER, AreaId.TEST);
        localArea = new PlayerLocalArea(this);
    }

    public void setPlayerSession(PlayerSession session) {
        localArea.resetKnowEntities();
        playerSession = session;

    }

    public void init(PlayerSession playerSession, AreaId id, int startX, int startY, List<Component<?>> components) {
        this.playerSession = playerSession;
        AreaEntity areaEntity = WorldState.GET().getAreaTable().get(id);
        localArea.updateCurrArea(areaEntity, startX, startY);
        areaEntity.addActivePlayer(this);
        areaEntity.addEntityToGrid(IVector2.of(startX, startY), this);
        isInit = true;
        components.forEach(c -> {
            attachedComponents.set(c.componentType().ordinal());
            c.getEmitedEvents().forEach(e -> listeningFor.set(e.ordinal()));
        });
        Component<?> posComponent = getComponent(ComponentType.GLOBAL_POSITION);
        if (posComponent == null) {
            System.out.println("position Component not found for PlayerState");
            // todo log this;
            return;
        }
    }

    @Override
    protected void chunkChangedCallBack(EventData.EntityChunkChanged entityChunkChanged) {
        this.chunkIndex = entityChunkChanged.newChunk();
    }

    @Override
    protected void areaChangedCallBack(EventData.EntityAreaChanged entityAreaChanged) {
        this.areaId = entityAreaChanged.newArea();
    }

    public void onPositionUpdate(int posX, int posY, long timestamp) {
//        if (!isInit) { return; }

        var test = new GlobalPosition(null, null, null, null);
        test.addConsumer(gp -> {
            gp.lastPosition().add(2, 2);
        });

        test.handleCallBack(c -> c.);
        long t = System.nanoTime();
        try {
            //      System.out.println("ID:" + id() + " position: " + posX + ", " + posY);

            boolean valid = localArea.validateUpdate(posX, posY, timestamp);
            if (!valid) {
                playerSession.submitMsg(NetSerializer.getPosAuthCorrection(localArea.currPosition()));
            }

            localArea.updateLocalArea();
            if (localArea.isMoving()) {
                if (localArea.currChunk() == null) {
                    // TODO log this this should only happen in testing
                }

                localArea.currArea().updateGridEntity(localArea.mVector, this);
                Event.Emit.newPlayerPosition(this, globalPosition());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Input Time: " + (System.nanoTime() - t));
    }

    double fullQueryDelta = 0.0;

    public void onTick(long _tickTime, double deltaTime) {

        long t = System.nanoTime();
        fullQueryDelta += deltaTime;
        if (playerSession == null || !playerSession.isConnected()) { return; }
        if (deltaTime < 1.5) {

            return;

        }

        deltaTime = 0.0;

        List<QuadItem<Entity>> eUpdateList = localArea.entityUpdateList();
        BitSet knownEntities = localArea.knownEntities();
        localArea.currArea().queryEntityGrid(localArea.viewRect(), eUpdateList);

        for (var quadItem : eUpdateList) {
            if (quadItem.item().entityId() == id) { continue; }
            System.out.println("sending tileData for id:" + entityId() + "data_id:" + quadItem.item().entityId());

            if (knownEntities.get(quadItem.item().entityId())) {
                //  System.out.println("sent know entity");
                System.out.println("sending known tileData for id:" + entityId() + "  data_id:" + quadItem.item().entityId());

                playerSession.entityUpdateContainer().addEntity(quadItem.item(), false);
            } else {
                System.out.println("sending new tileData for id:" + entityId() + " data_id:" + quadItem.item().entityId());

                Utils.printThreadMethod();
                System.out.println(knownEntities);
                System.out.println(quadItem.item().entityId());
                knownEntities.set(quadItem.item().entityId());
                playerSession.entityUpdateContainer().addEntity(quadItem.item(), true);
            }
        }
        playerSession.submitMsg(playerSession.entityUpdateContainer().getAsEntityPayLoad());
        eUpdateList.clear();
        System.out.println("Output Time: " + (System.nanoTime() - t));

//        outerLoop:
//        for (int i = 0; i < 4; ++i) {
//            IVector2 chunkVec = localArea.localChunksList()[i].end();
//            for (int j = 0; j < i; ++j) {
//                // Skip if already calculated, as these can be redundant, depending on view atm
//                if (chunkVec.equals(localChunks[j].end())) { continue outerLoop; }
//            }
//
//            ChunkData chunk = localArea.currArea().getChunkByIndex(chunkVec);
//            if (chunk == null) { continue; }
//
//            // This appends to the list internally when doing the grid check
//            localArea.currChunk().queryEntityGrid(localArea.viewRect(), eUpdateList);
//
//            BitSet knownEntities = localArea.knownEntitiesSet(chunkVec);
//            for (int j = lSize; j < eUpdateList.size(); ++j) {
//                Entity entity = eUpdateList.get(j).item();
//                if (knownEntities.get(entity.id())) {
//                    //  System.out.println("sent know entity");
//                    playerSession.entityUpdateContainer().addEntity(entity, false);
//                } else {
//                    //  System.out.println("sent new entity");
//                    knownEntities.set(entity.id());
//                    playerSession.entityUpdateContainer().addEntity(entity, true);
//                }
//                lSize++;
//            }
//        }
//
//        if (eUpdateList.isEmpty()) {
//            System.out.println("empty");
//            return;
//        }
//        playerSession.submitMsg(playerSession.entityUpdateContainer().getAsEntityPayLoad());
//        ;
//        eUpdateList.clear();

    }

    @Override
    public void onEvent(Event<?> event) {

    }

    @Override
    public void onCallBack(Consumer<PlayerState> consumer) {

    }

    @Override
    public boolean isListenerFor(EventType eventType) {
        return false;
    }


}
