package io.mindspice.outerfieldsserver.systems.event;

import io.mindspice.mindlib.data.collections.lists.primative.IntList;
import io.mindspice.outerfieldsserver.area.TileData;
import io.mindspice.outerfieldsserver.entities.Entity;
import io.mindspice.outerfieldsserver.entities.ItemEntity;
import io.mindspice.outerfieldsserver.enums.AreaId;
import io.mindspice.outerfieldsserver.enums.EntityState;
import io.mindspice.mindlib.data.geometry.IPolygon2;
import io.mindspice.mindlib.data.geometry.IRect2;
import io.mindspice.mindlib.data.geometry.IVector2;
import io.mindspice.mindlib.data.tuples.Pair;
import io.mindspice.outerfieldsserver.enums.TokenType;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class EventData {

    private EventData() { }

    public record EntityAreaChanged(boolean isPlayer, AreaId oldArea, AreaId newArea, IVector2 position) { }


    public record EntityChunkChanged(boolean isPlayer, IVector2 oldChunk, IVector2 newChunk) { }


    public record AreaEntered(boolean isPlayer, int enteredEntity) { }


    public record EntityPositionChanged(boolean isPlayer, IVector2 oldPosition, IVector2 newPosition) { }


    public record EntityStateUpdate(boolean clearExisting, List<EntityState> stateAdditions, List<EntityState> statesRemovals) { }


    public record ViewRectUpdate(boolean isPlayer, IRect2 viewRect) { }


    public record LocalAreaChanged(boolean isPlayer, Set<IVector2> localChunks) { }


    public record NewPositionalEntity(AreaId area, IVector2 position, Entity entity) { }


    public record TileDataUpdate(AreaId areaId, IVector2 chunkIndex, List<Pair<IVector2, TileData>> tileData, boolean isRemoval) { }


    public record CollisionData(boolean isRemoved, IPolygon2 poly) { }


    public record CompletableEvent<T, U>(Event<T> mainEvent, Event<U> completionEvent) { }


    public record EntitySerialization(int[] ids, ByteBuffer buffer) { }


    public record NPCLocationArrival(long locationKey, int locationId) { }


    public record NPCTravelTo(IVector2 locationPos, long locationKey, int locationId, int speed, boolean overRideExisting) { }


    public record CombatInit(int playerEntityId, int enemyEntityId) { }


    public record CharacterDeath(int deadEntityId, int killerEntityId) { }


    public record FundsAndItems(
            Map<TokenType, Integer> bankedTokens,
            Map<TokenType, Integer> inventoryTokens,
            Map<Long, ItemEntity<?>> bankedItems,
            Map<Long, ItemEntity<?>> inventoryItems
    ) { }


    public record TokensAndItems(Map<TokenType, Integer> tokens, Map<Long, ItemEntity<?>> items) {
        public TokensAndItems {
            if (tokens == null) { tokens = Map.of(); }
            if (items == null) { items = Map.of(); }
        }
    }


    public record EntityVisibility(boolean isActive, boolean isInvisibleToAll, IntList visibleTo, IntList inVisibleTo) { }


    public record VisibilityUpdate(
            boolean invisibleToAll,
            boolean visibleToAll,
            IntList visibleIds,
            boolean visibleIsRemoval,
            IntList inVisibleIds,
            boolean inVisibleIsRemoval
    ) {
        public static VisibilityUpdate newVisibleToAll() {
            return new VisibilityUpdate(false, true, IntList.of(), false, IntList.of(), false);
        }

        public static VisibilityUpdate newInvisibleToAll() {
            return new VisibilityUpdate(true, false, IntList.of(), false, IntList.of(), false);
        }

        public static VisibilityUpdate addVisibleTo(IntList visibleIds) {
            return new VisibilityUpdate(false, false, visibleIds, false, IntList.of(), false);
        }

        public static  VisibilityUpdate removeVisibleTo(IntList visibleIds) {
            return new VisibilityUpdate(false, false, visibleIds, true, IntList.of(), false);
        }

        public static  VisibilityUpdate addInvisibleTo(IntList invisibleIds) {
            return new VisibilityUpdate(false, false, IntList.of(), false, invisibleIds, false);
        }

        public static VisibilityUpdate removeInvisibleTo(IntList invisibleIds) {
            return new VisibilityUpdate(false, false, IntList.of(), false, invisibleIds, true);
        }

    }


}
