package io.mindspce.outerfieldsserver.entities;

import io.mindspce.outerfieldsserver.enums.AreaId;
import io.mindspce.outerfieldsserver.enums.EntityType;
import io.mindspce.outerfieldsserver.enums.FactionType;
import io.mindspice.mindlib.data.geometry.IVector2;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class CharacterEntity extends PositionalEntity{
    protected List<FactionType> factions = new CopyOnWriteArrayList<>();

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
}
