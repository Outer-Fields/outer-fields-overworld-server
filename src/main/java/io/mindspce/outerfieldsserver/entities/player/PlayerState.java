package io.mindspce.outerfieldsserver.entities.player;

import io.mindspce.outerfieldsserver.area.AreaInstance;
import io.mindspce.outerfieldsserver.area.ChunkData;
import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.enums.PosAuthResponse;
import io.mindspce.outerfieldsserver.networking.NetSerializer;
import io.mindspce.outerfieldsserver.networking.incoming.NetMessageIn;
import io.mindspice.mindlib.data.geometry.IVector2;
import io.mindspice.mindlib.data.geometry.QuadItem;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;


public class PlayerState extends PlayerEntity {
    private PlayerSession playerSession;
    private final PlayerLocalArea localArea;

    public PlayerState(int playerId) {
        super(playerId);
        localArea = new PlayerLocalArea(this);
    }

    public void setPlayerSession(WebSocketSession session) {
        playerSession = new PlayerSession(session);
    }

    public void init(AreaInstance currArea, int startX, int startY) {
    }

    public void onPositionUpdate(int posX, int posY, long timestamp) {
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
            localArea.currChunk().updateEntityPosition(this);
        }
    }

    public void onTick(long _tickTime) {
        List<QuadItem<Entity>> eUpdateList = new ArrayList<>(50);
        int lSize = 0;
        for (int i = 0; i < 4; ++i) {
            IVector2 chunkVec = localArea.localChunksList()[i].end();
            ChunkData chunk = localArea.currArea().getChunkByIndex(chunkVec);
            if (chunk == null) { continue; }

            localArea.currChunk().queryEntityGrid(localArea.viewRect(), eUpdateList);
            BitSet knownEntities = localArea.knownEntitiesSets()[i];

            for (int j = lSize; j < eUpdateList.size(); ++j) {
                Entity entity = eUpdateList.get(j).item();
                if (knownEntities.get(entity.id())) {
                    playerSession.entityUpdateContainer().addNewEntity(entity, false);
                } else {
                    knownEntities.set(entity.id());
                    playerSession.entityUpdateContainer().addNewEntity(entity, false);
                }
                lSize++;
            }
        }
        playerSession.send(playerSession.entityUpdateContainer().getAsEntityPayLoad());

    }

    @Override
    public IVector2 globalPosition() {
        return localArea.currPosition();
    }

    @Override
    public IVector2 priorPosition() {
        return localArea.priorPosition();
    }

}
