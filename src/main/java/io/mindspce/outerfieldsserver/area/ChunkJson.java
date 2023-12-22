package io.mindspce.outerfieldsserver.area;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.mindspice.mindlib.data.geometry.IPolygon2;
import io.mindspice.mindlib.data.geometry.IVector2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AreaJson {
    private final IVector2 index;
    private final List<IVector2> areaMask = new ArrayList<>(1000);
    private final List<IVector2> navMask = new ArrayList<>(1000);
    private final List<IVector2> collisionMask = new ArrayList<>(1000);
    private final Map<String, IPolygon2> areaRects = new HashMap<>(50);
    private final List<IPolygon2> collisionPolys = new ArrayList<>(25);

    public AreaJson(
            @JsonProperty("chunk_index_x") int indexX,
            @JsonProperty("chunk_index_y") int indexY,
            @JsonProperty("area_masks") int[] areaMask,
            @JsonProperty("navigation_masks") int[] navMask,
            @JsonProperty("collision_maks") int[] collisionMask,
            @JsonProperty("area_rects") HashMap<String, List<Integer>> areaRect,
            @JsonProperty("collision_polys") List<List<Integer>> collisionPolys) {

        this.index = IVector2.of(indexX, indexY);
        for (int i = 0; i < areaMask.length; i += 2) {
            this.areaMask.add(IVector2.of(areaMask[i], areaMask[i + 1]));
        }

        for (int i = 0; i < navMask.length; i += 2) {
            this.navMask.add(IVector2.of(navMask[i], navMask[i + 2]));
        }

        for (int i = 0; i < collisionMask.length; i += 2) {
            this.collisionMask.add(IVector2.of(collisionMask[i], collisionMask[i + 2]));
        }

        for (var entry : areaRect.entrySet()) {
            this.areaRects.put(entry.getKey(), IPolygon2.of(entry.getValue()));
        }

        for (var polyArr : collisionPolys) {
            this.collisionPolys.add(IPolygon2.of(polyArr));
        }
    }

    public IVector2 index() {
        return index;
    }

    public List<IVector2> areaMask() {
        return areaMask;
    }

    public List<IVector2> navMask() {
        return navMask;
    }

    public List<IVector2> collisionMask() {
        return collisionMask;
    }

    public Map<String, IPolygon2> areaRects() {
        return areaRects;
    }

    public List<IPolygon2> collisionPolys() {
        return collisionPolys;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AreaJson: ");
        sb.append("\n  index: ").append(index);
        sb.append(",\n  areaMask: ").append(areaMask);
        sb.append(",\n  navMask: ").append(navMask);
        sb.append(",\n  collisionMask: ").append(collisionMask);
        sb.append(",\n  areaRects: ").append(areaRects);
        sb.append(",\n  collisionPolys: ").append(collisionPolys);
        sb.append("\n");
        return sb.toString();
    }
}
