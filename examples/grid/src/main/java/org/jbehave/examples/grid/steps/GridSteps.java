package org.jbehave.examples.grid.steps;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.jbehave.core.annotations.Aliases;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.examples.grid.domain.Game;
import org.jbehave.examples.grid.domain.StringObserver;

public class GridSteps {
    
    private Game game;
    private StringObserver renderer;

    @Given("a $width by $height game")
    @Aliases(values = { "a new game: $width by $height" })
    public void theGameIsRunning(int width, int height) {
        game = new Game(width, height);
        renderer = new StringObserver();
        game.setObserver(renderer);
    }
    
    @When("I toggle the cell at ($column, $row)")
    public void iToggleTheCellAt(int column, int row) {
        game.toggleCellAt(column, row);
    }
    
    @Then("the grid should look like $grid")
    @Aliases(values = { "the grid should be $grid" })
    public void theGridShouldLookLike(String grid) {
        assertThat(renderer.asString(), equalTo(grid));
    }

}
