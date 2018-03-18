package org.jbehave.core.reporters;

import org.hamcrest.MatcherAssert;
import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.jbehave.core.model.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class StepFailureDecoratorBehaviour {

    @Mock
    private StoryReporter delegate;

    @InjectMocks
    private StepFailureDecorator decorator;

    @Test
    public void shouldJustDelegateAllReportingMethodsOtherThanFailure() {
        // Given
        Story story = new Story();
        boolean givenStory = false;
        List<String> steps = asList("Given step <one>", "Then step <two>");
        ExamplesTable table = new ExamplesTable("|one|two|\n |1|2|\n");
        Map<String, String> tableRow = table.getRow(0);

        // When
        decorator.dryRun();
        decorator.beforeStory(story, givenStory);
        Scenario scenario = new Scenario("My core 1", Meta.EMPTY);
        decorator.beforeScenario(scenario);
        GivenStories givenStories = new GivenStories("/path1,/path2");
        decorator.givenStories(givenStories);
        decorator.ignorable("!-- Then ignore me");
        decorator.comment("!-- A comment");
        decorator.successful("Given step 1.1");
        decorator.pending("When step 1.2");
        decorator.notPerformed("Then step 1.3");
        decorator.beforeExamples(steps, table);
        decorator.example(tableRow);
        decorator.afterExamples();
        decorator.afterScenario();
        decorator.afterStory(givenStory);

        // Then
        InOrder inOrder = inOrder(delegate);

        inOrder.verify(delegate).beforeStory(story, givenStory);
        inOrder.verify(delegate).beforeScenario(scenario);
        inOrder.verify(delegate).givenStories(givenStories);
        inOrder.verify(delegate).ignorable("!-- Then ignore me");
        inOrder.verify(delegate).comment("!-- A comment");
        inOrder.verify(delegate).successful("Given step 1.1");
        inOrder.verify(delegate).pending("When step 1.2");
        inOrder.verify(delegate).notPerformed("Then step 1.3");
        inOrder.verify(delegate).beforeExamples(steps, table);
        inOrder.verify(delegate).example(tableRow);
        inOrder.verify(delegate).afterExamples();
        inOrder.verify(delegate).afterScenario();
        inOrder.verify(delegate).afterStory(givenStory);
    }

    @Test
    public void shouldProvideFailureCauseWithMessageDescribingStep() {
        // Given
        Throwable t = new UUIDExceptionWrapper(new IllegalArgumentException("World Peace for everyone"));
        // When
        decorator.failed("When I have a bad idea", t);
        OutcomesTable table = new OutcomesTable();
        decorator.failedOutcomes("When outcomes fail", table);

        // Then
        verify(delegate).failed(Mockito.eq("When I have a bad idea"), Mockito.eq(t));
        verify(delegate).failedOutcomes(Mockito.eq("When outcomes fail"), Mockito.eq(table));
    }

    @Test
    public void shouldRethrowFailureCauseAfterStory() {
        // Given
        Throwable t = new UUIDExceptionWrapper(new IllegalArgumentException("World Peace for everyone"));
        String stepAsString = "When I have a bad idea";
        decorator.failed(stepAsString, t);
        boolean givenStory = false;

        // When
        try {
            decorator.afterStory(givenStory);
            throw new AssertionError("Should have rethrown exception");
        } catch (Throwable rethrown) {
            // Then
            MatcherAssert.assertThat(rethrown, equalTo(t));
        }
    }
}
