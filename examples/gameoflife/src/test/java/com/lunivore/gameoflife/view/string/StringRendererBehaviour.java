package com.lunivore.gameoflife.view.string;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import com.lunivore.gameoflife.domain.Grid;

public class StringRendererBehaviour {

    private static final String NL = System.getProperty("line.separator");

    @Test
    public void shouldRenderCellsAsCrosses() {
        StringRenderer renderer = new StringRenderer();
        Grid grid = mock(Grid.class);
        when(grid.getWidth()).thenReturn(5);
        when(grid.getHeight()).thenReturn(6);
        when(grid.hasLife(3, 4)).thenReturn(true);
        renderer.gridChanged(grid);
        
        assertThat(renderer.asString(), equalTo(
                "....." + NL +
                "....." + NL +
                "....." + NL +
                "....." + NL +
                "...X." + NL +
                "....."
                ));
    }
    
}
