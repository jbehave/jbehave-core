package org.jbehave.core.configuration.guice;

import java.util.List;

import org.jbehave.core.annotations.WithEmbedderControls;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.steps.CandidateSteps;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

import com.google.inject.Injector;

public class GuiceJUnit4ClassRunner extends BlockJUnit4ClassRunner {

	private Injector injector;

	public GuiceJUnit4ClassRunner(Class<?> klass) throws InitializationError {
		super(klass);
	}

	@Override
	public Object createTest() {
		Class<?> myTestClass = getTestClass().getJavaClass();
		Object test = null;
		try {
			GuiceAnnotationBuilder annotationBuilder = new GuiceAnnotationBuilder(
					myTestClass);
			injector = annotationBuilder.getInjector();

			// Create the configuration reading the annotations from test
			// wrapper
			Configuration config = annotationBuilder.buildConfiguration();
			List<CandidateSteps> cadidateSteps = annotationBuilder
					.buildCandidateSteps();

			// Create the embedder class
			Embedder embedder = injector.getInstance(Embedder.class);
			// TODO **** REMOVE THIS WHEN JAVAX.INJECT IS AVAIABLE ON GUICE
			// (hope in version 2.1)
			embedder.useConfiguration(config);
			embedder.useCandidateSteps(cadidateSteps);
			//////******
			if (annotationBuilder.getFinder().isAnnotationPresent(
					WithEmbedderControls.class)) {

				boolean batch = annotationBuilder.getFinder()
						.getAnnotatedValue(WithEmbedderControls.class,
								Boolean.class, "batch");
				boolean skip = annotationBuilder.getFinder().getAnnotatedValue(
						WithEmbedderControls.class, Boolean.class, "skip");
				boolean generateViewAfterStories = annotationBuilder
						.getFinder().getAnnotatedValue(
								WithEmbedderControls.class, Boolean.class,
								"generateViewAfterStories");
				boolean ignoreFailureInStories = annotationBuilder.getFinder()
						.getAnnotatedValue(WithEmbedderControls.class,
								Boolean.class, "ignoreFailureInStories");
				boolean ignoreFailureInView = annotationBuilder.getFinder()
						.getAnnotatedValue(WithEmbedderControls.class,
								Boolean.class, "ignoreFailureInView");

				embedder.embedderControls().doBatch(batch);
				embedder.embedderControls().doSkip(skip);
				embedder.embedderControls().doGenerateViewAfterStories(
						generateViewAfterStories);
				embedder.embedderControls().doIgnoreFailureInStories(
						ignoreFailureInStories);
				embedder.embedderControls().doIgnoreFailureInView(
						ignoreFailureInView);

				test = injector.getInstance(myTestClass);
			}
		} catch (Exception e) {
			// logger.log(Level.SEVERE, "Configuration Error", e);
		}
		return test;
	}

	// TODO this will be necessary in future
	// private static class DefaultModules extends AbstractModule
	// {
	//
	// @Override
	// protected void configure() {
	//
	// // Used to configure Embedder
	// bind(StoryRunner.class).to(StoryRunner.class);
	// bind(EmbedderMonitor.class).to(PrintStreamEmbedderMonitor.class);
	// }
	//
	// }

}
