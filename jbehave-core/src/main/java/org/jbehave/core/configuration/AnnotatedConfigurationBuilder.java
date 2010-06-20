package org.jbehave.core.configuration;

import java.util.ArrayList;
import java.util.List;

import org.jbehave.core.annotations.WithCandidateSteps;
import org.jbehave.core.annotations.WithConfiguration;
import org.jbehave.core.annotations.exceptions.IllegalAnnotationException;
import org.jbehave.core.annotations.exceptions.MissingAnnotationException;
import org.jbehave.core.embedder.EmbedderControls;
import org.jbehave.core.failures.FailureStrategy;
import org.jbehave.core.failures.PendingStepStrategy;
import org.jbehave.core.io.StoryLoader;
import org.jbehave.core.io.StoryPathResolver;
import org.jbehave.core.parsers.StepPatternParser;
import org.jbehave.core.parsers.StoryParser;
import org.jbehave.core.reporters.StepdocReporter;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.reporters.ViewGenerator;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.ParameterConverters.ParameterConverter;
import org.jbehave.core.steps.StepCollector;
import org.jbehave.core.steps.StepFinder;
import org.jbehave.core.steps.StepMonitor;
import org.jbehave.core.steps.Steps;

import com.thoughtworks.paranamer.Paranamer;

public class AnnotatedConfigurationBuilder {

	private static final AnnotationMonitor annotationMonitor = new PrintStreamAnnotationMonitor();

	public AnnotatedConfigurationBuilder() {
	}

	public static Configuration buildConfiguration(Object pAnnotatedRunner)
			throws MissingAnnotationException, IllegalAnnotationException {
		Configuration configuration = new Configuration();
		// Load the Annotation Database for the annotatedRunner object.
		AnnotationFinder annotationFinder = new AnnotationFinder(
				pAnnotatedRunner.getClass());

		configuration.useFailureStrategy(getFailureStrategy(annotationFinder));
		configuration.useKeywords(getKeywords(annotationFinder));
		configuration
				.useParameterConverters(getParameterConverters(annotationFinder));
		configuration.useParanamer(getParanamer(annotationFinder));
		configuration
				.usePendingStepStrategy(getPendingStepStrategy(annotationFinder));
		configuration.useStepCollector(getStepCollector(annotationFinder));
		configuration.useStepdocReporter(getStepdocReporter(annotationFinder));
		configuration.useStepFinder(getStepFinder(annotationFinder));
		configuration.useStepMonitor(getStepMonitor(annotationFinder));
		configuration
				.useStepPatternParser(getStepPatternParser(annotationFinder));
		configuration.useStoryLoader(getStoryLoader(annotationFinder));
		configuration.useStoryParser(getStoryParser(annotationFinder));
		configuration
				.useStoryPathResolver(getStoryPathResolver(annotationFinder));

		configuration
				.useDefaultStoryReporter(getStoryReporter(annotationFinder));
		configuration
				.useEmbedderControls(getEmbedderControls(annotationFinder));
		configuration
				.useStoryReporterBuilder(getStoryReporterBuilder(annotationFinder));
		configuration.useViewGenerator(getViewGenerator(annotationFinder));

		return configuration;
	}



	/**
	 * Helper method that will read the annotation on all pAnnotatedRunner
	 * object hierachy and build a List<CandidateSteps> based on annotations
	 * that was found.
	 * 
	 * @param pAnnotatedRunner
	 * @return
	 */
	public static List<CandidateSteps> buildCandidateSteps(
			Object pAnnotatedRunner) {

		List<CandidateSteps> candidateSteps = new ArrayList<CandidateSteps>();
		Configuration configuration = AnnotatedConfigurationBuilder
				.buildConfiguration(pAnnotatedRunner);
		// Load the Annotation Database for the annotatedRunner object.
		AnnotationFinder annotationFinder = new AnnotationFinder(
				pAnnotatedRunner.getClass());

		if (annotationFinder.isAnnotationPresent(WithCandidateSteps.class)) {
			List<Class<?>> candidateStepsClasses = new ArrayList<Class<?>>();
			annotationFinder.getMemberValues(WithCandidateSteps.class,
					candidateStepsClasses, "candidateSteps");

			for (Class<?> candidateStepClass : candidateStepsClasses) {
				Object candidateStepInstance;
				try {
					candidateStepInstance = candidateStepClass.newInstance();
					candidateSteps.add(new Steps(configuration,
							candidateStepInstance));
				} catch (InstantiationException e) {
					annotationMonitor.processingFailed(pAnnotatedRunner, e);
				} catch (IllegalAccessException e) {
					annotationMonitor.processingFailed(pAnnotatedRunner, e);
				}
			}
		}
		return candidateSteps;
	}

