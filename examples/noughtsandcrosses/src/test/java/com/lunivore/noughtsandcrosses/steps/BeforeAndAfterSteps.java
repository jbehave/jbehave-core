package com.lunivore.noughtsandcrosses.steps;

import org.jbehave.core.annotations.AfterScenario;
import org.jbehave.core.annotations.BeforeScenario;
import org.jbehave.core.steps.Steps;

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
