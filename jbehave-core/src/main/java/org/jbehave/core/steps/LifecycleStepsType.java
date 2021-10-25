package org.jbehave.core.steps;

public enum LifecycleStepsType {

    /** 
     * Represents steps declared in Lifecycle section: composite ones and steps annotated with @Given, @When, @Then
     */
    USER,

    /** 
     * Represents steps annotated with @BeforeScenario, @AfterScenario, @BeforeStory, @AfterStory
     */
    SYSTEM;
}
