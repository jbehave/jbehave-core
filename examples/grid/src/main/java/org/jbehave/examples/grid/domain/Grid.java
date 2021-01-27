package org.jbehave.examples.grid.domain;

public interface Grid {

    Grid NULL = new Grid() {
        @Override
        public int getHeight() { return 0; }

        @Override
        public int getWidth() { return 0; }

        @Override
        public boolean cellToggledAt(int column, int row) { return false; }
    };

    int getWidth();

    int getHeight();

    boolean cellToggledAt(int column, int row);

}
