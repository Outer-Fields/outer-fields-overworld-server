package io.mindspice.outerfieldsserver.entities;

import io.mindspice.outerfieldsserver.enums.AreaId;
import io.mindspice.outerfieldsserver.enums.EntityType;
import io.mindspice.outerfieldsserver.enums.FactionType;
import io.mindspice.mindlib.data.geometry.IVector2;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class CharacterEntity extends PositionalEntity {
    protected List<FactionType> factions = new CopyOnWriteArrayList<>();
    protected volatile long inBuildingKey;

    public CharacterEntity(int id, EntityType entityType, AreaId areaId, IVector2 position) {
        super(id, entityType, areaId, position);
    }

    public void addFaction(FactionType faction) {
        factions.add(faction);
    }

    public void removeFaction(FactionType faction) {
        factions.remove(faction);
    }

    public List<FactionType> factions() { return factions; }

    public boolean isInBuilding() {
        return inBuildingKey != -1;
    }

    public long inBuildingKey() {
        return inBuildingKey;
    }


}
