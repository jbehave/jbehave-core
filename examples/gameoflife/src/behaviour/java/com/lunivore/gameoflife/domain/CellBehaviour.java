package com.lunivore.gameoflife.domain;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.jbehave.Ensure.ensureThat;

import org.junit.Test;

public class CellBehaviour {

    @Test
    public void shouldBeEqualToCellsWithSameRowAndColumn() {
        Cell cell = new Cell(3, 4);
        Cell sameCell = new Cell(3, 4);
        Cell otherCell = new Cell(2, 5);
        
        ensureThat(cell, equalTo(sameCell));
        ensureThat(otherCell, not(equalTo(sameCell)));
        
        ensureThat(cell.hashCode(), equalTo(sameCell.hashCode()));
    }
}
