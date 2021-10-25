package org.jbehave.core.reporters;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.GivenStories;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.steps.StepCollector.Stage;
import org.jbehave.core.steps.LifecycleStepsType;
import org.jbehave.core.steps.Timing;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

class DelegatingStoryReporterBehaviour {

    @Test
    void shouldDelegateReporterEvents() {
        // Given
        StoryReporter delegate = mock(StoryReporter.class);
        StoryReporter delegator = new DelegatingStoryReporter(delegate);
        List<String> givenStoryPaths = asList("path/to/story1", "path/to/story2");
        GivenStories givenStories = new GivenStories(StringUtils.join(givenStoryPaths, ","));
        ExamplesTable examplesTable = new ExamplesTable("|one|two|\n|1|2|\n");
        UUIDExceptionWrapper anException = new UUIDExceptionWrapper(new IllegalArgumentException());
        Story story = new Story();
        boolean givenStory = false;
        Scenario scenario = new Scenario();
        Scenario scenario2 = new Scenario();
        String filter = "-some property";
        Timing timing = new Timing();
        
        // When
        delegator.dryRun();
        
        delegator.beforeStory(story, givenStory);        
        delegator.storyExcluded(story, filter);
        delegator.beforeScenarios();
        delegator.beforeScenario(scenario);
        delegator.beforeScenarioSteps(Stage.BEFORE, LifecycleStepsType.USER);
        delegator.afterScenarioSteps(Stage.BEFORE, LifecycleStepsType.USER);
        delegator.scenarioExcluded(scenario, filter);
        delegator.givenStories(givenStoryPaths);
        delegator.givenStories(givenStories);
        delegator.beforeScenarioSteps(null, null);
        delegator.successful("Given step 1.1");
        delegator.ignorable("!-- Then ignore me");
        delegator.comment("!-- comment");
        delegator.pending("When step 1.2");
        delegator.notPerformed("Then step 1.3");
        delegator.afterScenarioSteps(null, null);
        delegator.beforeExamples(asList("Given step <one>", "Then step <two>"), examplesTable);
        delegator.example(examplesTable.getRow(0), 0);
        delegator.beforeScenarioSteps(Stage.AFTER, LifecycleStepsType.USER);
        delegator.afterScenarioSteps(Stage.AFTER, LifecycleStepsType.USER);
        delegator.afterExamples();
        delegator.afterScenario(timing);
       
        delegator.beforeScenario(scenario2);
        delegator.beforeScenarioSteps(Stage.BEFORE, LifecycleStepsType.USER);
        delegator.afterScenarioSteps(Stage.BEFORE, LifecycleStepsType.USER);
        delegator.beforeScenarioSteps(null, null);
        delegator.successful("Given step 2.1");
        delegator.successful("When step 2.2");
        delegator.failed("Then step 2.3", anException);
        delegator.afterScenarioSteps(null, null);
        delegator.beforeScenarioSteps(Stage.AFTER, LifecycleStepsType.USER);
        delegator.afterScenarioSteps(Stage.AFTER, LifecycleStepsType.USER);
        delegator.afterScenario(timing);
        delegator.afterScenarios();
        
        delegator.afterStory(givenStory);
        
        // Then        
        assertThat(delegator.toString(), containsString(delegate.toString()));
        
        InOrder inOrder = inOrder(delegate);
                
        inOrder.verify(delegate).dryRun();        

        inOrder.verify(delegate).beforeStory(story, givenStory);
        inOrder.verify(delegate).storyExcluded(story, filter);

        inOrder.verify(delegate).beforeScenarios();
        inOrder.verify(delegate).beforeScenario(scenario);
        inOrder.verify(delegate).beforeScenarioSteps(Stage.BEFORE, LifecycleStepsType.USER);
        inOrder.verify(delegate).afterScenarioSteps(Stage.BEFORE, LifecycleStepsType.USER);
        inOrder.verify(delegate).scenarioExcluded(scenario, filter);
        inOrder.verify(delegate).givenStories(givenStoryPaths);
        inOrder.verify(delegate).givenStories(givenStories);
        inOrder.verify(delegate).beforeScenarioSteps(null, null);
        inOrder.verify(delegate).successful("Given step 1.1");
        inOrder.verify(delegate).ignorable("!-- Then ignore me");
        inOrder.verify(delegate).comment("!-- comment");
        inOrder.verify(delegate).pending("When step 1.2");
        inOrder.verify(delegate).notPerformed("Then step 1.3");
        inOrder.verify(delegate).afterScenarioSteps(null, null);
        inOrder.verify(delegate).beforeExamples(asList("Given step <one>", "Then step <two>"), examplesTable);
        inOrder.verify(delegate).example(examplesTable.getRow(0), 0);
        inOrder.verify(delegate).beforeScenarioSteps(Stage.AFTER, LifecycleStepsType.USER);
        inOrder.verify(delegate).afterScenarioSteps(Stage.AFTER, LifecycleStepsType.USER);
        inOrder.verify(delegate).afterExamples();
        inOrder.verify(delegate).afterScenario(timing);
        
        inOrder.verify(delegate).beforeScenario(scenario2);
        inOrder.verify(delegate).beforeScenarioSteps(Stage.BEFORE, LifecycleStepsType.USER);
        inOrder.verify(delegate).afterScenarioSteps(Stage.BEFORE, LifecycleStepsType.USER);
        inOrder.verify(delegate).beforeScenarioSteps(null, null);
        inOrder.verify(delegate).successful("Given step 2.1");
        inOrder.verify(delegate).successful("When step 2.2");
        inOrder.verify(delegate).failed("Then step 2.3", anException);        
        inOrder.verify(delegate).afterScenarioSteps(null, null);
        inOrder.verify(delegate).beforeScenarioSteps(Stage.AFTER, LifecycleStepsType.USER);
        inOrder.verify(delegate).afterScenarioSteps(Stage.AFTER, LifecycleStepsType.USER);
        inOrder.verify(delegate).afterScenario(timing);
        inOrder.verify(delegate).afterScenarios();
        
        inOrder.verify(delegate).afterStory(givenStory);
    }
}
