package com.lunivore.gameoflife.domain;

public interface Grid {

    Grid NULL = new Grid() {

        @Override
        public int getHeight() { return 0; }

        @Override
        public int getWidth() { return 0; }

        @Override
        public boolean hasLife(int column, int row) { return false; }
    };

    int getWidth();

    int getHeight();

    boolean hasLife(int column, int row);

}
