package org.jbehave.scenario.reporters;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.jbehave.scenario.definition.ExamplesTable;
import org.jbehave.scenario.definition.StoryDefinition;
import org.junit.Test;
import org.mockito.InOrder;

public class DelegatingScenarioReporterBehaviour {

    @Test
    public void shouldDelegateScenarioReporterEvents() {
        ScenarioReporter delegate = mock(ScenarioReporter.class);
        DelegatingScenarioReporter delegator = new DelegatingScenarioReporter(delegate);
        List<String> givenScenarios = asList("path/to/scenario1", "path/to/scenario2");
        ExamplesTable examplesTable = new ExamplesTable("|one|two|\n|1|2|\n");
        IllegalArgumentException anException = new IllegalArgumentException();
        StoryDefinition story = new StoryDefinition();        
        boolean embeddedStory = false;
        
        delegator.beforeStory(story, embeddedStory);
        
        delegator.beforeScenario("My scenario 1");
        delegator.givenScenarios(givenScenarios);
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
        
        delegator.afterStory(embeddedStory);
        
        InOrder inOrder = inOrder(delegate);
                
        inOrder.verify(delegate).beforeStory(story, embeddedStory);

        inOrder.verify(delegate).beforeScenario("My scenario 1");
        inOrder.verify(delegate).givenScenarios(givenScenarios);
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
        
        inOrder.verify(delegate).afterStory(embeddedStory);
        
    }
}
