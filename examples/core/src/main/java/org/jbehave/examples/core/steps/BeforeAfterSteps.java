package org.jbehave.examples.core.steps;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.concurrent.atomic.AtomicInteger;

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

    private final AtomicInteger invokeCounter = new AtomicInteger();
    private final ThreadLocal<AtomicInteger> nestedInvokeCounter = ThreadLocal
            .withInitial(() -> new AtomicInteger(invokeCounter.get()));

    @BeforeStories(order = 1)
    public void beforeStoriesOrdered() {
        System.out.println("Before Stories with higher order");
        assertThat(invokeCounter.incrementAndGet(), equalTo(1));
    }

    @BeforeStories
    public void beforeStories() {
        System.out.println("Before Stories ...");
        assertThat(invokeCounter.incrementAndGet(), equalTo(2));
    }

    @AfterStories
    public void afterStories() {
        System.out.println("After Stories ...");
        assertThat(invokeCounter.decrementAndGet(), equalTo(1));
    }

    @AfterStories(order = 1)
    public void afterStoriesOrdered() {
        System.out.println("After Stories with higher order");
        assertThat(invokeCounter.decrementAndGet(), equalTo(0));
    }

    @BeforeStory
    public void beforeStory(@Named("author") String author) {
        if (author.length() > 0) {
            System.out.println("This story is authored by " + author);
        } else {
            System.out.println("Before Story ...");
        }
        assertThat(nestedInvokeCounter.get().incrementAndGet(), equalTo(4));
    }

    @BeforeStory(order = 1)
    public void beforeStoryOrdered() {
        System.out.println("Before Story with higher order");
        assertThat(nestedInvokeCounter.get().incrementAndGet(), equalTo(3));
    }

    @AfterStory
    public void afterStory(@Named("theme") String theme) {
        if (theme.length() > 0) {
            System.out.println("After Story with theme '" + theme + "'.");
        } else {
            System.out.println("After Story ...");
        }
        assertThat(nestedInvokeCounter.get().decrementAndGet(), equalTo(3));
    }

    @AfterStory(order = 1)
    public void afterStoryOrdered() {
        System.out.println("After Story with higher order");
        assertThat(nestedInvokeCounter.get().decrementAndGet(), equalTo(2));
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
    public void givenASetup() {
        System.out.println("Doing a setup");
    }

    @Then("a teardown")
    public void thenATeardown() {
        System.out.println("Doing a teardown");
    }
}
