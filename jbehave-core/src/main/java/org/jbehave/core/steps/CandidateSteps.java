package org.jbehave.core.steps;

import java.util.List;
import java.util.Map;

import org.jbehave.core.annotations.ScenarioType;

/**
 * Interface providing the list of step candidates, representing methods
 * annotated with {@link org.jbehave.core.annotations.Given @Given}, {@link org.jbehave.core.annotations.When @When}
 * or {@link org.jbehave.core.annotations.Then @Then}
 * that can be matched. It also provides the list of before and after steps,
 * representing methods annotated with {@link org.jbehave.core.annotations.BeforeStories @BeforeStories},
 * {@link org.jbehave.core.annotations.AfterStories @AfterStories},
 * {@link org.jbehave.core.annotations.BeforeStory @BeforeStory},
 * {@link org.jbehave.core.annotations.AfterStory @AfterStory},
 * {@link org.jbehave.core.annotations.BeforeScenario @BeforeScenario},
 * {@link org.jbehave.core.annotations.AfterScenario @AfterScenario}, that do not require any matching.
 */
public interface CandidateSteps {

    /**
     * Returns the step candidates that can be matched
     *
     * @return The list of step candidates
     */
    List<StepCandidate> listCandidates();

    /**
     * Returns the before stories steps
     *
     * @return The list of the before stories steps
     */
    List<BeforeOrAfterStep> listBeforeStories();

    /**
     * Returns the after stories steps
     *
     * @return The list of the after stories steps
     */
    List<BeforeOrAfterStep> listAfterStories();

    /**
     * Returns the before story steps, based on the given story status
     *
     * @param givenStory the boolean flag denoting if it's a given story
     * @return The list of the before story steps
     */
    List<BeforeOrAfterStep> listBeforeStory(boolean givenStory);

    /**
     * Returns the after story steps, based on the given story status
     *
     * @param givenStory the boolean flag denoting if it's a given story
     * @return The list of the after story steps
     */
    List<BeforeOrAfterStep> listAfterStory(boolean givenStory);

    /**
     * Returns the before scenario steps
     *
     * @return The list of the before scenario steps
     */
    Map<ScenarioType, List<BeforeOrAfterStep>> listBeforeScenario();

    /**
     * Returns the after scenario steps
     *
     * @return The list of the after scenario steps
     */
    Map<ScenarioType, List<BeforeOrAfterStep>> listAfterScenario();
}
