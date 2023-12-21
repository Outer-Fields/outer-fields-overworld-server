package io.mindspce.outerfieldsserver.entities.player;

import io.mindspce.outerfieldsserver.area.AreaInstance;
import io.mindspce.outerfieldsserver.area.ChunkData;
import io.mindspce.outerfieldsserver.core.GameSettings;
import io.mindspce.outerfieldsserver.core.authority.PlayerAuthority;
import io.mindspce.outerfieldsserver.core.singletons.EntityManager;
import io.mindspce.outerfieldsserver.data.wrappers.DynamicTileRef;
import io.mindspce.outerfieldsserver.entities.Entity;
import io.mindspce.outerfieldsserver.enums.EventType;
import io.mindspce.outerfieldsserver.enums.PosAuthResponse;
import io.mindspice.mindlib.data.collections.other.GridArray;
import io.mindspice.mindlib.data.geometry.*;

import java.util.*;


public class PlayerLocalArea {
    private volatile AreaInstance currArea;
    private final PlayerState playerState;

    // Local Chunks
    private final IMutLine2 tLeftChunk = ILine2.ofMutable(-1, -1, -1, -1);
    private final IMutLine2 tRightChunk = ILine2.ofMutable(-1, -1, -1, -1);
    private final IMutLine2 bLeftChunk = ILine2.ofMutable(-1, -1, -1, -1);
    private final IMutLine2 bRightChunk = ILine2.ofMutable(-1, -1, -1, -1);
    private final IMutLine2[] chunkList = {tLeftChunk, tRightChunk, bLeftChunk, bRightChunk};

    // Local Tiles
    private final GridArray<DynamicTileRef> localTileGrid = new GridArray<>(5, 5);

    // View Bounds
    private final IAtomicRect2 viewRect = IRect2.fromCenterAtomic(
            IVector2.of(0, 0), GameSettings.GET().playerViewWithBuffer()
    );
    private final IVector2[] viewRectCorners = {
            viewRect.topLeft(), viewRect.topRight(),
            viewRect.bottomLeft(), viewRect.bottomRight()
    };
    private final BitSet knownEntities = new BitSet(EntityManager.GET().entityCount());
    private final List<QuadItem<Entity>> entitiyUpdateList = new ArrayList<>(100);

    // TODO this will need set on area updates, as inner areas have a smaller chunk size
    private IVector2 chunkSize = GameSettings.GET().chunkSize();

    // Currents
    private long lastTimestamp = -1;
    private final IMutVector2 currChunk = IVector2.ofMutable(0, 0);

    // Movement Vector position (start = prior, end = current()
    final IAtomicLine2 mVector = ILine2.ofAtomic(0, 0, 0, 0);

    // TODO this needs a starting postion and then update current area needs called;
    public PlayerLocalArea(PlayerState playerState) {
        this.playerState = playerState;
        for (int x = 0; x < 5; ++x) {
            for (int y = 0; y < 5; ++y) {
                localTileGrid.set(x, y, new DynamicTileRef(
                        currArea, IVector2.of(
                        GameSettings.GET().tileSize() * (x - 2),
                        GameSettings.GET().tileSize() * (y - 2))
                ));
            }
        }
    }

    public void updateCurrArea(AreaInstance area, int posX, int posY) {
        currPosition().setXY(posX, posY);
        // clear old subscriptions if they exist
        if (currArea != null) {
            for (var chunk : chunkList) {
                currArea.unSubscribeToChunk(chunk.end(), EventType.ENTITY_UPDATE, playerState);
            }
        }
        // remove and clear old enities bitset, to be recalculated
        System.out.println("cleared known");
        knownEntities.clear();
        this.currArea = area;
        viewRect.reCenter(currPosition());
        updateChunks();
        if (currArea != null) {
            for (var chunk : chunkList) {
                currArea.subscribeToChunk(chunk.end(), EventType.ENTITY_UPDATE, playerState);
            }
        }
        ChunkData chunk = currArea.getChunkByIndex(currChunk);
        if (chunk != null) {
            chunk.addActiveEntity(playerState);
        } else {
            System.out.println("couldnt add entity, null chunk");
        }
        updateTileGridArea();
        updateTileGrid();
        calcSubscriptionChanges();
        swapToOld();
    }

    public void resetKnowEntities() {
        System.out.println("here");
        knownEntities.clear();
    }

    public PosAuthResponse validateUpdate(int posX, int posY, long currTimestamp) {
        mVector.shiftLine(posX, posY); //set end to start, start to new pos

        // validates distance, mutates the line and sets the current position (end) to max allowed distance
        // boolean invalidMove = PlayerAuthority.validateDistance(mVector, lastTimestamp, currTimestamp);

        // validates collision, mutates the line and states end to start if collision is detected
        if (!PlayerAuthority.validateCollision(currArea, localTileGrid, mVector)) {
            return PosAuthResponse.INVALID_COLLISION;
        }
        lastTimestamp = currTimestamp;
        // return invalidMove ? PosAuthResponse.INVALID_MOVEMENT : PosAuthResponse.VALID;
        return PosAuthResponse.VALID;
    }

