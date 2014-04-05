package org.jbehave.examples.core.steps;

import org.jbehave.core.annotations.AfterScenario;
import org.jbehave.core.annotations.AfterScenario.Outcome;
import org.jbehave.core.annotations.AfterStories;
import org.jbehave.core.annotations.AfterStory;
import org.jbehave.core.annotations.BeforeScenario;
import org.jbehave.core.annotations.BeforeStories;
import org.jbehave.core.annotations.BeforeStory;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.ScenarioType;
import org.jbehave.core.annotations.Then;

/**
 * Steps executed before and after stories/story/scenario depending on type and outcome
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
    public void beforeStory(@Named("author") String author) {
        if (author.length() > 0) {
            System.out.println("This story is authored by " + author);
        } else {
            System.out.println("Before Story ...");
        }
    }

    @AfterStory
    public void afterStory(@Named("theme") String theme) {
        if (theme.length() > 0) {
            System.out.println("After Story with theme '" + theme + "'.");
        } else {
            System.out.println("After Story ...");
        }
    }

    @BeforeStory(uponGivenStory = true)
    public void beforeGivenStory() {
        System.out.println("Before Given Story ...");
    }

    @AfterStory(uponGivenStory = true)
    public void afterGivenStory() {
        System.out.println("After Given Story ...");
    }

    @BeforeScenario
    public void beforeScenario(@Named("theme") String theme) {
        if (theme.length() > 0) {
            System.out.println("Before Normal Scenario with theme: " + theme);
        } else {
            System.out.println("Before Normal Scenario ...");
        }
    }

    @BeforeScenario(uponType = ScenarioType.EXAMPLE)
    public void beforeExampleScenario() {
        System.out.println("Before Example Scenario ...");
    }

    @BeforeScenario(uponType = ScenarioType.ANY)
    public void beforeAnyScenario() {
        System.out.println("Before Any Scenario ...");
    }

    @AfterScenario(uponType = ScenarioType.NORMAL)
    public void afterScenario(@Named("variant") String variant, @Named("theme") String theme) {
        if (variant.length() > 0 && theme.length() > 0) {
            System.out.println("After Normal Scenario with variant '" + variant + "' and theme '" + theme + "'.");
        } else {
            System.out.println("After Normal Scenario with any outcome ...");
        }
    }

    @AfterScenario(uponType = ScenarioType.NORMAL, uponOutcome = Outcome.FAILURE)
    public void afterFailedScenario(@Named("theme") String theme) {
        if ("parametrisation".equals(theme)) {
            System.out.println("After Normal Scenario with failed outcome with theme 'parametrisation'.");
        } else {
            System.out.println("After Normal Scenario with failed outcome ...");
        }
    }

    @AfterScenario(uponType = ScenarioType.NORMAL, uponOutcome = Outcome.SUCCESS)
    public void afterSuccessfulScenario() {
        System.out.println("After Normal Scenario with successful outcome ...");
    }
    
    @AfterScenario(uponType = ScenarioType.EXAMPLE)
    public void afterExampleScenario() {
        System.out.println("After Example Scenario ...");
    }
    
    @AfterScenario(uponType = ScenarioType.ANY)
    public void afterAnyScenario() {
        System.out.println("After Any Scenario ...");
    }
    
    @Given("a setup")
    public void givenASetup(){
        System.out.println("Doing a setup");
    }

    @Then("a teardown")
    public void thenATeardown(){
        System.out.println("Doing a teardown");
    }
}
