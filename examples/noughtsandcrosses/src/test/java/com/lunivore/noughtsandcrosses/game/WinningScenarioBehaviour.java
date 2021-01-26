package com.lunivore.noughtsandcrosses.game;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


class WinningScenarioBehaviour {

    @Test
    void shouldFindWinningGameWhenCoordsAreAllTheSamePlayer() {
        Map<Coord, Player> game = new HashMap<>();
        game.put(new Coord(0,0), Player.X);
        game.put(new Coord(1,0), Player.X);
        game.put(new Coord(1,1), Player.O);
        game.put(new Coord(2,2), Player.O);
        assertThat(!WinningScenario.ROW1.isAchievedIn(game), is(true));
        game.put(new Coord(2,0), Player.X);
        assertThat(WinningScenario.ROW1.isAchievedIn(game), is(true));
        assertThat(!WinningScenario.COL1.isAchievedIn(game), is(true));
        assertThat(!WinningScenario.NORTH_WEST.isAchievedIn(game), is(true));
    }
}
