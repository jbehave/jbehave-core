package org.jbehave.examples.trader.steps;

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
 * Steps executed before and after stories/story/scenario
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
            System.out.println("Before scenario with theme: " + theme);
        } else {
            System.out.println("Before Scenario ...");
        }
    }

    @BeforeScenario(uponType = ScenarioType.EXAMPLE)
    public void beforeExampleScenario() {
        System.out.println("Before Example Scenario ...");
    }

    @AfterScenario
    public void afterScenario(@Named("variant") String variant, @Named("theme") String theme) {
        if (variant.length() > 0 && theme.length() > 0) {
            System.out.println("After scenario with variant '" + variant + "' and theme '" + theme + "'.");
        } else {
            System.out.println("After Any Scenario ...");
        }
    }

    @AfterScenario(uponOutcome = Outcome.FAILURE)
    public void afterFailedScenario(@Named("theme") String theme) {
        if ("parametrisation".equals(theme)) {
            System.out.println("Wow, something failed in a scenario with theme 'parametrisation'.");
        } else {
            System.out.println("After Failed Scenario ...");
        }
    }

    @AfterScenario(uponOutcome = Outcome.SUCCESS)
    public void afterSuccessfulScenario() {
        System.out.println("After Successful Scenario ...");
    }

    @AfterScenario(uponType = ScenarioType.EXAMPLE)
    public void afterExampleScenario() {
        System.out.println("After Example Scenario ...");
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
