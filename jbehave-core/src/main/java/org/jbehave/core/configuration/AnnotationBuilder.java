package org.jbehave.core.configuration;

import java.util.ArrayList;
import java.util.List;

import org.jbehave.core.annotations.WithCandidateSteps;
import org.jbehave.core.annotations.WithConfiguration;
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
import org.jbehave.core.steps.StepCollector;
import org.jbehave.core.steps.StepFinder;
import org.jbehave.core.steps.StepMonitor;
import org.jbehave.core.steps.Steps;
import org.jbehave.core.steps.ParameterConverters.ParameterConverter;

import com.thoughtworks.paranamer.Paranamer;

public class AnnotationBuilder {

    private final AnnotationMonitor annotationMonitor;

    public AnnotationBuilder() {
        this(new PrintStreamAnnotationMonitor());
    }

    public AnnotationBuilder(AnnotationMonitor annotationMonitor) {
        this.annotationMonitor = annotationMonitor;
    }

    /**
     * Builds Configuration instance based on annotation
     * {@link WithConfiguration} found in the annotated object instance
     * 
     * @param annotatedInstance
     *            the Object instance that contains the annotations
     * @return A Configuration instance
     */
    public Configuration buildConfiguration(Object annotatedInstance) throws MissingAnnotationException {
        AnnotationFinder finder = new AnnotationFinder(annotatedInstance.getClass());

        Configuration configuration = new Configuration();
        configuration.useKeywords(configurationElement(finder, "keywords", Keywords.class));
        configuration.useFailureStrategy(configurationElement(finder, "failureStrategy", FailureStrategy.class));
        configuration.usePendingStepStrategy(configurationElement(finder, "pendingStepStrategy",
                PendingStepStrategy.class));
        configuration.useParanamer(configurationElement(finder, "paranamer", Paranamer.class));
        configuration.useStepCollector(configurationElement(finder, "stepCollector", StepCollector.class));
        configuration.useStepdocReporter(configurationElement(finder, "stepdocReporter", StepdocReporter.class));
        configuration.useStepFinder(configurationElement(finder, "stepFinder", StepFinder.class));
        configuration.useStepMonitor(configurationElement(finder, "stepMonitor", StepMonitor.class));
        configuration.useStepPatternParser(configurationElement(finder, "stepPatternParser", StepPatternParser.class));
        configuration.useStoryLoader(configurationElement(finder, "storyLoader", StoryLoader.class));
        configuration.useStoryParser(configurationElement(finder, "storyParser", StoryParser.class));
        configuration.useStoryPathResolver(configurationElement(finder, "storyPathResolver", StoryPathResolver.class));
        configuration
                .useDefaultStoryReporter(configurationElement(finder, "defaultStoryReporter", StoryReporter.class));
        configuration.useStoryReporterBuilder(configurationElement(finder, "storyReporterBuilder",
                StoryReporterBuilder.class));
        configuration.useViewGenerator(configurationElement(finder, "viewGenerator", ViewGenerator.class));
        configuration.useEmbedderControls(configurationElement(finder, "embedderControls", EmbedderControls.class));
        configuration.useParameterConverters(parameterConverters(finder));
        return configuration;
    }

    /**
     * Builds CandidateSteps instances based on annotation
     * {@link WithCandidateSteps} found in the annotated object instance
     * 
     * @param annotatedInstance
     *            the Object instance that contains the annotations
     * @return A List of CandidateSteps instances
     */
    public List<CandidateSteps> buildCandidateSteps(Object annotatedInstance) {

        List<CandidateSteps> candidateSteps = new ArrayList<CandidateSteps>();
        Configuration configuration = buildConfiguration(annotatedInstance);
        AnnotationFinder finder = new AnnotationFinder(annotatedInstance.getClass());

        if (finder.isAnnotationPresent(WithCandidateSteps.class)) {
            List<Class<?>> candidateStepsClasses = new ArrayList<Class<?>>();
            finder.getMemberValues(WithCandidateSteps.class, candidateStepsClasses, "candidateSteps");

            for (Class<?> candidateStepClass : candidateStepsClasses) {
                Object candidateStepInstance;
                try {
                    candidateStepInstance = candidateStepClass.newInstance();
                    candidateSteps.add(new Steps(configuration, candidateStepInstance));
                } catch (Exception e) {
                    annotationMonitor.annotatedElementInvalid(candidateStepClass, e);
                }
            }
        }
        return candidateSteps;
    }

    @SuppressWarnings("unchecked")
    private <T> T configurationElement(AnnotationFinder finder, String name, Class<T> type) {
        Class<?> elementClass = finder.getMemberValue(WithConfiguration.class, Class.class, name);
        try {
            return (T) elementClass.newInstance();
        } catch (Exception e) {
            annotationMonitor.annotatedElementInvalid(elementClass, e);
            throw new RuntimeException(e);
        }
    }

    private ParameterConverters parameterConverters(AnnotationFinder annotationFinder) {
        List<Class<?>> converterClasses = new ArrayList<Class<?>>();
        annotationFinder.getMemberValues(WithConfiguration.class, converterClasses, "parameterConverters");
        List<ParameterConverter> converters = new ArrayList<ParameterConverter>();
        for (Class<?> converterClass : converterClasses) {
            try {
                converters.add((ParameterConverter) converterClass.newInstance());
            } catch (Exception e) {
                annotationMonitor.annotatedElementInvalid(converterClass, e);
            }
        }
        return new ParameterConverters().addConverters(converters);
    }

}
