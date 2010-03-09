package org.jbehave.scenario.reporters;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.jbehave.scenario.definition.ExamplesTable;
import org.jbehave.scenario.definition.StoryDefinition;
import org.junit.Test;
import org.mockito.InOrder;

public class PassSilentlyDecoratorBehaviour {

    @Test
    public void shouldSwallowOutputFromPassingScenarios() {
        ScenarioReporter delegate = mock(ScenarioReporter.class);
        PassSilentlyDecorator decorator = new PassSilentlyDecorator(delegate);
        List<String> givenScenarios = asList("path/to/scenario1", "path/to/scenario2");
        ExamplesTable examplesTable = new ExamplesTable("|one|two|\n|1|2|\n");
        IllegalArgumentException anException = new IllegalArgumentException();
        StoryDefinition story = new StoryDefinition();        
        boolean embeddedStory = false;
        
        decorator.beforeStory(story, embeddedStory);
        decorator.beforeScenario("My scenario 1");
        decorator.successful("Given step 1.1");
        decorator.ignorable("!-- ignore me");
        decorator.successful("When step 1.2");
        decorator.successful("Then step 1.3");
        decorator.afterScenario();
        
        decorator.beforeScenario("My scenario 2");
		decorator.givenScenarios(givenScenarios);
        decorator.successful("Given step 2.1");
        decorator.pending("When step 2.2");
        decorator.notPerformed("Then step 2.3");
        decorator.afterScenario();
        
        decorator.beforeScenario("My scenario 3");
		decorator.beforeExamples(asList("Given step <one>", "Then step <two>"), examplesTable);
        decorator.successful("Given step 3.1");
        decorator.successful("When step 3.2");
        decorator.failed("Then step 3.3", anException);
        decorator.afterScenario();
        
        decorator.beforeScenario("My scenario 4");
        decorator.successful("Given step 4.1");
        decorator.successful("When step 4.2");
        decorator.successful("Then step 4.3");
        decorator.afterScenario();
        decorator.afterStory(embeddedStory);
        
        InOrder inOrder = inOrder(delegate);
        
        verify(delegate, never()).beforeScenario("My scenario 1");
        verify(delegate, never()).successful("Given step 1.1");
        verify(delegate, never()).ignorable("!-- ignore me");
        verify(delegate, never()).successful("When step 1.2");
        verify(delegate, never()).successful("Then step 1.3");

        verify(delegate, never()).beforeScenario("My scenario 4");
        verify(delegate, never()).successful("Given step 4.1");
        verify(delegate, never()).successful("When step 4.2");
        verify(delegate, never()).successful("Then step 4.3");
        
        inOrder.verify(delegate).beforeStory(story, embeddedStory);
        inOrder.verify(delegate).beforeScenario("My scenario 2");
        inOrder.verify(delegate).givenScenarios(givenScenarios);
        inOrder.verify(delegate).successful("Given step 2.1");
        inOrder.verify(delegate).pending("When step 2.2");
        inOrder.verify(delegate).notPerformed("Then step 2.3");
        inOrder.verify(delegate).afterScenario();
        
        inOrder.verify(delegate).beforeScenario("My scenario 3");
        inOrder.verify(delegate).beforeExamples(asList("Given step <one>", "Then step <two>"), examplesTable);
        inOrder.verify(delegate).successful("Given step 3.1");
        inOrder.verify(delegate).successful("When step 3.2");
        inOrder.verify(delegate).failed("Then step 3.3", anException);
        
        inOrder.verify(delegate).afterScenario();
        inOrder.verify(delegate).afterStory(embeddedStory);
        
    }
}
