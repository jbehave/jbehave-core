package com.lunivore.gameoflife.domain;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

public class CellBehaviour {

    @Test
    public void shouldBeEqualToCellsWithSameRowAndColumn() {
        Cell cell = new Cell(3, 4);
        Cell sameCell = new Cell(3, 4);
        Cell otherCell = new Cell(2, 5);
        
        assertThat(cell, equalTo(sameCell));
        assertThat(otherCell, not(equalTo(sameCell)));
        
        assertThat(cell.hashCode(), equalTo(sameCell.hashCode()));
    }
}