    public void updateLocalArea() {
        viewRect.reCenter(currPosition());
        updateTileGrid();
        updateChunks();
        calcSubscriptionChanges();
        swapToOld(); // call twice
        swapToOld();
    }

    public IVector2 currPosition() {
        return mVector.end();
    }

    public IVector2 priorPosition() {
        return mVector.start();
    }

    public AreaInstance currArea() {
        return currArea;
    }

    public IRect2 viewRect() {
        return viewRect;
    }

    public boolean isMoving() {
        return !mVector.isPointLine();
    }

    public ChunkData currChunk() {
        return currArea.getChunkByIndex(currChunk);
    }

    public IMutLine2[] localChunksList() {
        return chunkList;
    }

    public List<QuadItem<Entity>> entityUpdateList() {
        return entitiyUpdateList;
    }

    public BitSet knownEntities() {
        return knownEntities;
    }

    void updateTileGrid() {
        for (int x = 0; x < 5; ++x) {
            for (int y = 0; y < 5; ++y) {
                localTileGrid.get(x, y).updatePos(currPosition());
            }
        }
    }

    void updateTileGridArea() {
        for (int x = 0; x < 5; ++x) {
            for (int y = 0; y < 5; ++y) {
                localTileGrid.get(x, y).updateAreaRef(currArea);
            }
        }
        ;
    }

    public void updateChunks() {
        for (int i = 0; i < 4; ++i) {
            chunkList[i].shiftLine(
                    viewRectCorners[i].x() / chunkSize.x(),
                    viewRectCorners[i].y() / chunkSize.y()
            );
        }
        int newChunkX = currPosition().x() / chunkSize.x();
        int newChunkY = currPosition().y() / chunkSize.y();
        if (currChunk.x() != newChunkX || currChunk.y() != newChunkY) {
            ChunkData chunk = currArea.getChunkByIndex(currChunk);
            if (chunk != null) { chunk.removeActiveEntity(playerState); }

            currChunk.setXY(newChunkX, newChunkY);
            chunk = currArea.getChunkByIndex(currChunk);
            if (chunk != null) { chunk.addActiveEntity(playerState); }
        }
    }

    public void calcSubscriptionChanges() {
        for (ILine2 oCorner : chunkList) {
            if (!oCorner.start().equals(oCorner.end())) {
                for (ILine2 iCorner : chunkList) {
                    if (!oCorner.end().equals(iCorner.start())) {
                        ChunkData chunk = currArea.getChunkByIndex(oCorner.end());
                        if (chunk == null) { continue; }
                        chunk.unsubscribe(EventType.ENTITY_UPDATE, playerState);
                        chunk.getActiveEntitiesCopy().forEach(e -> knownEntities.set(e.id(), false));
                    }
                    if (!oCorner.start().equals(iCorner.end())) {
                        ChunkData chunk = currArea.getChunkByIndex(oCorner.end());
                        if (chunk == null) { continue; }
                        chunk.subscribe(EventType.ENTITY_UPDATE, playerState);
                    }
                }
            }
        }
    }

    public void removeOldKnownEntities(ChunkData chunk) {

    }

//    public void calcSubscriptionChanges() {
//        System.out.println(Arrays.toString(chunkList));
//        for (int i = 0; i < chunkList.length; ++i) {
//            boolean isNewChunk = true;
//            boolean isRemovedChunk = true;
//
//            for (var innerChunk : chunkList) {
//                // Check if the outerChunk's new position (start) matches any other chunk's old position (end)
//                if (chunkList[i].end().equals(innerChunk.start())) {
//                    isNewChunk = false;
//                }
//                // Check if the outerChunk's old position (end) matches any other chunk's new position (start)
//                if (chunkList[i].start().equals(innerChunk.end())) {
//                    isRemovedChunk = false;
//                }
//            }
//            // If isNewChunk is true, it means this chunk's new position is not an old position of any chunk
//
//            // If isOldChunk is true, it means this chunk's old position is not a new position of any chunk
//            if (isRemovedChunk) {
//                currArea.unSubscribeToChunk(chunkList[i].start(), EventType.ENTITY_UPDATE, playerState);
//                IVector2 vec = IVector2.of(chunkList[i].start());
//                BitSet entityBitSet = knownEntities.remove(vec);
//                if (entityBitSet != null) {
//                    entityBitSet.clear();
//                    freeEntitiesSets.add(entityBitSet);
//                }
//            }
//
//            if (isNewChunk) {
//                currArea.subscribeToChunk(chunkList[i].end(), EventType.ENTITY_UPDATE, playerState);
//                if (!knownEntities.containsKey(chunkList[i].end())) {
//                    BitSet entityBitSet = freeEntitiesSets.removeLast();
//                    knownEntities.put(IVector2.of(chunkList[i].end()), entityBitSet);
//                }
//            }
//        }
//        System.out.println(Arrays.toString(chunkList));
//    }

    public void swapToOld() {
        for (int i = 0; i < 4; ++i) {
            chunkList[i].shiftLine(chunkList[i].end());
        }
    }
}
