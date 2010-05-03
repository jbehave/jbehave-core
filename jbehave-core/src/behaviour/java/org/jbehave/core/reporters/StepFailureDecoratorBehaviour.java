package org.jbehave.core.reporters;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jbehave.core.model.Story;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

public class StepFailureDecoratorBehaviour {

	private StoryReporter delegate;
	private StepFailureDecorator decorator;

	@Before
	public void createDecorator() {
		delegate = mock(StoryReporter.class);
		decorator = new StepFailureDecorator(delegate);
	}

	@Test
	public void shouldJustDelegateAllReportingMethodsOtherThanFailure() {
		// Given
	    Story story = new Story();
		boolean embeddedStory = false;
		
        // When
		decorator.beforeStory(story, embeddedStory);
		decorator.beforeScenario("My core 1");
		decorator.successful("Given step 1.1");
		decorator.pending("When step 1.2");
		decorator.notPerformed("Then step 1.3");
		decorator.afterScenario();
		decorator.afterStory(embeddedStory);

		// Then
		InOrder inOrder = inOrder(delegate);

		inOrder.verify(delegate).beforeStory(story, embeddedStory);
		inOrder.verify(delegate).beforeScenario("My core 1");
		inOrder.verify(delegate).successful("Given step 1.1");
		inOrder.verify(delegate).pending("When step 1.2");
		inOrder.verify(delegate).notPerformed("Then step 1.3");
		inOrder.verify(delegate).afterScenario();
		inOrder.verify(delegate).afterStory(embeddedStory);
	}

	@Test
	public void shouldProvideFailureCauseWithMessageDescribingStep() {
		// Given
		Throwable t = new IllegalArgumentException("World Peace for everyone");
		String stepAsString = "When I have a bad idea";

		// When
		decorator.failed(stepAsString, t);

		// Then
		verify(delegate).failed(
				eq(stepAsString),
				argThat(hasMessage(t.getMessage() + "\nduring step: '"
						+ stepAsString + "'")));
	}

	@Test
	public void shouldRethrowFailureCauseAfterStory() {
		// Given
		Throwable t = new IllegalArgumentException("World Peace for everyone");
		String stepAsString = "When I have a bad idea";
		decorator.failed(stepAsString, t);
        boolean embeddedStory = false;

		// When
		try {
            decorator.afterStory(embeddedStory);
			fail("Should have rethrown exception");
		} catch (Throwable rethrown) {
			// Then
			assertThat(rethrown, hasMessage(t.getMessage() + "\nduring step: '"
					+ stepAsString + "'"));
		}
	}

	private Matcher<Throwable> hasMessage(final String string) {
		return new TypeSafeMatcher<Throwable>() {

			private Matcher<String> equalTo;

			@Override
			public boolean matchesSafely(Throwable t) {
				equalTo = equalTo(string);
				return equalTo.matches(t.getMessage());
			}

			public void describeTo(Description description) {
				description.appendText("Throwable with message: ")
						.appendDescriptionOf(equalTo);
			}
		};
	}

}