	private static StoryPathResolver getStoryPathResolver(
			AnnotationFinder annotationFinder) {
		StoryPathResolver storyPathResolver = null;

		Class<?> storyPathResolverClass = annotationFinder.getMemberValue(
				WithConfiguration.class, Class.class, "storyPathResolver");
		try {
			storyPathResolver = (StoryPathResolver) storyPathResolverClass
					.newInstance();
		} catch (Exception e) {
			annotationMonitor.processingFailed(storyPathResolverClass, e);
		}
		return storyPathResolver;
	}

	private static StoryParser getStoryParser(AnnotationFinder annotationFinder) {
		StoryParser storyParser = null;
		Class<?> storyParserClass = annotationFinder.getMemberValue(
				WithConfiguration.class, Class.class, "storyParser");
		try {
			storyParser = (StoryParser) storyParserClass.newInstance();
		} catch (Exception e) {
			annotationMonitor.processingFailed(storyParserClass, e);
		}
		return storyParser;
	}

	private static EmbedderControls getEmbedderControls(
			AnnotationFinder annotationFinder) {
		EmbedderControls embedderControls = null;
		Class<?> embedderControlsClass = annotationFinder.getMemberValue(
				WithConfiguration.class, Class.class, "embedderControls");
		try {
			embedderControls = (EmbedderControls) embedderControlsClass.newInstance();
		} catch (Exception e) {
			annotationMonitor.processingFailed(embedderControlsClass, e);
		}
		return embedderControls;
	}

	private static ViewGenerator getViewGenerator(
			AnnotationFinder annotationFinder) {
		ViewGenerator viewGenerator = null;
		Class<?> viewGeneratorClass = annotationFinder.getMemberValue(
				WithConfiguration.class, Class.class, "viewGenerator");
		try {
			viewGenerator = (ViewGenerator) viewGeneratorClass.newInstance();
		} catch (Exception e) {
			annotationMonitor.processingFailed(viewGeneratorClass, e);
		}
		return viewGenerator;
	}

	private static StoryReporterBuilder getStoryReporterBuilder(
			AnnotationFinder annotationFinder) {
		StoryReporterBuilder storyReporterBuilder = null;
		Class<?> storyReporterBuilderClass = annotationFinder.getMemberValue(
				WithConfiguration.class, Class.class, "storyReporterBuilder");
		try {
			storyReporterBuilder = (StoryReporterBuilder) storyReporterBuilderClass.newInstance();
		} catch (Exception e) {
			annotationMonitor.processingFailed(storyReporterBuilderClass, e);
		}
		return storyReporterBuilder;
	}
	
	private static StoryLoader getStoryLoader(AnnotationFinder annotationFinder) {
		StoryLoader storyLoader = null;
		Class<?> storyLoaderClass = annotationFinder.getMemberValue(
				WithConfiguration.class, Class.class, "storyLoader");
		try {
			storyLoader = (StoryLoader) storyLoaderClass.newInstance();
		} catch (Exception e) {
			annotationMonitor.processingFailed(storyLoaderClass, e);
		}
		return storyLoader;
	}

	private static StepPatternParser getStepPatternParser(
			AnnotationFinder annotationFinder) {
		StepPatternParser stepPatternParser = null;
		Class<?> stepPatternParserClass = annotationFinder.getMemberValue(
				WithConfiguration.class, Class.class, "stepPatternParser");
		try {
			stepPatternParser = (StepPatternParser) stepPatternParserClass
					.newInstance();
		} catch (Exception e) {
			annotationMonitor.processingFailed(stepPatternParserClass, e);
		}
		return stepPatternParser;
	}

	private static StepMonitor getStepMonitor(AnnotationFinder annotationFinder) {
		StepMonitor stepMonitor = null;
		Class<?> stepMonitorClass = annotationFinder.getMemberValue(
				WithConfiguration.class, Class.class, "stepMonitor");
		try {
			stepMonitor = (StepMonitor) stepMonitorClass.newInstance();
		} catch (Exception e) {
			annotationMonitor.processingFailed(stepMonitorClass, e);
		}
		return stepMonitor;
	}

	private static StepFinder getStepFinder(AnnotationFinder annotationFinder) {
		StepFinder stepFinder = null;
		Class<?> stepFinderClass = annotationFinder.getMemberValue(
				WithConfiguration.class, Class.class, "stepFinder");
		try {
			stepFinder = (StepFinder) stepFinderClass.newInstance();
		} catch (Exception e) {
			annotationMonitor.processingFailed(stepFinderClass, e);
		}
		return stepFinder;
	}

