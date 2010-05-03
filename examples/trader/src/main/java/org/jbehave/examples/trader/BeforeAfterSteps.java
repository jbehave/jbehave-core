package org.jbehave.examples.trader;

import org.jbehave.core.annotations.AfterScenario;
import org.jbehave.core.annotations.AfterStory;
import org.jbehave.core.annotations.BeforeScenario;
import org.jbehave.core.annotations.BeforeStory;

/**
 * Steps executed before and after each story and core
 */
public class BeforeAfterSteps {

    @BeforeStory
    public void beforeStory() {
        System.out.println("Before Story ...");
    }

    @AfterStory
    public void afterStory() {
        System.out.println("After Story ...");
    }
    
    @BeforeStory(uponEmbedded=true)
    public void beforeEmbeddedStory() {
        System.out.println("Before Embedded Story ...");
    }

    @AfterStory(uponEmbedded=true)
    public void afterEmbeddedStory() {
        System.out.println("After Embedded Story ...");
    }
    
    @BeforeScenario
    public void beforeScenario() {
        System.out.println("Before Scenario ...");
    }

    @AfterScenario
    public void afterScenario() {
        System.out.println("After Scenario ...");
    }
    
}
