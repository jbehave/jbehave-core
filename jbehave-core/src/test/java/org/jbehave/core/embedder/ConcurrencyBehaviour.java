package org.jbehave.core.embedder;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.jbehave.core.annotations.When;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.embedder.Embedder.RunningEmbeddablesFailed;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.junit.JUnitStories;
import org.jbehave.core.reporters.CrossReference;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.junit.Test;

import static java.util.Arrays.asList;
import static org.jbehave.core.io.CodeLocations.codeLocationFromClass;
import static org.jbehave.core.reporters.Format.CONSOLE;
import static org.jbehave.core.reporters.Format.HTML;
import static org.jbehave.core.reporters.Format.XML;

public class ConcurrencyBehaviour {

	@Test(expected = RunningEmbeddablesFailed.class)
	public void shouldAllowStoriesToBeCancelled() {
		Embedder embedder = new Embedder();
		embedder.embedderControls().useStoryTimeoutInSecs(1);
		embedder.runAsEmbeddables(asList(ThreadsStories.class.getName()));
	}

	@Test
	public void shouldAllowStoriesToBeTimed() {
		Embedder embedder = new Embedder();
		embedder.embedderControls().useStoryTimeoutInSecs(10).useThreads(2).doVerboseFailures(true);
		try {
			embedder.runAsEmbeddables(asList(ThreadsStories.class.getName()));
		} finally {
			embedder.generateCrossReference();
		}
	}

	public static class ThreadsStories extends JUnitStories {
		
		@Override
		public Configuration configuration() {
			return new MostUsefulConfiguration().useStoryLoader(
					new LoadFromClasspath(this.getClass()))
					.useStoryReporterBuilder(
							new StoryReporterBuilder().withFormats(CONSOLE,
									HTML, XML).withCrossReference(new CrossReference()));
		}

		@Override
		public InjectableStepsFactory stepsFactory() {
			return new InstanceStepsFactory(configuration(), new ThreadsSteps());
		}

		@Override
		protected List<String> storyPaths() {
			return new StoryFinder().findPaths(
					codeLocationFromClass(this.getClass()), "**/*.story", "");
		}

	}

	public static class ThreadsSteps {

		@When("$name counts to $n Mississippi")
		public void whenSomeoneCountsMississippis(String name, AtomicInteger n)
				throws InterruptedException {
			long start = System.currentTimeMillis();
			System.out.println(name + " starts counting to " + n);
			for (int i = 0; i < n.intValue(); i++) {
				System.out.println(name + " says " + i + " Mississippi ("
						+ (System.currentTimeMillis() - start) + " millis)");
				TimeUnit.SECONDS.sleep(1);
			}
		}

	}
}
