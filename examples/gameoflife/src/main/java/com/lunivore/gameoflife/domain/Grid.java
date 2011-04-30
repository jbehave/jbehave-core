package com.lunivore.gameoflife.domain;

public interface Grid {

    Grid NULL = new Grid() {

        public int getHeight() { return 0; }

        public int getWidth() { return 0; }

        public boolean hasLife(int column, int row) { return false; }
    };

    int getWidth();

    int getHeight();

    boolean hasLife(int column, int row);

}
