package com.lunivore.noughtsandcrosses.game;

import java.util.Map;


public enum WinningScenario {
    ROW1(new Coord(0, 0), new Coord(1, 0), new Coord(2, 0)),
    ROW2(new Coord(0, 1), new Coord(1, 1), new Coord(2, 1)),
    ROW3(new Coord(0, 2), new Coord(1, 2), new Coord(2, 2)),
    COL1(new Coord(0, 0), new Coord(0, 1), new Coord(0, 2)),
    COL2(new Coord(1, 0), new Coord(1, 1), new Coord(1, 2)),
    COL3(new Coord(2, 0), new Coord(2, 1), new Coord(2, 2)),
    NORTH_WEST(new Coord(0, 0), new Coord(1, 1), new Coord(2, 2)),
    NORTH_EAST(new Coord(2, 0), new Coord(1, 1), new Coord(0, 2));

    private final Coord[] coords;

    private WinningScenario(Coord... coords) {
        this.coords = coords;
    }
    
    public boolean isAchievedIn(Map<Coord, Player> map) {
        Player candidate = null;
        for (Coord coord : coords) {
            if (map.get(coord) == null) {
                return false;
            } else if (candidate == null){
                candidate = map.get(coord);
            } else if (candidate != map.get(coord)){
                return false;
            }
        }
        return true;
    }

}
