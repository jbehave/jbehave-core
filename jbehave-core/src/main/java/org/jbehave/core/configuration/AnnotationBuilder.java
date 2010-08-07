package org.jbehave.core.configuration;

import java.util.ArrayList;
import java.util.List;

import org.jbehave.core.ConfigurableEmbedder;
import org.jbehave.core.Embeddable;
import org.jbehave.core.annotations.Configure;
import org.jbehave.core.annotations.UsingEmbedder;
import org.jbehave.core.annotations.UsingSteps;
import org.jbehave.core.embedder.Embedder;
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
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.StepCollector;
import org.jbehave.core.steps.StepFinder;
import org.jbehave.core.steps.StepMonitor;
import org.jbehave.core.steps.ParameterConverters.ParameterConverter;

import com.thoughtworks.paranamer.Paranamer;

/**
 * Allows the building of {@link Configuration}, {@link CandidateSteps} and
 * {@link Embedder} from an annotated class.
 * 
 * @author Cristiano Gavião
 * @author Mauro Talevi
 */
public class AnnotationBuilder {

    private final AnnotationMonitor annotationMonitor;

    private final Class<?> annotatedClass;
    private final AnnotationFinder finder;

    public AnnotationBuilder(Class<?> annotatedClass) {
        this(annotatedClass, new PrintStreamAnnotationMonitor());
    }

    public AnnotationBuilder(Class<?> annotatedClass, AnnotationMonitor annotationMonitor) {
        this.annotationMonitor = annotationMonitor;
        this.annotatedClass = annotatedClass;
        this.finder = new AnnotationFinder(annotatedClass);
    }

    public Class<?> annotatedClass() {
        return annotatedClass;
    }

    /**
     * Builds a Configuration instance based on annotation {@link Configure}
     * found in the annotated object instance
     * 
     * @return A Configuration instance
     */
    public Configuration buildConfiguration() throws AnnotationRequired {

        Configuration configuration = new MostUsefulConfiguration();

        if (!finder.isAnnotationPresent(Configure.class)) {
            // not using annotation configuration, default to most useful
            // configuration
            return configuration;
        }

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
        configuration.useParameterConverters(parameterConverters(finder));
        return configuration;
    }

    /**
     * Builds CandidateSteps using annotation {@link UsingSteps} found in the
     * annotated object instance and using the configuration build by
     * {@link #buildConfiguration()}
     * 
     * @return A List of CandidateSteps instances
     */
    public List<CandidateSteps> buildCandidateSteps() {
        return buildCandidateSteps(buildConfiguration());
    }

    /**
     * Builds CandidateSteps using annotation {@link UsingSteps} found in the
     * annotated object instance and the configuration provided
     * 
     * @param configuration
     *            the Configuration
     * @return A List of CandidateSteps instances
     */
    public List<CandidateSteps> buildCandidateSteps(Configuration configuration) {
        List<Object> stepsInstances = new ArrayList<Object>();
        InjectableStepsFactory factory = null;
        if (finder.isAnnotationPresent(UsingSteps.class)) {
            List<Class<Object>> stepsClasses = finder.getAnnotatedClasses(UsingSteps.class, Object.class, "instances");
            for (Class<Object> stepsClass : stepsClasses) {
                stepsInstances.add(instanceOf(Object.class, stepsClass));
            }
            factory = new InstanceStepsFactory(configuration, stepsInstances);
        } else {
            annotationMonitor.annotationNotFound(UsingSteps.class, annotatedClass);
        }

        if (factory == null) {
            factory = new InstanceStepsFactory(configuration);
        }
        return factory.createCandidateSteps();
    }

    @SuppressWarnings("unchecked")
    public Embedder buildEmbedder() {
        if (!finder.isAnnotationPresent(UsingEmbedder.class)) {
            return new Embedder();
        }

        boolean batch = control(finder, "batch");
        boolean skip = control(finder, "skip");
        boolean generateViewAfterStories = control(finder, "generateViewAfterStories");
        boolean ignoreFailureInStories = control(finder, "ignoreFailureInStories");
        boolean ignoreFailureInView = control(finder, "ignoreFailureInView");
        Configuration configuration = buildConfiguration();
        List<CandidateSteps> candidateSteps = buildCandidateSteps(configuration);

        Embedder embedder = instanceOf(Embedder.class, finder.getAnnotatedValue(UsingEmbedder.class, Class.class,
                "embedder"));
        embedder.embedderControls().doBatch(batch).doSkip(skip).doGenerateViewAfterStories(generateViewAfterStories)
                .doIgnoreFailureInStories(ignoreFailureInStories).doIgnoreFailureInView(ignoreFailureInView);
        embedder.useConfiguration(configuration);
        embedder.useCandidateSteps(candidateSteps);
        return embedder;
    }

    private boolean control(AnnotationFinder finder, String name) {
        return finder.getAnnotatedValue(UsingEmbedder.class, Boolean.class, name);
    }

    private <T> T configurationElement(AnnotationFinder finder, String name, Class<T> type) {
        Class<T> implementation = elementImplementation(finder, name);
        return instanceOf(type, implementation);
    }

    @SuppressWarnings("unchecked")
    private <T> Class<T> elementImplementation(AnnotationFinder finder, String name) {
        return (Class<T>) finder.getAnnotatedValue(Configure.class, Class.class, name);
    }

    protected ParameterConverters parameterConverters(AnnotationFinder annotationFinder) {
        List<ParameterConverter> converters = new ArrayList<ParameterConverter>();
        for (Class<ParameterConverter> converterClass : annotationFinder.getAnnotatedClasses(Configure.class,
                ParameterConverter.class, "parameterConverters")) {
            converters.add(instanceOf(ParameterConverter.class, converterClass));
        }
        return new ParameterConverters().addConverters(converters);
    }

    protected <T, V extends T> T instanceOf(Class<T> type, Class<V> ofClass) {
        try {
            return (T) ofClass.newInstance();
        } catch (Exception e) {
            annotationMonitor.elementCreationFailed(ofClass, e);
            throw new InstantiationFailed(ofClass, type, e);
        }
    }

    protected AnnotationMonitor annotationMonitor() {
        return annotationMonitor;
    }

    protected AnnotationFinder annotationFinder() {
        return finder;
    }

    public Object embeddableInstance() {
        return injectEmbedder(buildEmbedder(), annotatedClass);
    }

    protected Object injectEmbedder(Embedder embedder, Class<?> annotatedClass) {
        try {
            Object instance = annotatedClass.newInstance();
            if (instance instanceof Embeddable) {
                Embeddable embeddable = (Embeddable) instance;
                embeddable.useEmbedder(embedder);
            }
            if (instance instanceof ConfigurableEmbedder) {
                ConfigurableEmbedder configurableEmbedder = (ConfigurableEmbedder) instance;
                configurableEmbedder.useConfiguration(embedder.configuration());
                configurableEmbedder.addSteps(embedder.candidateSteps());
            }
            return instance;
        } catch (Exception e) {
            annotationMonitor.elementCreationFailed(annotatedClass, e);
            throw new InstantiationFailed(annotatedClass, e);
        }
    }

    @SuppressWarnings("serial")
    public static class InstantiationFailed extends RuntimeException {

        public InstantiationFailed(Class<?> ofClass, Class<?> type, Throwable cause) {
            super("Failed to instantiate class " + ofClass + " of type " + type, cause);
        }

        public InstantiationFailed(Class<?> ofClass, Throwable cause) {
            super("Failed to instantiate class " + ofClass, cause);
        }

    }

}
