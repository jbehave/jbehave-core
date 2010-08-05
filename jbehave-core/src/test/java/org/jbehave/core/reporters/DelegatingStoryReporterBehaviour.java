package org.jbehave.core.reporters;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.Story;
import org.junit.Test;
import org.mockito.InOrder;

public class DelegatingStoryReporterBehaviour {

    @Test
    public void shouldDelegateReporterEvents() {
        StoryReporter delegate = mock(StoryReporter.class);
        DelegatingStoryReporter delegator = new DelegatingStoryReporter(delegate);
        List<String> givenStories = asList("path/to/story1", "path/to/story2");
        ExamplesTable examplesTable = new ExamplesTable("|one|two|\n|1|2|\n");
        IllegalArgumentException anException = new IllegalArgumentException();
        Story story = new Story();
        boolean givenStory = false;
        
        delegator.dryRun();
        
        delegator.beforeStory(story, givenStory);        
        
        delegator.beforeScenario("My core 1");
        delegator.givenStories(givenStories);
        delegator.successful("Given step 1.1");
        delegator.ignorable("!-- ignore me");
        delegator.pending("When step 1.2");
        delegator.notPerformed("Then step 1.3");
        delegator.beforeExamples(asList("Given step <one>", "Then step <two>"), examplesTable);
        delegator.example(examplesTable.getRow(0));
        delegator.afterExamples();
        delegator.afterScenario();
       
        delegator.beforeScenario("My core 2");
        delegator.successful("Given step 2.1");
        delegator.successful("When step 2.2");
        delegator.failed("Then step 2.3", anException);
        delegator.afterScenario();
        
        delegator.afterStory(givenStory);
        
        InOrder inOrder = inOrder(delegate);
                
        inOrder.verify(delegate).dryRun();        

        inOrder.verify(delegate).beforeStory(story, givenStory);

        inOrder.verify(delegate).beforeScenario("My core 1");
        inOrder.verify(delegate).givenStories(givenStories);
        inOrder.verify(delegate).successful("Given step 1.1");
        inOrder.verify(delegate).ignorable("!-- ignore me");
        inOrder.verify(delegate).pending("When step 1.2");
        inOrder.verify(delegate).notPerformed("Then step 1.3");
        inOrder.verify(delegate).beforeExamples(asList("Given step <one>", "Then step <two>"), examplesTable);
        inOrder.verify(delegate).example(examplesTable.getRow(0));
        inOrder.verify(delegate).afterExamples();
        inOrder.verify(delegate).afterScenario();
        
        inOrder.verify(delegate).beforeScenario("My core 2");
        inOrder.verify(delegate).successful("Given step 2.1");
        inOrder.verify(delegate).successful("When step 2.2");
        inOrder.verify(delegate).failed("Then step 2.3", anException);        
        inOrder.verify(delegate).afterScenario();
        
        inOrder.verify(delegate).afterStory(givenStory);
        
    }
}
