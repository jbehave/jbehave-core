package com.lunivore.noughtsandcrosses.steps;

import org.jbehave.core.annotations.AfterScenario;
import org.jbehave.core.annotations.BeforeScenario;
import org.jbehave.core.steps.Steps;

import com.lunivore.noughtsandcrosses.util.WindowControl;

public class BeforeAndAfterSteps extends Steps {

    private final WindowControl windowControl;

    public BeforeAndAfterSteps(WindowControl windowControl) {
        this.windowControl = windowControl;
    }

    @BeforeScenario
    public void beforeScenarios() throws Exception {
    	windowControl.reset();
    }
    
    @AfterScenario
    public void afterScenarios() throws Exception {
    	windowControl.destroy();
    }
}
