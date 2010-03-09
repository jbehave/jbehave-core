package com.lunivore.noughtsandcrosses.game;

import static org.jbehave.Ensure.ensureThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;


public class WinningScenarioBehaviour {

    @Test
    public void shouldFindWinningGameWhenCoordsAreAllTheSamePlayer() {
        Map<Coord, Player> game = new HashMap<Coord, Player>();
        game.put(new Coord(0,0), Player.X);
        game.put(new Coord(1,0), Player.X);
        game.put(new Coord(1,1), Player.O);
        game.put(new Coord(2,2), Player.O);
        ensureThat(!WinningScenario.ROW1.isAchievedIn(game));
        game.put(new Coord(2,0), Player.X);
        ensureThat(WinningScenario.ROW1.isAchievedIn(game));
        ensureThat(!WinningScenario.COL1.isAchievedIn(game));
        ensureThat(!WinningScenario.NORTH_WEST.isAchievedIn(game));
    }
}
