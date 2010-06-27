package org.jbehave.examples.trader.guice;

import static java.util.Arrays.asList;
import static org.jbehave.core.io.CodeLocations.codeLocationFromClass;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.jbehave.core.annotations.Configure;
import org.jbehave.core.annotations.UsingSteps;
import org.jbehave.core.annotations.WithEmbedderControls;
import org.jbehave.core.annotations.guice.UsingGuice;
import org.jbehave.core.configuration.guice.GuiceJUnit4ClassRunner;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.io.StoryLoader;
import org.jbehave.core.parsers.RegexPrefixCapturingPatternParser;
import org.jbehave.core.parsers.StepPatternParser;
import org.jbehave.core.steps.ParameterConverters.DateConverter;
import org.jbehave.core.steps.ParameterConverters.ParameterConverter;
import org.jbehave.examples.trader.BeforeAfterSteps;
import org.jbehave.examples.trader.stories.AndStep.AndSteps;
import org.jbehave.examples.trader.stories.ClaimsWithNullCalendar;
import org.jbehave.examples.trader.stories.FailureFollowedByGivenStories.SandpitSteps;
import org.jbehave.examples.trader.stories.PriorityMatching.PriorityMatchingSteps;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;

/**
 * Run stories via Embedder using JBehave's annotated configuration using a
 * Guice module built from separate locations for configuration and steps.
 */
@RunWith(GuiceJUnit4ClassRunner.class)
@Configure()
@UsingSteps(instances = { BeforeAfterSteps.class, AndSteps.class,
		ClaimsWithNullCalendar.CalendarSteps.class,
		PriorityMatchingSteps.class, SandpitSteps.class })
@UsingGuice(modules = { AnnotatedEmbedderUsingGuice.TraderModule.class }, embedder = Embedder.class, 
		embedderControls = @WithEmbedderControls(ignoreFailureInStories = true, ignoreFailureInView = true, batch = true))
public class AnnotatedEmbedderUsingGuice {

	@Inject
	Embedder embedder;

	@Test
	public void run() {
		embedder.runStoriesAsPaths(new StoryFinder().findPaths(
				codeLocationFromClass(this.getClass()).getFile(),
				asList("**/stories/*.story"), asList(""), null));
	}

	// that could be a normal class reused
	public static class TraderModule extends AbstractModule {

		@Override
		protected void configure() {

			bind(ParameterConverter.class).to(DateConverter.class);
			bind(DateFormat.class).toInstance(
					new SimpleDateFormat("yyyy-MM-dd"));
		}

	}
}
