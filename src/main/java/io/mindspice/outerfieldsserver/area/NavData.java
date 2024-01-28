package io.mindspice.outerfieldsserver.area;

import io.mindspice.outerfieldsserver.enums.NavLocation;
import io.mindspice.outerfieldsserver.enums.NavPath;

import java.util.HashSet;
import java.util.Set;


public class NavData {
    Set<NavPath> paths;
    Set<NavLocation> locations;

    public NavData() { }

    public NavData(Set<NavPath> paths, Set<NavLocation> locations) {
        this.paths = paths;
        this.locations = locations;
    }

    public NavData(Set<NavPath> paths) {
        this.paths = paths;
    }

    public void addPath(NavPath path) {
        if (paths == null) {
            paths = new HashSet<>();
        }
        paths.add(path);
    }

    public void removePath(NavPath path) {
        paths.remove(path);
    }

    public void addLocation(NavLocation navLocation) {
        if (locations == null) {
            locations = new HashSet<>();
        }
        locations.add(navLocation);
    }

    public void removeLocation(NavLocation navLocation) {
        locations.remove(navLocation);
    }

    public boolean pathOf(NavPath path) {
        return paths.contains(path);
    }

    public boolean locationOf(NavLocation navLocation) {
        return locations.contains(navLocation);
    }
}
