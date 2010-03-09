package com.lunivore.gameoflife.domain;

import static org.jbehave.Ensure.ensureThat;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

import com.lunivore.gameoflife.view.string.StringRenderer;

public class GameBehaviour {

    private static final String NL = System.getProperty("line.separator");

    @Test
    public void shouldStartEmpty() {
        StringRenderer renderer = new StringRenderer();
        Game game = new Game(6, 4);
        game.setObserver(renderer);
        ensureThat(renderer.asString(), CoreMatchers.equalTo(
        "......" + NL +
        "......" + NL +
        "......" + NL +
        "......"));
    }
    
    @Test
    public void shouldAllowUsersToToggleCells() {
        StringRenderer renderer = new StringRenderer();
        Game game = new Game(6, 4);
        game.setObserver(renderer);
        game.toggleCellAt(2, 1);
        ensureThat(renderer.asString(), CoreMatchers.equalTo(
        "......" + NL +
        "..X..." + NL +
        "......" + NL +
        "......"));
        game.toggleCellAt(2, 2);
        ensureThat(renderer.asString(), CoreMatchers.equalTo(
        "......" + NL +
        "..X..." + NL +
        "..X..." + NL +
        "......"));
        game.toggleCellAt(2, 1);
        ensureThat(renderer.asString(), CoreMatchers.equalTo(
        "......" + NL +
        "......" + NL +
        "..X..." + NL +
        "......"));
    }
}
