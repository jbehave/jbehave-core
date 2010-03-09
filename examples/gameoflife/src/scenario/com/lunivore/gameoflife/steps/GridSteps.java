package com.lunivore.gameoflife.steps;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.jbehave.Ensure.ensureThat;

import org.jbehave.scenario.annotations.Aliases;
import org.jbehave.scenario.annotations.Given;
import org.jbehave.scenario.annotations.Then;
import org.jbehave.scenario.annotations.When;
import org.jbehave.scenario.steps.Steps;

import com.lunivore.gameoflife.domain.Game;
import com.lunivore.gameoflife.view.string.StringRenderer;

public class GridSteps extends Steps {
    
    private Game game;
    private StringRenderer renderer;

    @Given("a $width by $height game")
    @Aliases(values={"a new game: $width by $height"})
    public void theGameIsRunning(int width, int height) {
        game = new Game(width, height);
        renderer = new StringRenderer();
        game.setObserver(renderer);
    }
    
    @When("I toggle the cell at ($column, $row)")
    public void iToggleTheCellAt(int column, int row) {
        game.toggleCellAt(column, row);
    }
    
    @Then("the grid should look like $grid")
    @Aliases(values={"the grid should be $grid"})
    public void theGridShouldLookLike(String grid) {
        ensureThat(renderer.asString(), equalTo(grid));
    }

}
