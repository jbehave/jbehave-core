package com.lunivore.gameoflife.domain;

public class Cell {

    private final int column;
    private final int row;

    public Cell(int column, int row) {
        this.column = column;
        this.row = row;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + column;
        result = prime * result + row;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        final Cell other = (Cell) obj;
        return other.column == this.column && other.row == this.row;
    }
    
    

}
