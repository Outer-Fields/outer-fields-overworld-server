package io.mindspce.outerfieldsserver.entities.player;

import io.mindspce.outerfieldsserver.area.AreaInstance;
import io.mindspce.outerfieldsserver.area.ChunkData;
import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.enums.PosAuthResponse;
import io.mindspice.mindlib.data.geometry.ILine2;
import io.mindspice.mindlib.data.geometry.IVector2;
import io.mindspice.mindlib.data.geometry.QuadItem;
import org.springframework.web.socket.WebSocketSession;

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

    public void setPlayerSession(WebSocketSession session) {
        playerSession = new PlayerSession(session);
    }

    public void init(AreaInstance currArea, int startX, int startY) {
        localArea.updateCurrArea(currArea, startX, startY);
        isInit = true;
    }

    public void onPositionUpdate(int posX, int posY, long timestamp) {
//        if (!isInit) { return; }
        try {
//            System.out.println("Position: " + posX + ", " + posY);

            PosAuthResponse valid = localArea.validateUpdate(posX, posY, timestamp);
            if (valid != PosAuthResponse.VALID) {
                // TODO log
                // TODO send correction to player
                if (valid == PosAuthResponse.INVALID_COLLISION) {
                    return;
                }
            }
            localArea.updateLocalArea();
            if (localArea.isMoving()) {
                if (localArea.currChunk() == null) {
                    // TODO log this this should only happen in testing
                }
                localArea.currChunk().updateEntityPosition(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onTick(long _tickTime) {
        if (playerSession == null || !playerSession.isConnected()) { return; }

        List<QuadItem<Entity>> eUpdateList = localArea.entityUpdateList();
        ILine2[] localChunks = localArea.localChunksList();
        int lSize = 0;

        outerLoop:
        for (int i = 0; i < 4; ++i) {
            IVector2 chunkVec = localArea.localChunksList()[i].end();
            for (int j = 0; j < i; ++j) {
                // Skip if already calculated, as these can be redundant, depending on view atm
                if (chunkVec.equals(localChunks[j].end())) { continue outerLoop; }
            }

            ChunkData chunk = localArea.currArea().getChunkByIndex(chunkVec);
            if (chunk == null) { continue; }

            // This appends to the list internally when doing the grid check
            localArea.currChunk().queryEntityGrid(localArea.viewRect(), eUpdateList);

            BitSet knownEntities = localArea.knownEntitiesSet(chunkVec);
            for (int j = lSize; j < eUpdateList.size(); ++j) {
                Entity entity = eUpdateList.get(j).item();
                if (knownEntities.get(entity.id())) {
                    System.out.println("sent know entity");
                    playerSession.entityUpdateContainer().addEntity(entity, false);
                } else {
                    System.out.println("sent new entity");
                    knownEntities.set(entity.id());
                    playerSession.entityUpdateContainer().addEntity(entity, true);
                }
                lSize++;
            }
        }
        if (eUpdateList.isEmpty()) {
            System.out.println("empty");
            return;
        }
        playerSession.send(playerSession.entityUpdateContainer().getAsEntityPayLoad());
        eUpdateList.clear();
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
