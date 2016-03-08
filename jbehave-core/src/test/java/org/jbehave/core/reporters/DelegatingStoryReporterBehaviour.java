package org.jbehave.core.reporters;

import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.GivenStories;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.junit.Test;
import org.mockito.InOrder;

import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

public class DelegatingStoryReporterBehaviour {

    @Test
    public void shouldDelegateReporterEvents() {
        // Given
        StoryReporter delegate = mock(StoryReporter.class);
        DelegatingStoryReporter delegator = new DelegatingStoryReporter(delegate);
        List<String> givenStoryPaths = asList("path/to/story1", "path/to/story2");
        GivenStories givenStories = new GivenStories(StringUtils.join(givenStoryPaths, ","));
        ExamplesTable examplesTable = new ExamplesTable("|one|two|\n|1|2|\n");
        UUIDExceptionWrapper anException = new UUIDExceptionWrapper(new IllegalArgumentException());
        Story story = new Story();
        boolean givenStory = false;
        Scenario scenario = new Scenario();
        String filter = "-some property";
        
        // When
        delegator.dryRun();
        
        delegator.beforeStory(story, givenStory);        
        delegator.storyNotAllowed(story, filter);
        delegator.beforeScenario("My scenario 1");
        delegator.scenarioNotAllowed(scenario, filter);
        delegator.scenarioMeta(Meta.EMPTY);
        delegator.givenStories(givenStoryPaths);
        delegator.givenStories(givenStories);
        delegator.successful("Given step 1.1");
        delegator.ignorable("!-- ignore me");
        delegator.pending("When step 1.2");
        delegator.notPerformed("Then step 1.3");
        delegator.beforeExamples(asList("Given step <one>", "Then step <two>"), examplesTable);
        delegator.example(examplesTable.getRow(0));
        delegator.afterExamples();
        delegator.afterScenario();
       
        delegator.beforeScenario("My scenario 2");
        delegator.successful("Given step 2.1");
        delegator.successful("When step 2.2");
        delegator.failed("Then step 2.3", anException);
        delegator.afterScenario();
        
        delegator.afterStory(givenStory);
        
        // Then        
        assertThat(delegator.toString(), containsString(delegate.toString()));
        
        InOrder inOrder = inOrder(delegate);
                
        inOrder.verify(delegate).dryRun();        

        inOrder.verify(delegate).beforeStory(story, givenStory);
        inOrder.verify(delegate).storyNotAllowed(story, filter);
        
        inOrder.verify(delegate).beforeScenario("My scenario 1");
        inOrder.verify(delegate).scenarioNotAllowed(scenario, filter);
        inOrder.verify(delegate).scenarioMeta(Meta.EMPTY);
        inOrder.verify(delegate).givenStories(givenStoryPaths);
        inOrder.verify(delegate).givenStories(givenStories);
        inOrder.verify(delegate).successful("Given step 1.1");
        inOrder.verify(delegate).ignorable("!-- ignore me");
        inOrder.verify(delegate).pending("When step 1.2");
        inOrder.verify(delegate).notPerformed("Then step 1.3");
        inOrder.verify(delegate).beforeExamples(asList("Given step <one>", "Then step <two>"), examplesTable);
        inOrder.verify(delegate).example(examplesTable.getRow(0));
        inOrder.verify(delegate).afterExamples();
        inOrder.verify(delegate).afterScenario();
        
        inOrder.verify(delegate).beforeScenario("My scenario 2");
        inOrder.verify(delegate).successful("Given step 2.1");
        inOrder.verify(delegate).successful("When step 2.2");
        inOrder.verify(delegate).failed("Then step 2.3", anException);        
        inOrder.verify(delegate).afterScenario();
        
        inOrder.verify(delegate).afterStory(givenStory);
        
    }
}
