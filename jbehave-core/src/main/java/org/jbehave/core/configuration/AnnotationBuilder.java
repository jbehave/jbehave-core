package org.jbehave.core.configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.ConfigurableEmbedder;
import org.jbehave.core.Embeddable;
import org.jbehave.core.annotations.Configure;
import org.jbehave.core.annotations.UsingEmbedder;
import org.jbehave.core.annotations.UsingPaths;
import org.jbehave.core.annotations.UsingSteps;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.embedder.EmbedderControls;
import org.jbehave.core.embedder.StoryControls;
import org.jbehave.core.failures.FailureStrategy;
import org.jbehave.core.failures.PendingStepStrategy;
import org.jbehave.core.io.PathCalculator;
import org.jbehave.core.io.ResourceLoader;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.io.StoryLoader;
import org.jbehave.core.io.StoryPathResolver;
import org.jbehave.core.model.TableTransformers;
import org.jbehave.core.parsers.StepPatternParser;
import org.jbehave.core.parsers.StoryParser;
import org.jbehave.core.reporters.StepdocReporter;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.reporters.ViewGenerator;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.core.steps.ParameterControls;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.ParameterConverters.ParameterConverter;
import org.jbehave.core.steps.ScanningStepsFactory;
import org.jbehave.core.steps.StepCollector;
import org.jbehave.core.steps.StepFinder;
import org.jbehave.core.steps.StepMonitor;

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

        if (!finder.isAnnotationPresent(Configure.class)) {
            // not using annotation configuration, default to most useful
            // configuration
            return new MostUsefulConfiguration();
        }

        Configuration configuration = configurationElement(finder, "using", Configuration.class);
        configuration.useKeywords(configurationElement(finder, "keywords", Keywords.class));
        configuration.useFailureStrategy(configurationElement(finder, "failureStrategy", FailureStrategy.class));
        configuration.usePendingStepStrategy(configurationElement(finder, "pendingStepStrategy",
                PendingStepStrategy.class));
        configuration.useParanamer(configurationElement(finder, "paranamer", Paranamer.class));
        configuration.useStoryControls(configurationElement(finder, "storyControls", StoryControls.class));
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
        configuration.useTableTransformers(configurationElement(finder, "tableTransformers", TableTransformers.class));
        configuration.useParameterConverters(
                parameterConverters(finder, configuration.storyLoader(), configuration.tableTransformers()));
        configuration.useParameterControls(configurationElement(finder, "parameterControls", ParameterControls.class));
        configuration.usePathCalculator(configurationElement(finder, "pathCalculator", PathCalculator.class));
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
     * @param configuration the Configuration
     * @return A List of CandidateSteps instances
     */
    public List<CandidateSteps> buildCandidateSteps(Configuration configuration) {
        return buildStepsFactory(configuration).createCandidateSteps();
    }

    /**
     * Builds the {@link InjectableStepsFactory} using annotation
     * {@link UsingSteps} found in the annotated object instance and the
     * configuration provided
     * 
     * @param configuration the Configuration
     * @return A {@link InjectableStepsFactory}
     */
    public InjectableStepsFactory buildStepsFactory(Configuration configuration) {
        List<Object> stepsInstances = new ArrayList<Object>();
        InjectableStepsFactory factory = null;
        if (finder.isAnnotationPresent(UsingSteps.class)) {
            List<Class<Object>> stepsClasses = finder.getAnnotatedClasses(
                    UsingSteps.class, Object.class, "instances");
            if (!stepsClasses.isEmpty()) {
                for (Class<Object> stepsClass : stepsClasses) {
                    stepsInstances.add(instanceOf(Object.class, stepsClass));
                }
                factory = new InstanceStepsFactory(configuration,
                        stepsInstances);
            }
            List<String> packages = finder.getAnnotatedValues(UsingSteps.class,
                    String.class, "packages");
            if (!packages.isEmpty()) {
                String matchingNames = finder.getAnnotatedValue(UsingSteps.class,
                        String.class, "matchingNames");
                String notMatchingNames = finder.getAnnotatedValue(UsingSteps.class,
                        String.class, "notMatchingNames");
                factory = new ScanningStepsFactory(configuration,
                        packages.toArray(new String[packages.size()]))
                        .matchingNames(matchingNames).notMatchingNames(notMatchingNames);
            }
        } else {
            annotationMonitor.annotationNotFound(UsingSteps.class, annotatedClass);
        }

        if (factory == null) {
            factory = new InstanceStepsFactory(configuration);
        }
        return factory;
    }

    public Embedder buildEmbedder() {
        if (!finder.isAnnotationPresent(UsingEmbedder.class)) {
            return defaultEmbedder();
        }

        boolean batch = control(finder, "batch");
        boolean skip = control(finder, "skip");
        boolean generateViewAfterStories = control(finder, "generateViewAfterStories");
        boolean ignoreFailureInStories = control(finder, "ignoreFailureInStories");
        boolean ignoreFailureInView = control(finder, "ignoreFailureInView");
        boolean verboseFailures = control(finder, "verboseFailures");
        boolean verboseFiltering = control(finder, "verboseFiltering");
        String storyTimeouts = finder.getAnnotatedValue(UsingEmbedder.class, String.class, "storyTimeouts");
        long storyTimeoutInSecs = finder.getAnnotatedValue(UsingEmbedder.class, Long.class, "storyTimeoutInSecs");
        String storyTimeoutInSecsByPath = finder.getAnnotatedValue(UsingEmbedder.class, String.class, "storyTimeoutInSecsByPath");
        boolean failOnStoryTimeout = control(finder, "failOnStoryTimeout");
        int threads = finder.getAnnotatedValue(UsingEmbedder.class, Integer.class, "threads");
        Embedder embedder = embedder();
        EmbedderControls embedderControls = embedder.embedderControls();
        embedderControls.doBatch(batch).doSkip(skip).doGenerateViewAfterStories(generateViewAfterStories)
                .doIgnoreFailureInStories(ignoreFailureInStories).doIgnoreFailureInView(ignoreFailureInView)
                .doVerboseFailures(verboseFailures).doVerboseFiltering(verboseFiltering)
                .doFailOnStoryTimeout(failOnStoryTimeout).useThreads(threads);
        if ( storyTimeoutInSecs != 0 ){
            embedderControls.useStoryTimeoutInSecs(storyTimeoutInSecs);
        }
        if ( StringUtils.isNotBlank(storyTimeoutInSecsByPath) ){
            embedderControls.useStoryTimeoutInSecsByPath(storyTimeoutInSecsByPath);
        }
        if ( StringUtils.isNotBlank(storyTimeouts) ){
            embedderControls.useStoryTimeouts(storyTimeouts);
        }
        Configuration configuration = buildConfiguration();
        embedder.useConfiguration(configuration);
        boolean useStepsFactory = finder.getAnnotatedValue(UsingEmbedder.class, Boolean.class, "stepsFactory");
        if (useStepsFactory) {
            embedder.useStepsFactory(buildStepsFactory(configuration));
        } else {
            embedder.useCandidateSteps(buildCandidateSteps(configuration));
        }
        List<String> metaFilters = finder.getAnnotatedValues(UsingEmbedder.class, String.class, "metaFilters");
        if (!metaFilters.isEmpty()) {
            embedder.useMetaFilters(metaFilters);
        }
        Properties systemProperties = loadProperties(finder.getAnnotatedValue(UsingEmbedder.class, String.class,
                "systemProperties"));
        if (!systemProperties.isEmpty()) {
            embedder.useSystemProperties(systemProperties);
        }
        return embedder;
    }

    @SuppressWarnings("unchecked")
    protected Embedder embedder() {
        return instanceOf(Embedder.class,
                (Class<? extends Embedder>) finder.getAnnotatedValue(UsingEmbedder.class, Class.class, "embedder"));
    }
    
    protected Embedder defaultEmbedder() {
        return new Embedder();
    }

    public AnnotationFinder finder() {
        return finder;
    }
    
    public List<String> findPaths() {
        if (!finder.isAnnotationPresent(UsingPaths.class)) {
            return new ArrayList<String>();
        }

        String searchIn = finder.getAnnotatedValue(UsingPaths.class, String.class, "searchIn");
        List<String> includes = finder.getAnnotatedValues(UsingPaths.class, String.class, "includes");
        List<String> excludes = finder.getAnnotatedValues(UsingPaths.class, String.class, "excludes");
        return storyFinder().findPaths(searchIn, includes, excludes);
    }

    @SuppressWarnings("unchecked")
    private StoryFinder storyFinder() {
        return instanceOf(StoryFinder.class, (Class<? extends StoryFinder>)finder.getAnnotatedValue(UsingPaths.class, Class.class, "storyFinder"));
    }

    private boolean control(AnnotationFinder finder, String name) {
        return finder.getAnnotatedValue(UsingEmbedder.class, Boolean.class, name);
    }

    private <T> T configurationElement(AnnotationFinder finder, String name, Class<T> type) {
        Class<T> implementation = elementImplementation(finder, name);
        return instanceOf(type, implementation);
    }

    @SuppressWarnings("unchecked")
    protected <T> Class<T> elementImplementation(AnnotationFinder finder, String name) {
        return finder.getAnnotatedValue(Configure.class, Class.class, name);
    }

    private Properties loadProperties(String systemPropertiesCSV) {
        Properties properties = new Properties();
        try {
            properties.load(new ByteArrayInputStream(systemPropertiesCSV.replace(",", "\n").getBytes()));
        } catch (IOException e) {
            // return empty map
        }
        return properties;
    }

    protected ParameterConverters parameterConverters(AnnotationFinder annotationFinder,
            ResourceLoader resourceLoader, TableTransformers tableTransformers) {
        ParameterConverters parameterConverters = new ParameterConverters(resourceLoader, tableTransformers);
        for (Class<ParameterConverter> converterClass : annotationFinder.getAnnotatedClasses(Configure.class,
                ParameterConverter.class, "parameterConverters")) {
            parameterConverters.addConverters(instanceOf(ParameterConverter.class, converterClass));
        }
        return parameterConverters;
    }

    protected <T, V extends T> T instanceOf(Class<T> type, Class<V> ofClass) {
        try { 
            // by classloader constructor
            try {
                Constructor<V> constructor =
                        ofClass.getConstructor(new Class<?>[]{ClassLoader.class});
                return constructor.newInstance(annotatedClass.getClassLoader());
            }
            catch(NoSuchMethodException ns){
            }
            // by class constructor
            try {
                Constructor<V> constructor =
                        ofClass.getConstructor(new Class<?>[]{Class.class});
                return constructor.newInstance(annotatedClass);
            }
            catch(NoSuchMethodException ns){
            }                 
            // by class instance
            return ofClass.newInstance();
        }
        catch (Exception e) {
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
                configurableEmbedder.useStepsFactory(embedder.stepsFactory());
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
            super("Failed to instantiate " + ofClass + " of type " + type, cause);
        }

        public InstantiationFailed(Class<?> ofClass, Throwable cause) {
            super("Failed to instantiate " + ofClass, cause);
        }

    }

}