	private static StepdocReporter getStepdocReporter(
			AnnotationFinder annotationFinder) {
		StepdocReporter stepdocReporter = null;
		Class<?> stepdocReporterClass = annotationFinder.getMemberValue(
				WithConfiguration.class, Class.class, "stepdocReporter");
		try {
			stepdocReporter = (StepdocReporter) stepdocReporterClass
					.newInstance();
		} catch (Exception e) {
			annotationMonitor.processingFailed(stepdocReporterClass, e);
		}
		return stepdocReporter;
	}

	private static StoryReporter getStoryReporter(
			AnnotationFinder annotationFinder) {
		StoryReporter defaultStoryReporter = null;
		Class<?> defaultStoryReporterClass = annotationFinder.getMemberValue(
				WithConfiguration.class, Class.class, "defaultStoryReporter");
		try {
			defaultStoryReporter = (StoryReporter) defaultStoryReporterClass
					.newInstance();
		} catch (Exception e) {
			annotationMonitor.processingFailed(defaultStoryReporterClass, e);
		}
		return defaultStoryReporter;
	}

	private static StepCollector getStepCollector(
			AnnotationFinder annotationFinder) {
		StepCollector stepCollector = null;
		Class<?> stepCollectorClass = annotationFinder.getMemberValue(
				WithConfiguration.class, Class.class, "stepCollector");
		try {
			stepCollector = (StepCollector) stepCollectorClass.newInstance();
		} catch (Exception e) {
			annotationMonitor.processingFailed(stepCollectorClass, e);
		}
		return stepCollector;
	}

	private static PendingStepStrategy getPendingStepStrategy(
			AnnotationFinder annotationFinder) {
		PendingStepStrategy pendingStepStrategy = null;
		Class<?> pendingStepStrategyClass = annotationFinder.getMemberValue(
				WithConfiguration.class, Class.class, "pendingStepStrategy");
		try {
			pendingStepStrategy = (PendingStepStrategy) pendingStepStrategyClass
					.newInstance();
		} catch (Exception e) {
			annotationMonitor.processingFailed(pendingStepStrategyClass, e);
		}
		return pendingStepStrategy;
	}

	private static Paranamer getParanamer(AnnotationFinder annotationFinder) {
		Paranamer paranamer = null;
		Class<?> paranamerClass = annotationFinder.getMemberValue(
				WithConfiguration.class, Class.class, "paranamer");
		try {
			paranamer = (Paranamer) paranamerClass.newInstance();
		} catch (Exception e) {
			annotationMonitor.processingFailed(paranamerClass, e);
		}
		return paranamer;
	}

	private static ParameterConverters getParameterConverters(
			AnnotationFinder annotationFinder) {
		ParameterConverters parameterConverters = null;
		try {
			List<Class<?>> parameterConverterClasses = new ArrayList<Class<?>>();
			List<ParameterConverter> converterList = new ArrayList<ParameterConverter>();
			annotationFinder.getMemberValues(WithConfiguration.class,
					parameterConverterClasses, "parameterConverters");
			parameterConverters = new ParameterConverters();
			for (Class<?> parameterConverterClass : parameterConverterClasses) {
				ParameterConverter parameterConverter = (ParameterConverter) parameterConverterClass
						.newInstance();
				converterList.add(parameterConverter);
			}
			parameterConverters.addConverters(converterList);
		} catch (Exception e) {
			annotationMonitor.processingFailed(parameterConverters, e);
		}
		return parameterConverters;
	}

	private static Keywords getKeywords(AnnotationFinder annotationFinder) {
		Keywords keywords = null;
		Class<?> keywordsClass = annotationFinder.getMemberValue(
				WithConfiguration.class, Class.class, "keywords");
		try {
			keywords = (Keywords) keywordsClass.newInstance();
		} catch (Exception e) {
			annotationMonitor.processingFailed(keywordsClass, e);
		}
		return keywords;
	}

	private static FailureStrategy getFailureStrategy(
			AnnotationFinder annotationFinder) {
		FailureStrategy failureStrategy = null;
		Class<?> failureStrategyClass = annotationFinder.getMemberValue(
				WithConfiguration.class, Class.class, "failureStrategy");
		try {
			failureStrategy = (FailureStrategy) failureStrategyClass
					.newInstance();
		} catch (Exception e) {
			annotationMonitor.processingFailed(failureStrategyClass, e);
		}
		return failureStrategy;
	}
}
