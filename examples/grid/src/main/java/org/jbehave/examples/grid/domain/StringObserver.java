package org.jbehave.examples.grid.domain;

public class StringObserver implements GridObserver {

    private static final String NL = System.getProperty("line.separator");
    private Grid grid = Grid.NULL;

    public String asString() {
        StringBuilder builder = new StringBuilder();
        for (int row = 0; row < grid.getHeight(); row++) {
            for (int column = 0; column < grid.getWidth(); column++) {
                builder.append(grid.cellToggledAt(column, row) ? "X" : ".");
            }
            if (row < grid.getHeight() -1) {
                builder.append(NL);
            }
        }
        return builder.toString();
    }

    @Override
    public void gridChanged(Grid grid) {
        this.grid = grid;
    }
}
