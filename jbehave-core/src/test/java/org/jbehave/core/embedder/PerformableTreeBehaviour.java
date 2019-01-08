package org.jbehave.core.embedder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.jbehave.core.annotations.Scope;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.embedder.PerformableTree.RunContext;
import org.jbehave.core.failures.BatchFailures;
import org.jbehave.core.io.StoryLoader;
import org.jbehave.core.model.GivenStories;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.StepCollector;
import org.junit.Test;
import org.mockito.InOrder;

/**
 * @author Valery Yatsynovich
 */
public class PerformableTreeBehaviour {

    private static final String GIVEN_SCENARIO_FAIL = "Scenario: given scenario title\nWhen I fail";
    private static final String STORY_PATH = "path";

    @Test
    public void shouldAddNotAllowedPerformableScenariosToPerformableStory() {
        Scenario scenario = new Scenario("scenario title", Meta.createMeta("@skip", new Keywords()));
        Story story = new Story(STORY_PATH, Collections.singletonList(scenario));
        List<Story> stories = Collections.singletonList(story);

        StepCollector stepCollector = mock(StepCollector.class);
        Configuration configuration = mock(Configuration.class);
        when(configuration.stepCollector()).thenReturn(stepCollector);
        when(configuration.storyControls()).thenReturn(new StoryControls());
        List<CandidateSteps> candidateSteps = Collections.emptyList();
        EmbedderMonitor embedderMonitor = mock(EmbedderMonitor.class);
        MetaFilter filter = new MetaFilter("-skip", embedderMonitor);
        BatchFailures failures = mock(BatchFailures.class);

        PerformableTree performableTree = new PerformableTree();
        PerformableTree.RunContext runContext = performableTree.newRunContext(configuration, candidateSteps,
                embedderMonitor, filter, failures);
        performableTree.addStories(runContext, stories);

        assertThat(performableTree.getRoot().getStories().get(0).getScenarios().size(), equalTo(1));

        InOrder ordered = inOrder(stepCollector);
        ordered.verify(stepCollector).collectBeforeOrAfterStoriesSteps(candidateSteps, StepCollector.Stage.BEFORE);
        ordered.verify(stepCollector).collectBeforeOrAfterStorySteps(candidateSteps, story, StepCollector.Stage.BEFORE,
                false);
        ordered.verify(stepCollector).collectLifecycleSteps(eq(candidateSteps), eq(story.getLifecycle()),
                any(Meta.class), eq(StepCollector.Stage.BEFORE), eq(Scope.STORY));
        ordered.verify(stepCollector).collectLifecycleSteps(eq(candidateSteps), eq(story.getLifecycle()),
                any(Meta.class), eq(StepCollector.Stage.AFTER), eq(Scope.STORY));
        ordered.verify(stepCollector).collectBeforeOrAfterStorySteps(candidateSteps, story, StepCollector.Stage.AFTER,
                false);
        ordered.verify(stepCollector).collectBeforeOrAfterStoriesSteps(candidateSteps, StepCollector.Stage.AFTER);
        verifyNoMoreInteractions(stepCollector);
    }

    @Test
    public void shouldNotSkipStoryWhenGivenStoryIsFailed() {
        RunContext context = performStoryRun(false, GIVEN_SCENARIO_FAIL);
        assertThat(context.failureOccurred(), is(false));
        assertThat(context.configuration().storyControls().skipStoryIfGivenStoryFailed(), is(false));
    }

    @Test
    public void shouldSkipStoryWhenGivenStoryIsFailed() {
        RunContext context = performStoryRun(true, GIVEN_SCENARIO_FAIL);
        assertThat(context.failureOccurred(), is(true));
        assertThat(context.configuration().storyControls().skipStoryIfGivenStoryFailed(), is(true));
    }

    @Test
    public void shouldNotSkipStoryWhenGivenStoryIsPassed() {
        RunContext context = performStoryRun(true, "Scenario: given scenario title");
        assertThat(context.failureOccurred(), is(false));
        assertThat(context.configuration().storyControls().skipStoryIfGivenStoryFailed(), is(true));
    }

    private RunContext performStoryRun(boolean skipScenariosAfterGivenStoriesFailure, String givenStoryAsString) {
        String givenStoryPath = "given/path";
        Scenario scenario = new Scenario("base scenario title", Meta.EMPTY);
        Story story = new Story(STORY_PATH, null, null, null, new GivenStories(givenStoryPath),
                Collections.singletonList(scenario));

        Configuration configuration = new MostUsefulConfiguration()
                .useStoryControls(new StoryControls().doSkipStoryIfGivenStoryFailed(skipScenariosAfterGivenStoriesFailure));
        StoryLoader storyLoader = mock(StoryLoader.class);
        configuration.useStoryLoader(storyLoader);
        when(storyLoader.loadStoryAsText(givenStoryPath)).thenReturn(givenStoryAsString);
        List<CandidateSteps> candidateSteps = Collections.emptyList();
        EmbedderMonitor embedderMonitor = mock(EmbedderMonitor.class);
        BatchFailures failures = mock(BatchFailures.class);

        PerformableTree performableTree = new PerformableTree();
        RunContext runContext = performableTree.newRunContext(configuration, candidateSteps,
                embedderMonitor, new MetaFilter(), failures);
        performableTree.addStories(runContext, Collections.singletonList(story));
        performableTree.perform(runContext, story);
        return runContext;
    }
}
