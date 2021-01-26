package com.lunivore.gameoflife.domain;

import com.lunivore.gameoflife.view.string.StringRenderer;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;

class GameBehaviour {

    private static final String NL = System.getProperty("line.separator");

    @Test
    void shouldStartEmpty() {
        StringRenderer renderer = new StringRenderer();
        Game game = new Game(6, 4);
        game.setObserver(renderer);
        assertThat(renderer.asString(), Matchers.equalTo(
        "......" + NL +
        "......" + NL +
        "......" + NL +
        "......"));
    }
    
    @Test
    void shouldAllowUsersToToggleCells() {
        StringRenderer renderer = new StringRenderer();
        Game game = new Game(6, 4);
        game.setObserver(renderer);
        game.toggleCellAt(2, 1);
        assertThat(renderer.asString(), Matchers.equalTo(
        "......" + NL +
        "..X..." + NL +
        "......" + NL +
        "......"));
        game.toggleCellAt(2, 2);
        assertThat(renderer.asString(), Matchers.equalTo(
        "......" + NL +
        "..X..." + NL +
        "..X..." + NL +
        "......"));
        game.toggleCellAt(2, 1);
        assertThat(renderer.asString(), Matchers.equalTo(
        "......" + NL +
        "......" + NL +
        "..X..." + NL +
        "......"));
    }
}
