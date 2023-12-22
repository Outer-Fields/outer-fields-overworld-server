package io.mindspce.outerfieldsserver.entities.player;

import io.mindspce.outerfieldsserver.area.AreaInstance;
import io.mindspce.outerfieldsserver.core.WorldState;
import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.enums.AreaId;
import io.mindspce.outerfieldsserver.enums.PosAuthResponse;
import io.mindspce.outerfieldsserver.networking.NetSerializer;
import io.mindspice.mindlib.data.geometry.IVector2;
import io.mindspice.mindlib.data.geometry.QuadItem;
import io.mindspice.mindlib.util.Utils;

import java.util.BitSet;
import java.util.List;


public class PlayerState extends PlayerEntity {
    private PlayerSession playerSession;
    private final PlayerLocalArea localArea;
    private volatile boolean isInit = false;

    public PlayerState(int playerId) {
        super(playerId);
        localArea = new PlayerLocalArea(this);
    }

    public void setPlayerSession(PlayerSession session) {
        localArea.resetKnowEntities();
        playerSession = session;
    }

    public void init(PlayerSession playerSession, AreaId id, int startX, int startY) {
        this.playerSession = playerSession;
        AreaInstance areaInstance = WorldState.GET().getAreaTable().get(id);
        localArea.updateCurrArea(areaInstance, startX, startY);
        areaInstance.addActivePlayer(this);
        areaInstance.addEntityToGrid(IVector2.of(startX, startY), this);
        isInit = true;
    }

    public void onPositionUpdate(int posX, int posY, long timestamp) {
//        if (!isInit) { return; }


        try {

            //      System.out.println("ID:" + id() + " Position: " + posX + ", " + posY);

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
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onTick(long _tickTime) {
        ;
        if (playerSession == null || !playerSession.isConnected()) { return; }
        var t = System.nanoTime();
        List<QuadItem<Entity>> eUpdateList = localArea.entityUpdateList();
        BitSet knownEntities = localArea.knownEntities();
        localArea.currArea().queryEntityGrid(localArea.viewRect(), eUpdateList);

        for (var quadItem : eUpdateList) {
            if (quadItem.item().id() == id.get()) { continue; }
            System.out.println("sending data for id:" + id() + "data_id:" + quadItem.item().id());

            if (knownEntities.get(quadItem.item().id())) {
                //  System.out.println("sent know entity");
                System.out.println("sending known data for id:" + id() + "  data_id:" + quadItem.item().id());

                playerSession.entityUpdateContainer().addEntity(quadItem.item(), false);
            } else {
                System.out.println("sending new data for id:" + id() + " data_id:" + quadItem.item().id());

                Utils.printThreadMethod();
                System.out.println(knownEntities);
                System.out.println(quadItem.item().id());
                knownEntities.set(quadItem.item().id());
                playerSession.entityUpdateContainer().addEntity(quadItem.item(), true);
            }
        }
        playerSession.submitMsg(playerSession.entityUpdateContainer().getAsEntityPayLoad());
        eUpdateList.clear();

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
    public IVector2 globalPosition() {
        return localArea.currPosition();
    }

    @Override
    public IVector2 priorPosition() {
        return localArea.priorPosition();
    }

    @Override
    public int hashCode() {
        return playerId();
    }
}
