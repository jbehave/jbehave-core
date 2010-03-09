package com.lunivore.noughtsandcrosses.steps;

import org.jbehave.scenario.annotations.AfterScenario;
import org.jbehave.scenario.annotations.BeforeScenario;
import org.jbehave.scenario.steps.Steps;

import com.lunivore.noughtsandcrosses.util.OAndXUniverse;

public class BeforeAndAfterSteps extends Steps {

    private final OAndXUniverse universe;

    public BeforeAndAfterSteps(OAndXUniverse universe) {
        this.universe = universe;
    }

    @BeforeScenario
    public void runThisBeforeScenarios() throws Exception {
    	universe.reset();
    }
    
    @AfterScenario
    public void runThisAfterScenarios() throws Exception {
    	universe.destroy();
    }
}
