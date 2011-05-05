package com.lunivore.gameoflife.view.string;

import com.lunivore.gameoflife.domain.GameObserver;
import com.lunivore.gameoflife.domain.Grid;

public class StringRenderer implements GameObserver {

    private static final String NL = System.getProperty("line.separator");
    private Grid grid = Grid.NULL;

    public String asString() {
        StringBuilder builder = new StringBuilder();
        for (int row = 0; row < grid.getHeight(); row++) {
            for (int column = 0; column < grid.getWidth(); column++) {
                builder.append(grid.hasLife(column, row) ? "X" : ".");
            }
            if (row < grid.getHeight() -1) { builder.append(NL); }
        }
        return builder.toString();
    }

    public void gridChanged(Grid grid) {
        this.grid = grid;
    }

}
