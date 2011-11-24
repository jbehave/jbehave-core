package org.jbehave.core.steps;

import java.util.List;

import org.jbehave.core.annotations.ScenarioType;
import org.jbehave.core.configuration.Configuration;

/**
 * Interface providing the list of step candidates, representing methods
 * annotated with {@link Given @Given}, {@link When @When} or {@link Then @Then}
 * that can be matched. It also provides the list of before and after steps,
 * representing methods annotated with {@link BeforeStories @BeforeStories},
 * {@link AfterStories @AfterStories}, {@link BeforeStory @BeforeStory},
 * {@link AfterStory @AfterStory}, {@link BeforeScenario @BeforeScenario},
 * {@link AfterScenario @AfterScenario}, that do not require any matching.
 */
public interface CandidateSteps {

    /**
     * Returns the step candidates that can be matched
     * 
     * @return The list of step candidates
     */
    List<StepCandidate> listCandidates();

    /**
     * Returns the before or after stories steps
     * 
     * @return The list of before or after steps
     */
    List<BeforeOrAfterStep> listBeforeOrAfterStories();

    /**
     * Returns the before or after story steps, based on the given story status
     * 
     * @param givenStory
     *            the boolean flag denoting if it's a given story
     * @return The list of before or after steps
     */
    List<BeforeOrAfterStep> listBeforeOrAfterStory(boolean givenStory);

    /**
     * Returns the before or after scenario steps
     * @param type the ScenarioType
     * 
     * @return The list of before or after steps
     */
    List<BeforeOrAfterStep> listBeforeOrAfterScenario(ScenarioType type);

    /**
     * Returns the configuration
     * 
     * @return The Configuration
     */
    Configuration configuration();

}
