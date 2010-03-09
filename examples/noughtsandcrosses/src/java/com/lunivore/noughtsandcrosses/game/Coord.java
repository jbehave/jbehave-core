/**
 * 
 */
package com.lunivore.noughtsandcrosses.game;

class Coord {
    private final int column;
    private final int row;

    public Coord(int column, int row) {
        this.column = column;
        this.row = row;
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + column;
        result = 31 * result + row;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        final Coord other = (Coord) obj;
        return column == other.column && row == other.row;
    }

}