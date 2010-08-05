package org.jbehave.examples.trader.steps;

import org.jbehave.core.annotations.AfterStories;
import org.jbehave.core.annotations.AfterScenario;
import org.jbehave.core.annotations.AfterStory;
import org.jbehave.core.annotations.BeforeStories;
import org.jbehave.core.annotations.BeforeScenario;
import org.jbehave.core.annotations.BeforeStory;
import org.jbehave.core.annotations.AfterScenario.Outcome;

/**
 * Steps executed before and after each story and core
 */
public class BeforeAfterSteps {

    @BeforeStories
    public void beforeStories() {
        System.out.println("Before Stories ...");
    }

    @AfterStories
    public void afterStories() {
        System.out.println("After Stories ...");
    }

    @BeforeStory
    public void beforeStory() {
        System.out.println("Before Story ...");
    }

    @AfterStory
    public void afterStory() {
        System.out.println("After Story ...");
    }
    
    @BeforeStory(uponGivenStory=true)
    public void beforeGivenStory() {
        System.out.println("Before Given Story ...");
    }

    @AfterStory(uponGivenStory=true)
    public void afterGivenStory() {
        System.out.println("After Given Story ...");
    }
    
    @BeforeScenario
    public void beforeScenario() {
        System.out.println("Before Scenario ...");
    }

    @AfterScenario
    public void afterScenario() {
        System.out.println("After Any Scenario ...");
    }

    @AfterScenario(uponOutcome=Outcome.FAILURE)
    public void afterFailedScenario() {
        System.out.println("After Failed Scenario ...");
    }

    @AfterScenario(uponOutcome=Outcome.SUCCESS)
    public void afterSuccessfulScenario() {
        System.out.println("After Successful Scenario ...");
    }

}
