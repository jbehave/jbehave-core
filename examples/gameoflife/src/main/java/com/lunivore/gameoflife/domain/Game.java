package com.lunivore.gameoflife.domain;

import java.util.HashSet;
import java.util.Set;


public class Game {

    private final int width;
    private final int height;
    private GameObserver observer;
    private Set<Cell> cells = new HashSet<Cell>();

    public Game(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void setObserver(GameObserver observer) {
        this.observer = observer;
        notifyObserver();
    }

    private void notifyObserver() {
        observer.gridChanged(new Grid() {
            public int getHeight() { return height; }
            public int getWidth() { return width; }
            public boolean hasLife(int column, int row) { return cells.contains(new Cell(column, row)); }
            
        });
    }

    public void toggleCellAt(int column, int row) {
        Cell toggled = new Cell(column, row);
        if (cells.contains(toggled)) {
            cells.remove(toggled);
        } else {
            cells.add(toggled);
        }
        notifyObserver();
    }

    public void nextGeneration() {
        throw new UnsupportedOperationException("TODO");
    }

}
