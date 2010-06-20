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
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.StepCollector;
import org.jbehave.core.steps.StepFinder;
import org.jbehave.core.steps.StepMonitor;
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
        AnnotationFinder finder = new AnnotationFinder(annotatedInstance.getClass());

        List<Object> stepsInstances = new ArrayList<Object>();
        if (finder.isAnnotationPresent(WithCandidateSteps.class)) {
            List<Class<?>> stepsClasses = new ArrayList<Class<?>>();
            finder.getMemberValues(WithCandidateSteps.class, stepsClasses, "candidateSteps");
            for (Class<?> stepsClass : stepsClasses) {
                stepsInstances.add(instanceOf(stepsClass));
            }
        }
        Configuration configuration = buildConfiguration(annotatedInstance);
        return new InstanceStepsFactory(configuration, stepsInstances).createCandidateSteps();
    }

    @SuppressWarnings("unchecked")
    private <T> T configurationElement(AnnotationFinder finder, String name, Class<T> type) {
        Class<T> elementClass = finder.getMemberValue(WithConfiguration.class, Class.class, name);
        return instanceOf(elementClass);
    }

    private ParameterConverters parameterConverters(AnnotationFinder annotationFinder) {
        List<Class<ParameterConverter>> converterClasses = new ArrayList<Class<ParameterConverter>>();
        annotationFinder.getMemberValues(WithConfiguration.class, converterClasses, "parameterConverters");
        List<ParameterConverter> converters = new ArrayList<ParameterConverter>();
        for (Class<ParameterConverter> converterClass : converterClasses) {
            converters.add(instanceOf(converterClass));
        }
        return new ParameterConverters().addConverters(converters);
    }

    private <T> T instanceOf(Class<T> ofClass) {
        try {
            return (T)ofClass.newInstance();
        } catch (Exception e) {
            annotationMonitor.elementCreationFailed(ofClass, e);
            throw new RuntimeException(e);
        }
    }

}
