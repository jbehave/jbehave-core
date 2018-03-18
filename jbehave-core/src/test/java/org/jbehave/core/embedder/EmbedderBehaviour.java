package org.jbehave.core.embedder;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.jbehave.core.Embeddable;
import org.jbehave.core.InjectableEmbedder;
import org.jbehave.core.annotations.Configure;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.UsingEmbedder;
import org.jbehave.core.annotations.When;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.embedder.Embedder.EmbedderFailureStrategy;
import org.jbehave.core.embedder.Embedder.RunningEmbeddablesFailed;
import org.jbehave.core.embedder.Embedder.RunningStoriesFailed;
import org.jbehave.core.embedder.Embedder.ViewGenerationFailed;
import org.jbehave.core.embedder.PerformableTree.RunContext;
import org.jbehave.core.failures.BatchFailures;
import org.jbehave.core.failures.FailingUponPendingStep;
import org.jbehave.core.io.StoryPathResolver;
import org.jbehave.core.io.UnderscoredCamelCaseResolver;
import org.jbehave.core.junit.AnnotatedEmbedderRunner;
import org.jbehave.core.junit.AnnotatedEmbedderUtils.ClassLoadingFailed;
import org.jbehave.core.junit.JUnitStory;
import org.jbehave.core.junit.JUnitStoryMaps;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Story;
import org.jbehave.core.model.StoryMap;
import org.jbehave.core.model.StoryMaps;
import org.jbehave.core.reporters.CrossReference;
import org.jbehave.core.reporters.PrintStreamStepdocReporter;
import org.jbehave.core.reporters.ReportsCount;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.reporters.ViewGenerator;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.StepFinder;
import org.jbehave.core.steps.Steps;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;

public class EmbedderBehaviour {

    @Test
    public void shouldMapStoriesAsEmbeddables() throws Throwable {
        // Given
        PerformableTree performableTree = mock(PerformableTree.class);
        OutputStream out = new ByteArrayOutputStream();
        EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(new PrintStream(out));
        String myEmbeddableName = MyStoryMaps.class.getName();
        List<String> classNames = asList(myEmbeddableName);
        Embeddable myEmbeddable = new MyStoryMaps();
        List<Embeddable> embeddables = asList(myEmbeddable);
        EmbedderClassLoader classLoader = mock(EmbedderClassLoader.class);
        when(classLoader.newInstance(Embeddable.class, myEmbeddableName)).thenReturn(myEmbeddable);

        // When
        Configuration configuration = new MostUsefulConfiguration();
        Embedder embedder = embedderWith(performableTree, new EmbedderControls(), monitor);
        embedder.useClassLoader(classLoader);
        embedder.useConfiguration(configuration);
        embedder.runAsEmbeddables(classNames);

        // Then
        for (Embeddable embeddable : embeddables) {
            assertThat(out.toString(), containsString("Running embeddable " + embeddable.getClass().getName()));
        }
        assertThat(MyStoryMaps.run, is(true));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldMapStoriesAsPaths() throws Throwable {
        // Given
        StoryMapper mapper = mock(StoryMapper.class);
        PerformableTree performableTree = mock(PerformableTree.class);
        EmbedderControls embedderControls = new EmbedderControls();
        OutputStream out = new ByteArrayOutputStream();
        EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(new PrintStream(out));

        Embedder embedder = embedderWith(mapper, performableTree, embedderControls, monitor);
        Configuration configuration = embedder.configuration();
        List<? extends Class<? extends Embeddable>> embeddables = asList(MyStory.class, MyOtherEmbeddable.class);
        StoryPathResolver resolver = configuration.storyPathResolver();
        List<String> storyPaths = new ArrayList<>();
        Map<String, Story> stories = new HashMap<>();
        for (Class<? extends Embeddable> embeddable : embeddables) {
            String storyPath = resolver.resolve(embeddable);
            storyPaths.add(storyPath);
            Story story = new Story(storyPath);
            stories.put(storyPath, story);
            when(performableTree.storyOfPath(configuration, storyPath)).thenReturn(story);
        }

        // When
        List<StoryMap> maps = asList(new StoryMap("filter", new HashSet<>(stories.values())));
        StoryMaps storyMaps = new StoryMaps(maps);
        when(mapper.getStoryMaps()).thenReturn(storyMaps);
        embedder.mapStoriesAsPaths(storyPaths);

        // Then
        for (String storyPath : storyPaths) {
            verify(mapper).map(Matchers.eq(stories.get(storyPath)), Matchers.any(MetaFilter.class));
            assertThat(out.toString(), containsString("Mapping story " + storyPath));
        }
        assertThatMapsViewGenerated(out);
    }

    private void assertThatMapsViewGenerated(OutputStream out) {
        assertThat(out.toString(), containsString("Generating maps view"));
    }

    @Test
    public void shouldRunStoriesAsEmbeddables() throws Throwable {
        // Given
        PerformableTree performableTree = mock(PerformableTree.class);
        EmbedderControls embedderControls = new EmbedderControls();
        OutputStream out = new ByteArrayOutputStream();
        EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(new PrintStream(out));
        String myEmbeddableName = MyEmbeddable.class.getName();
        String myOtherEmbeddableName = MyOtherEmbeddable.class.getName();
        List<String> classNames = asList(myEmbeddableName, myOtherEmbeddableName);
        Embeddable myEmbeddable = new MyEmbeddable();
        Embeddable myOtherEmbeddable = new MyOtherEmbeddable();
        List<Embeddable> embeddables = asList(myEmbeddable, myOtherEmbeddable);
        EmbedderClassLoader classLoader = mock(EmbedderClassLoader.class);
        when(classLoader.newInstance(Embeddable.class, myEmbeddableName)).thenReturn(myEmbeddable);
        when(classLoader.newInstance(Embeddable.class, myOtherEmbeddableName)).thenReturn(myOtherEmbeddable);

        // When
        Configuration configuration = new MostUsefulConfiguration();
        CandidateSteps steps = mock(CandidateSteps.class);
        Embedder embedder = embedderWith(performableTree, embedderControls, monitor);
        embedder.useClassLoader(classLoader);
        embedder.useConfiguration(configuration);
        embedder.useCandidateSteps(asList(steps));
        embedder.runAsEmbeddables(classNames);

        // Then
        for (Embeddable embeddable : embeddables) {
            assertThat(out.toString(), containsString("Running embeddable " + embeddable.getClass().getName()));
        }
    }

    private void assertThatReportsViewGenerated(OutputStream out) {
        assertThat(out.toString(), containsString("Generating reports view"));
        assertThat(out.toString(), containsString("Reports view generated"));
    }

    @Test
    public void shouldNotRunStoriesAsEmbeddablesIfAbstract() throws Throwable {
        // Given
        PerformableTree performableTree = mock(PerformableTree.class);
        EmbedderControls embedderControls = new EmbedderControls();
        OutputStream out = new ByteArrayOutputStream();
        EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(new PrintStream(out));
        String myEmbeddableName = MyAbstractEmbeddable.class.getName();
        String myOtherEmbeddableName = MyOtherEmbeddable.class.getName();
        List<String> classNames = asList(myEmbeddableName, myOtherEmbeddableName);
        Embeddable myEmbeddable = new MyEmbeddable();
        Embeddable myOtherEmbeddable = new MyOtherEmbeddable();
        EmbedderClassLoader classLoader = mock(EmbedderClassLoader.class);
        when(classLoader.isAbstract(myEmbeddableName)).thenReturn(true);
        when(classLoader.newInstance(Embeddable.class, myEmbeddableName)).thenReturn(myEmbeddable);
        when(classLoader.isAbstract(myOtherEmbeddableName)).thenReturn(false);
        when(classLoader.newInstance(Embeddable.class, myOtherEmbeddableName)).thenReturn(myOtherEmbeddable);

        // When
        Embedder embedder = embedderWith(performableTree, embedderControls, monitor);
        embedder.useClassLoader(classLoader);
        embedder.configuration().useStoryPathResolver(new UnderscoredCamelCaseResolver());
        embedder.runAsEmbeddables(classNames);

        // Then
        assertThat(out.toString(), not(containsString("Running embeddable " + myEmbeddableName)));
        assertThat(out.toString(), containsString("Running embeddable " + myOtherEmbeddableName));
    }

    @Test
    public void shouldNotRunStoriesAsEmbeddablesIfSkipFlagIsSet() throws Throwable {
        // Given
        PerformableTree performableTree = mock(PerformableTree.class);
        EmbedderControls embedderControls = new EmbedderControls().doSkip(true);
        OutputStream out = new ByteArrayOutputStream();
        EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(new PrintStream(out));
        String myEmbeddableName = MyEmbeddable.class.getName();
        String myOtherEmbeddableName = MyOtherEmbeddable.class.getName();
        List<String> classNames = asList(myEmbeddableName, myOtherEmbeddableName);
        Embeddable myEmbeddable = new MyEmbeddable();
        Embeddable myOtherEmbeddable = new MyOtherEmbeddable();
        List<Embeddable> embeddables = asList(myEmbeddable, myOtherEmbeddable);
        EmbedderClassLoader classLoader = mock(EmbedderClassLoader.class);
        when(classLoader.newInstance(Embeddable.class, myEmbeddableName)).thenReturn(myEmbeddable);
        when(classLoader.newInstance(Embeddable.class, myOtherEmbeddableName)).thenReturn(myOtherEmbeddable);

        // When
        Embedder embedder = embedderWith(performableTree, embedderControls, monitor);
        embedder.useClassLoader(classLoader);
        embedder.configuration().useStoryPathResolver(new UnderscoredCamelCaseResolver());
        embedder.runAsEmbeddables(classNames);

        // Then
        for (Embeddable embeddable : embeddables) {
            assertThat(out.toString(), not(containsString("Running embeddable " + embeddable.getClass().getName())));
        }
    }

    @Test(expected = RunningEmbeddablesFailed.class)
    public void shouldThrowExceptionUponFailingStoriesAsEmbeddablesIfIgnoreFailureInStoriesFlagIsNotSet()
            throws Throwable {
        // Given
        PerformableTree performableTree = mock(PerformableTree.class);
        EmbedderControls embedderControls = new EmbedderControls();
        OutputStream out = new ByteArrayOutputStream();
        EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(new PrintStream(out));
        String myEmbeddableName = MyEmbeddable.class.getName();
        String myOtherEmbeddableName = MyOtherEmbeddable.class.getName();
        List<String> classNames = asList(myEmbeddableName, myOtherEmbeddableName);
        Embeddable myEmbeddable = new MyFailingEmbeddable();
        Embeddable myOtherEmbeddable = new MyOtherEmbeddable();
        EmbedderClassLoader classLoader = mock(EmbedderClassLoader.class);
        when(classLoader.newInstance(Embeddable.class, myEmbeddableName)).thenReturn(myEmbeddable);
        when(classLoader.newInstance(Embeddable.class, myOtherEmbeddableName)).thenReturn(myOtherEmbeddable);

        // When
        Embedder embedder = embedderWith(performableTree, embedderControls, monitor);
        embedder.useClassLoader(classLoader);
        embedder.runAsEmbeddables(classNames);

        // Then fail as expected
    }

    @Test
    public void shouldNotThrowExceptionUponFailingStoriesAsEmbeddablesIfIgnoreFailureFlagsAreSet() throws Throwable {
        // Given
        PerformableTree performableTree = mock(PerformableTree.class);
        EmbedderControls embedderControls = new EmbedderControls().doIgnoreFailureInStories(true)
                .doIgnoreFailureInView(true);
        OutputStream out = new ByteArrayOutputStream();
        EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(new PrintStream(out));
        String myEmbeddableName = MyFailingEmbeddable.class.getName();
        String myOtherEmbeddableName = MyOtherEmbeddable.class.getName();
        List<String> classNames = asList(myEmbeddableName, myOtherEmbeddableName);
        Embeddable myEmbeddable = new MyFailingEmbeddable();
        Embeddable myOtherEmbeddable = new MyOtherEmbeddable();
        EmbedderClassLoader classLoader = mock(EmbedderClassLoader.class);
        when(classLoader.newInstance(Embeddable.class, myEmbeddableName)).thenReturn(myEmbeddable);
        when(classLoader.newInstance(Embeddable.class, myOtherEmbeddableName)).thenReturn(myOtherEmbeddable);

        // When
        Embedder embedder = embedderWith(performableTree, embedderControls, monitor);
        embedder.useClassLoader(classLoader);
        embedder.runAsEmbeddables(classNames);

        // Then
        assertThat(out.toString(), containsString("Running embeddable " + myEmbeddableName));
        assertThat(out.toString(), containsString("Failed to run embeddable " + myEmbeddableName));
        assertThat(out.toString(), containsString("Running embeddable " + myOtherEmbeddableName));
        assertThat(out.toString(), not(containsString("Failed to run embeddable " + myOtherEmbeddableName)));

    }

    @Test
    public void shouldRunStoriesAsEmbeddablesInBatchIfBatchFlagIsSet() throws Throwable {
        // Given
        PerformableTree performableTree = mock(PerformableTree.class);
        EmbedderControls embedderControls = new EmbedderControls().doBatch(true);
        OutputStream out = new ByteArrayOutputStream();
        EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(new PrintStream(out));
        String myEmbeddableName = MyEmbeddable.class.getName();
        String myOtherEmbeddableName = MyOtherEmbeddable.class.getName();
        List<String> classNames = asList(myEmbeddableName, myOtherEmbeddableName);
        Embeddable myEmbeddable = new MyEmbeddable();
        Embeddable myOtherEmbeddable = new MyOtherEmbeddable();
        List<Embeddable> embeddables = asList(myEmbeddable, myOtherEmbeddable);
        EmbedderClassLoader classLoader = mock(EmbedderClassLoader.class);
        when(classLoader.newInstance(Embeddable.class, myEmbeddableName)).thenReturn(myEmbeddable);
        when(classLoader.newInstance(Embeddable.class, myOtherEmbeddableName)).thenReturn(myOtherEmbeddable);

        // When
        Embedder embedder = embedderWith(performableTree, embedderControls, monitor);
        embedder.useClassLoader(classLoader);
        embedder.runAsEmbeddables(classNames);

        // Then
        for (Embeddable story : embeddables) {
            String name = story.getClass().getName();
            assertThat(out.toString(), containsString("Running embeddable " + name));
        }
    }

    @Test(expected = RunningEmbeddablesFailed.class)
    public void shouldThrowExceptionUponFailingStoriesAsEmbeddablesInBatchIfIgnoreFailureInStoriesFlagIsNotSet()
            throws Throwable {
        // Given
        PerformableTree performableTree = mock(PerformableTree.class);
        EmbedderControls embedderControls = new EmbedderControls().doBatch(true);
        OutputStream out = new ByteArrayOutputStream();
        EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(new PrintStream(out));
        String myStoryName = MyStory.class.getName();
        String myOtherStoryName = MyOtherEmbeddable.class.getName();
        List<String> classNames = asList(myStoryName, myOtherStoryName);
        Embeddable myStory = new MyFailingEmbeddable();
        Embeddable myOtherStory = new MyOtherEmbeddable();
        EmbedderClassLoader classLoader = mock(EmbedderClassLoader.class);
        when(classLoader.newInstance(Embeddable.class, myStoryName)).thenReturn(myStory);
        when(classLoader.newInstance(Embeddable.class, myOtherStoryName)).thenReturn(myOtherStory);

        // When
        Embedder embedder = embedderWith(performableTree, embedderControls, monitor);
        embedder.useClassLoader(classLoader);
        embedder.runAsEmbeddables(classNames);

        // Then fail as expected

    }

    @Test
    public void shouldRunFailingStoriesAsEmbeddablesInBatchIfBatchFlagIsSet() throws Throwable {
        // Given
        PerformableTree performableTree = mock(PerformableTree.class);
        EmbedderControls embedderControls = new EmbedderControls().doBatch(true).doIgnoreFailureInStories(true)
                .doIgnoreFailureInView(true);
        OutputStream out = new ByteArrayOutputStream();
        EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(new PrintStream(out));
        String myStoryName = MyFailingEmbeddable.class.getName();
        String myOtherStoryName = MyOtherEmbeddable.class.getName();
        List<String> classNames = asList(myStoryName, myOtherStoryName);
        Embeddable myStory = new MyFailingEmbeddable();
        Embeddable myOtherStory = new MyOtherEmbeddable();
        List<Embeddable> embeddables = asList(myStory, myOtherStory);
        EmbedderClassLoader classLoader = mock(EmbedderClassLoader.class);
        when(classLoader.newInstance(Embeddable.class, myStoryName)).thenReturn(myStory);
        when(classLoader.newInstance(Embeddable.class, myOtherStoryName)).thenReturn(myOtherStory);

        // When
        Embedder embedder = embedderWith(performableTree, embedderControls, monitor);
        embedder.useClassLoader(classLoader);
        embedder.runAsEmbeddables(classNames);

        // Then
        for (Embeddable embeddable : embeddables) {
            String storyName = embeddable.getClass().getName();
            assertThat(out.toString(), containsString("Running embeddable " + storyName));
        }
        assertThat(out.toString(), containsString("Failed to run batch"));
    }

    @Test
    public void shouldNotGenerateViewWhenRunningStoriesAsEmbeddablesIfGenerateViewAfterStoriesFlagIsNotSet()
            throws Throwable {
        // Given
        PerformableTree performableTree = mock(PerformableTree.class);
        EmbedderControls embedderControls = new EmbedderControls().doGenerateViewAfterStories(false);
        OutputStream out = new ByteArrayOutputStream();
        EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(new PrintStream(out));
        String myEmbeddableName = MyEmbeddable.class.getName();
        String myOtherEmbeddableName = MyOtherEmbeddable.class.getName();
        List<String> classNames = asList(myEmbeddableName, myOtherEmbeddableName);
        Embeddable myEmbeddable = new MyEmbeddable();
        Embeddable myOtherEmbeddable = new MyOtherEmbeddable();
        List<Embeddable> embeddables = asList(myEmbeddable, myOtherEmbeddable);
        EmbedderClassLoader classLoader = mock(EmbedderClassLoader.class);
        when(classLoader.newInstance(Embeddable.class, myEmbeddableName)).thenReturn(myEmbeddable);
        when(classLoader.newInstance(Embeddable.class, myOtherEmbeddableName)).thenReturn(myOtherEmbeddable);

        // When
        Embedder embedder = embedderWith(performableTree, embedderControls, monitor);
        embedder.useClassLoader(classLoader);
        embedder.runAsEmbeddables(classNames);

        // Then
        for (Embeddable embeddable : embeddables) {
            assertThat(out.toString(), containsString("Running embeddable " + embeddable.getClass().getName()));
        }
        assertThat(out.toString(), not(containsString("Generating stories view")));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldRunStoriesAsPaths() throws Throwable {
        // Given
        PerformableTree performableTree = mock(PerformableTree.class);
        EmbedderControls embedderControls = new EmbedderControls();
        EmbedderMonitor embedderMonitor = mock(EmbedderMonitor.class);
        OutputStream out = new ByteArrayOutputStream();
        EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(new PrintStream(out));
        List<? extends Class<? extends Embeddable>> embeddables = asList(MyStory.class, MyOtherEmbeddable.class);

        Embedder embedder = embedderWith(performableTree, embedderControls, monitor);
        InjectableStepsFactory stepsFactory = embedder.stepsFactory();
        MetaFilter filter = embedder.metaFilter();
        final StoryReporter storyReporter = mock(StoryReporter.class);
        MostUsefulConfiguration configuration = new MostUsefulConfiguration() {
            @Override
            public StoryReporter storyReporter(String storyPath) {
                return storyReporter;
            }
        };
        embedder.useConfiguration(configuration);
        StoryPathResolver resolver = configuration.storyPathResolver();

        List<String> storyPaths = new ArrayList<>();
        Map<String, Story> stories = new HashMap<>();
        for (Class<? extends Embeddable> embeddable : embeddables) {
            String storyPath = resolver.resolve(embeddable);
            storyPaths.add(storyPath);
            Story story = mockStory(Meta.EMPTY);
            stories.put(storyPath, story);
            when(performableTree.storyOfPath(configuration, storyPath)).thenReturn(story);
            when(story.getPath()).thenReturn(storyPath);
            assertThat(configuration.storyReporter(storyPath), sameInstance(storyReporter));
        }
		RunContext runContext = new RunContext(configuration, stepsFactory, embedderMonitor, filter, new BatchFailures());
        when(
                performableTree.newRunContext(isA(Configuration.class), isA(InjectableStepsFactory.class), isA(EmbedderMonitor.class),
                        isA(MetaFilter.class), isA(BatchFailures.class))).thenReturn(runContext);

        // When
        embedder.runStoriesAsPaths(storyPaths);

        // Then
        for (String storyPath : storyPaths) {
            verify(performableTree).perform(Matchers.isA(RunContext.class), Matchers.eq(stories.get(storyPath)));
            assertThat(out.toString(), containsString("Running story " + storyPath));
        }
        assertThatReportsViewGenerated(out);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldRunStoriesApplyingFilter() throws Throwable {
        // Given
        PerformableTree performableTree = mock(PerformableTree.class);
        EmbedderControls embedderControls = new EmbedderControls();
        OutputStream out = new ByteArrayOutputStream();
        EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(new PrintStream(out));
        List<? extends Class<? extends Embeddable>> embeddables = asList(MyStory.class, MyOtherEmbeddable.class);

        Embedder embedder = embedderWith(performableTree, embedderControls, monitor);
        final StoryReporter storyReporter = mock(StoryReporter.class);

        Configuration configuration = new MostUsefulConfiguration() {
            @Override
            public StoryReporter storyReporter(String storyPath) {
                return storyReporter;
            }
        };
        embedder.useConfiguration(configuration);
        InjectableStepsFactory stepsFactory = embedder.stepsFactory();
        StoryPathResolver resolver = configuration.storyPathResolver();
        List<String> storyPaths = new ArrayList<>();
        Map<String, Story> stories = new HashMap<>();
        Meta meta = mock(Meta.class);
        for (Class<? extends Embeddable> embeddable : embeddables) {
            String storyPath = resolver.resolve(embeddable);
            storyPaths.add(storyPath);
            Story story = mockStory(Meta.EMPTY);
            when(story.getMeta()).thenReturn(meta);
            stories.put(storyPath, story);
            when(performableTree.storyOfPath(configuration, storyPath)).thenReturn(story);
            when(story.getPath()).thenReturn(storyPath);
            assertThat(configuration.storyReporter(storyPath), sameInstance(storyReporter));
        }

        // When
        MetaFilter filter = mock(MetaFilter.class);
        when(filter.allow(meta)).thenReturn(false);
        RunContext runContext = new RunContext(configuration, stepsFactory, monitor, filter, new BatchFailures());
        when(
                performableTree.newRunContext(isA(Configuration.class), isA(InjectableStepsFactory.class), isA(EmbedderMonitor.class),
                        isA(MetaFilter.class), isA(BatchFailures.class))).thenReturn(
                runContext);
        embedder.runStoriesAsPaths(storyPaths);

        // Then
        for (String storyPath : storyPaths) {
            verify(performableTree, never()).perform(runContext, stories.get(storyPath));
        }
        assertThatReportsViewGenerated(out);
        assertThat(embedder.hasExecutorService(), is(false));

    }

    @Test
    public void shouldProcessSystemProperties() throws Throwable {

        // Given
        PerformableTree performableTree = mock(PerformableTree.class);
        EmbedderControls embedderControls = new EmbedderControls();
        OutputStream out = new ByteArrayOutputStream();
        EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(new PrintStream(out));
        Embedder embedder = embedderWith(performableTree, embedderControls, monitor);

        // When
        embedder.processSystemProperties();

        // Then
        assertThat(out.toString(), containsString("Processing system properties " + embedder.systemProperties()));
        assertThat(out.toString(), not(containsString("System property")));

        // When
        Properties systemProperties = new Properties();
        systemProperties.setProperty("first", "one");
        systemProperties.setProperty("second", "");
        embedder.useSystemProperties(systemProperties);
        embedder.processSystemProperties();

        // Then
        assertThat(out.toString(), containsString("Processing system properties " + systemProperties));
        assertThat(out.toString(), containsString("System property 'first' set to 'one'"));
        assertThat(out.toString(), containsString("System property 'second' set to ''"));

    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldNotRunStoriesIfSkipFlagIsSet() throws Throwable {
        // Given
        PerformableTree performableTree = mock(PerformableTree.class);
        EmbedderControls embedderControls = new EmbedderControls().doSkip(true);
        OutputStream out = new ByteArrayOutputStream();
        EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(new PrintStream(out));
        List<? extends Class<? extends Embeddable>> embeddables = asList(MyStory.class, MyOtherEmbeddable.class);

        Embedder embedder = embedderWith(performableTree, embedderControls, monitor);
        Configuration configuration = embedder.configuration();
        InjectableStepsFactory stepsFactory = embedder.stepsFactory();
        MetaFilter filter = embedder.metaFilter();
        StoryPathResolver resolver = configuration.storyPathResolver();
        List<String> storyPaths = new ArrayList<>();
        Map<String, Story> stories = new HashMap<>();
        for (Class<? extends Embeddable> embeddable : embeddables) {
            String storyPath = resolver.resolve(embeddable);
            storyPaths.add(storyPath);
            Story story = mockStory(Meta.EMPTY);
            stories.put(storyPath, story);
            when(performableTree.storyOfPath(configuration, storyPath)).thenReturn(story);
            when(story.getPath()).thenReturn(storyPath);
        }
        RunContext runContext = new RunContext(configuration, stepsFactory, monitor, filter, new BatchFailures());
        when(
                performableTree.newRunContext(isA(Configuration.class), isA(InjectableStepsFactory.class), isA(EmbedderMonitor.class),
                        isA(MetaFilter.class), isA(BatchFailures.class))).thenReturn(
                runContext);

        // When
        embedder.runStoriesAsPaths(storyPaths);

        // Then
        for (String storyPath : storyPaths) {
            verify(performableTree, never()).perform(runContext, stories.get(storyPath));
            assertThat(out.toString(), not(containsString("Running story " + storyPath)));
        }
        assertThat(out.toString(), containsString("Skipped stories " + storyPaths));
    }


    @SuppressWarnings("unchecked")
    @Test
    public void shouldNotThrowExceptionUponFailingStoriesAsPathsIfIgnoreFailureInStoriesFlagIsSet() throws Throwable {
        // Given
        PerformableTree performableTree = mock(PerformableTree.class);
        EmbedderControls embedderControls = new EmbedderControls().doIgnoreFailureInStories(true);
        OutputStream out = new ByteArrayOutputStream();
        EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(new PrintStream(out));
        List<? extends Class<? extends Embeddable>> embeddables = asList(MyStory.class, MyOtherEmbeddable.class);

        Embedder embedder = embedderWith(performableTree, embedderControls, monitor);
        Configuration configuration = embedder.configuration();
        InjectableStepsFactory stepsFactory = embedder.stepsFactory();
        MetaFilter filter = embedder.metaFilter();
        StoryPathResolver resolver = configuration.storyPathResolver();
        List<String> storyPaths = new ArrayList<>();
        Map<String, Story> stories = new HashMap<>();
        for (Class<? extends Embeddable> embeddable : embeddables) {
            String storyPath = resolver.resolve(embeddable);
            storyPaths.add(storyPath);
            Story story = mockStory(Meta.EMPTY);
            stories.put(storyPath, story);
            when(performableTree.storyOfPath(configuration, storyPath)).thenReturn(story);
            when(story.getPath()).thenReturn(storyPath);
        }
        RunContext runContext = new RunContext(configuration, stepsFactory, monitor, filter, new BatchFailures());
        when(
                performableTree.newRunContext(isA(Configuration.class), isA(InjectableStepsFactory.class), isA(EmbedderMonitor.class),
                        isA(MetaFilter.class), isA(BatchFailures.class))).thenReturn(
                runContext);

        for (String storyPath : storyPaths) {
            doThrow(new RuntimeException(storyPath + " failed")).when(performableTree).perform(runContext, stories.get(storyPath));
        }

        // When
        embedder.runStoriesAsPaths(storyPaths);
        TimeUnit.SECONDS.sleep(2);

        // Then
        for (String storyPath : storyPaths) {
            assertThat(out.toString(), containsString("Running story " + storyPath));
            assertThat(out.toString(), containsString("Failed to run story " + storyPath));
        }

    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldRunStoriesAsPathsInBatchIfBatchFlagIsSet() throws Throwable {
        // Given
        PerformableTree performableTree = mock(PerformableTree.class);
        EmbedderControls embedderControls = new EmbedderControls().doBatch(true);
        OutputStream out = new ByteArrayOutputStream();
        EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(new PrintStream(out));
        List<? extends Class<? extends Embeddable>> embeddables = asList(MyStory.class, MyOtherEmbeddable.class);

        Embedder embedder = embedderWith(performableTree, embedderControls, monitor);
        Configuration configuration = embedder.configuration();
        InjectableStepsFactory stepsFactory = embedder.stepsFactory();
        MetaFilter filter = embedder.metaFilter();
        StoryPathResolver resolver = configuration.storyPathResolver();
        List<String> storyPaths = new ArrayList<>();
        Map<String, Story> stories = new HashMap<>();
        for (Class<? extends Embeddable> embeddable : embeddables) {
            String storyPath = resolver.resolve(embeddable);
            storyPaths.add(storyPath);
            Story story = mockStory(Meta.EMPTY);
            stories.put(storyPath, story);
            when(performableTree.storyOfPath(configuration, storyPath)).thenReturn(story);
            when(story.getPath()).thenReturn(storyPath);
        }
        RunContext runContext = new RunContext(configuration, stepsFactory, monitor, filter, new BatchFailures());
        when(
                performableTree.newRunContext(isA(Configuration.class), isA(InjectableStepsFactory.class), isA(EmbedderMonitor.class),
                        isA(MetaFilter.class), isA(BatchFailures.class))).thenReturn(
                runContext);

        for (String storyPath : storyPaths) {
            doNothing().when(performableTree).perform(runContext, stories.get(storyPath));
        }
        
        // When
        embedder.runStoriesAsPaths(storyPaths);

        // Then
        for (String storyPath : storyPaths) {
            assertThat(out.toString(), containsString("Running story " + storyPath));
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldRunFailingStoriesAsPathsInBatchIfBatchFlagIsSet() throws Throwable {
        // Given
        PerformableTree performableTree = mock(PerformableTree.class);
        EmbedderControls embedderControls = new EmbedderControls().doBatch(true).doIgnoreFailureInStories(true);
        OutputStream out = new ByteArrayOutputStream();
        EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(new PrintStream(out));
        List<? extends Class<? extends Embeddable>> embeddables = asList(MyStory.class, MyOtherEmbeddable.class);

        Embedder embedder = embedderWith(performableTree, embedderControls, monitor);
        Configuration configuration = embedder.configuration();
        InjectableStepsFactory stepsFactory = embedder.stepsFactory();
        MetaFilter filter = embedder.metaFilter();
        StoryPathResolver resolver = configuration.storyPathResolver();
        List<String> storyPaths = new ArrayList<>();
        Map<String, Story> stories = new HashMap<>();
        for (Class<? extends Embeddable> embeddable : embeddables) {
            String storyPath = resolver.resolve(embeddable);
            storyPaths.add(storyPath);
            Story story = mockStory(Meta.EMPTY);
            stories.put(storyPath, story);
            when(performableTree.storyOfPath(configuration, storyPath)).thenReturn(story);
            when(story.getPath()).thenReturn(storyPath);
        }
        RunContext runContext = new RunContext(configuration, stepsFactory, monitor, filter, new BatchFailures());
        when(
                performableTree.newRunContext(isA(Configuration.class), isA(InjectableStepsFactory.class), isA(EmbedderMonitor.class),
                        isA(MetaFilter.class), isA(BatchFailures.class))).thenReturn(
                runContext);

        BatchFailures failures = new BatchFailures();
        for (String storyPath : storyPaths) {
            RuntimeException thrown = new RuntimeException(storyPath + " failed");
            failures.put(storyPath, thrown);
            doThrow(thrown).when(performableTree).perform(runContext, stories.get(storyPath));
        }

        // When
        embedder.runStoriesAsPaths(storyPaths);

        // Then
        String output = out.toString();
        for (String storyPath : storyPaths) {
            assertThat(output, containsString("Running story " + storyPath));
            assertThat(output, containsString("Failed to run story " + storyPath));
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldNotGenerateViewWhenRunningStoriesAsPathsIfGenerateViewAfterStoriesFlagIsNotSet() throws Throwable {
        // Given
        PerformableTree performableTree = mock(PerformableTree.class);
        EmbedderControls embedderControls = new EmbedderControls().doGenerateViewAfterStories(false);
        OutputStream out = new ByteArrayOutputStream();
        EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(new PrintStream(out));
        List<? extends Class<? extends Embeddable>> embeddables = asList(MyStory.class, MyOtherEmbeddable.class);

        Embedder embedder = embedderWith(performableTree, embedderControls, monitor);
        Configuration configuration = embedder.configuration();
        InjectableStepsFactory stepsFactory = embedder.stepsFactory();
        MetaFilter filter = embedder.metaFilter();

        StoryPathResolver resolver = configuration.storyPathResolver();
        List<String> storyPaths = new ArrayList<>();
        Map<String, Story> stories = new HashMap<>();
        for (Class<? extends Embeddable> embeddable : embeddables) {
            String storyPath = resolver.resolve(embeddable);
            storyPaths.add(storyPath);
            Story story = mockStory(Meta.EMPTY);
            stories.put(storyPath, story);
            when(performableTree.storyOfPath(configuration, storyPath)).thenReturn(story);
            when(story.getPath()).thenReturn(storyPath);
        }
        RunContext runContext = new RunContext(configuration, stepsFactory, monitor, filter, new BatchFailures());
        when(
                performableTree.newRunContext(isA(Configuration.class), isA(InjectableStepsFactory.class), isA(EmbedderMonitor.class),
                        isA(MetaFilter.class), isA(BatchFailures.class))).thenReturn(
                runContext);

        // When
        embedder.runStoriesAsPaths(storyPaths);

        // Then
        for (String storyPath : storyPaths) {
            verify(performableTree).perform(runContext, stories.get(storyPath));
            assertThat(out.toString(), containsString("Running story " + storyPath));
        }
        assertThat(out.toString(), not(containsString("Generating stories view")));
        assertThat(out.toString(), not(containsString("Stories view generated")));
    }

    @Test
    public void shouldRunStoriesWithAnnotatedEmbedderRunnerIfEmbeddable() throws Throwable {
        // Given
        Embedder embedder = new Embedder();
        embedder.useClassLoader(new EmbedderClassLoader(this.getClass().getClassLoader()));
        String runWithEmbedderRunner = RunningWithAnnotatedEmbedderRunner.class.getName();
        // When
        embedder.runStoriesWithAnnotatedEmbedderRunner(asList(runWithEmbedderRunner));
        // Then
        assertThat(RunningWithAnnotatedEmbedderRunner.hasRun, is(true));
    }

    @Test
    public void shouldNotRunStoriesWithAnnotatedEmbedderRunnerIfNotEmbeddable() throws Throwable {
        // Given
        Embedder embedder = new Embedder();
        embedder.useClassLoader(new EmbedderClassLoader(this.getClass().getClassLoader()));
        String runWithEmbedderRunner = NotEmbeddableWithAnnotatedEmbedderRunner.class.getName();
        // When
        embedder.runStoriesWithAnnotatedEmbedderRunner(asList(runWithEmbedderRunner));
        // Then
        assertThat(NotEmbeddableWithAnnotatedEmbedderRunner.hasRun, is(false));
    }

    @Test(expected = RuntimeException.class)
    public void shouldRethowFailuresWhenRunningWithAnnotatedEmbedderRunner() throws Throwable {
        // Given
        Embedder embedder = new Embedder();
        embedder.useClassLoader(new EmbedderClassLoader(this.getClass().getClassLoader()));
        String runWithEmbedderRunner = FailingWithAnnotatedEmbedderRunner.class.getName();
        // When
        embedder.runStoriesWithAnnotatedEmbedderRunner(asList(runWithEmbedderRunner));
        // Then fail as expected
    }

    @Test(expected = ClassLoadingFailed.class)
    public void shouldFailWhenRunningInexistingStoriesWithAnnotatedEmbedderRunner() throws Throwable {
        // Given
        Embedder embedder = new Embedder();
        embedder.useClassLoader(new EmbedderClassLoader(this.getClass().getClassLoader()));
        String runWithEmbedderRunner = "InexistingRunner";
        // When
        embedder.runStoriesWithAnnotatedEmbedderRunner(asList(runWithEmbedderRunner));
        // Then fail as expected
    }

    @Test
    public void shouldGenerateReportsViewFromExistingReports() throws Throwable {
        // Given
        PerformableTree performableTree = mock(PerformableTree.class);
        EmbedderControls embedderControls = new EmbedderControls().doGenerateViewAfterStories(false);
        OutputStream out = new ByteArrayOutputStream();
        EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(new PrintStream(out));
        ViewGenerator viewGenerator = mock(ViewGenerator.class);

        Embedder embedder = embedderWith(performableTree, embedderControls, monitor);
        embedder.configuration().useViewGenerator(viewGenerator);

        File outputDirectory = new File("target/output");
        List<String> formats = asList("html");
        Properties viewResources = new Properties();
        when(viewGenerator.getReportsCount()).thenReturn(new ReportsCount(2, 0, 1, 2, 0, 0, 1, 0));
        embedder.generateReportsView(outputDirectory, formats, viewResources);

        // Then
        verify(viewGenerator).generateReportsView(outputDirectory, formats, viewResources);
        assertThatReportsViewGenerated(out);
    }

    @Test(expected = RunningStoriesFailed.class)
    public void shouldFailWhenGeneratingReportsViewWithFailedSteps() throws Throwable {
        // Given
        PerformableTree performableTree = mock(PerformableTree.class);
        EmbedderControls embedderControls = new EmbedderControls().doGenerateViewAfterStories(false);
        OutputStream out = new ByteArrayOutputStream();
        EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(new PrintStream(out));
        ViewGenerator viewGenerator = mock(ViewGenerator.class);

        Embedder embedder = embedderWith(performableTree, embedderControls, monitor);
        embedder.configuration().useViewGenerator(viewGenerator);

        File outputDirectory = new File("target/output");
        List<String> formats = asList("html");
        Properties viewResources = new Properties();
        when(viewGenerator.getReportsCount()).thenReturn(new ReportsCount(2, 0, 0, 2, 1, 0, 0, 1));
        embedder.generateReportsView(outputDirectory, formats, viewResources);

        // Then
        verify(viewGenerator).generateReportsView(outputDirectory, formats, viewResources);
        assertThatReportsViewGenerated(out);
    }

    @Test(expected = RunningStoriesFailed.class)
    public void shouldFailWhenGeneratingReportsViewWithPendingSteps() throws Throwable {
        // Given
        PerformableTree performableTree = mock(PerformableTree.class);
        EmbedderControls embedderControls = new EmbedderControls().doGenerateViewAfterStories(false);
        OutputStream out = new ByteArrayOutputStream();
        EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(new PrintStream(out));
        ViewGenerator viewGenerator = mock(ViewGenerator.class);

        Embedder embedder = embedderWith(performableTree, embedderControls, monitor);
        embedder.configuration().useViewGenerator(viewGenerator).usePendingStepStrategy(new FailingUponPendingStep());

        File outputDirectory = new File("target/output");
        List<String> formats = asList("html");
        Properties viewResources = new Properties();
        when(viewGenerator.getReportsCount()).thenReturn(new ReportsCount(2, 0, 1, 2, 0, 0, 1, 0));
        embedder.generateReportsView(outputDirectory, formats, viewResources);

        // Then
        verify(viewGenerator).generateReportsView(outputDirectory, formats, viewResources);
        assertThatReportsViewGenerated(out);
    }

    @Test
    public void shouldNotGenerateViewIfSkipFlagIsSet() throws Throwable {
        // Given
        PerformableTree performableTree = mock(PerformableTree.class);
        EmbedderControls embedderControls = new EmbedderControls().doSkip(true);
        OutputStream out = new ByteArrayOutputStream();
        EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(new PrintStream(out));
        ViewGenerator viewGenerator = mock(ViewGenerator.class);

        Embedder embedder = embedderWith(performableTree, embedderControls, monitor);
        embedder.configuration().useStoryReporterBuilder(new StoryReporterBuilder().withDefaultFormats());
        embedder.configuration().useViewGenerator(viewGenerator);

        File outputDirectory = new File("target/output");
        List<String> formats = asList("html");
        Properties viewResources = new Properties();
        embedder.generateReportsView(outputDirectory, formats, viewResources);

        // Then
        verify(viewGenerator, never()).generateReportsView(outputDirectory, formats, viewResources);
        assertThat(out.toString(), not(containsString("Generating stories view")));
        assertThat(out.toString(), not(containsString("Stories view generated")));
    }

    @Test(expected = ViewGenerationFailed.class)
    public void shouldThrowExceptionIfViewGenerationFails() throws Throwable {
        // Given
        PerformableTree performableTree = mock(PerformableTree.class);
        EmbedderControls embedderControls = new EmbedderControls();
        OutputStream out = new ByteArrayOutputStream();
        EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(new PrintStream(out));
        ViewGenerator viewGenerator = mock(ViewGenerator.class);

        Embedder embedder = embedderWith(performableTree, embedderControls, monitor);
        embedder.configuration().useViewGenerator(viewGenerator);
        File outputDirectory = new File("target/output");
        List<String> formats = asList("html");
        Properties viewResources = new Properties();
        doThrow(new RuntimeException()).when(viewGenerator)
                .generateReportsView(outputDirectory, formats, viewResources);
        embedder.generateReportsView(outputDirectory, formats, viewResources);

        // Then fail as expected
    }

    @Test(expected = RunningStoriesFailed.class)
    public void shouldThrowExceptionIfScenariosFailedAndIgnoreFlagIsNotSet() throws Throwable {
        // Given
        PerformableTree performableTree = mock(PerformableTree.class);
        EmbedderControls embedderControls = new EmbedderControls();
        OutputStream out = new ByteArrayOutputStream();
        EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(new PrintStream(out));
        ViewGenerator viewGenerator = mock(ViewGenerator.class);

        Embedder embedder = embedderWith(performableTree, embedderControls, monitor);
        embedder.configuration().useViewGenerator(viewGenerator);
        File outputDirectory = new File("target/output");
        List<String> formats = asList("html");
        Properties viewResources = new Properties();
        when(viewGenerator.getReportsCount()).thenReturn(new ReportsCount(1, 0, 1, 2, 1, 1, 1, 1));
        embedder.generateReportsView(outputDirectory, formats, viewResources);

        // Then fail as expected
    }

    @Test(expected = RunningStoriesFailed.class)
    public void shouldThrowExceptionIfNoScenariosRunForStoriesAndIgnoreFlagIsNotSet() throws Throwable {
        // Given
        PerformableTree performableTree = mock(PerformableTree.class);
        EmbedderControls embedderControls = new EmbedderControls();
        OutputStream out = new ByteArrayOutputStream();
        EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(new PrintStream(out));
        ViewGenerator viewGenerator = mock(ViewGenerator.class);

        Embedder embedder = embedderWith(performableTree, embedderControls, monitor);
        embedder.configuration().useViewGenerator(viewGenerator);
        File outputDirectory = new File("target/output");
        List<String> formats = asList("html");
        Properties viewResources = new Properties();
        when(viewGenerator.getReportsCount()).thenReturn(new ReportsCount(1, 0, 0, 0, 0, 0, 0, 1));
        embedder.generateReportsView(outputDirectory, formats, viewResources);

        // Then fail as expected
    }

    @Test
    public void shouldNotThrowExceptionIfScenariosFailedAndIgnoreFlagIsSet() throws Throwable {
        // Given
        PerformableTree performableTree = mock(PerformableTree.class);
        EmbedderControls embedderControls = new EmbedderControls().doIgnoreFailureInView(true);
        OutputStream out = new ByteArrayOutputStream();
        EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(new PrintStream(out));
        ViewGenerator viewGenerator = mock(ViewGenerator.class);

        Embedder embedder = embedderWith(performableTree, embedderControls, monitor);
        embedder.configuration().useViewGenerator(viewGenerator);
        File outputDirectory = new File("target/output");
        List<String> formats = asList("html");
        Properties viewResources = new Properties();
        when(viewGenerator.getReportsCount()).thenReturn(new ReportsCount(1, 0, 1, 2, 1, 0, 1, 1));
        embedder.generateReportsView(outputDirectory, formats, viewResources);

        // Then
        verify(viewGenerator).generateReportsView(outputDirectory, formats, viewResources);
        assertThatReportsViewGenerated(out);
    }

    @Test
    public void shouldHandleFailuresAccordingToStrategy() throws Throwable {
        // Given
        PerformableTree performableTree = mock(PerformableTree.class);
        EmbedderControls embedderControls = new EmbedderControls();
        OutputStream out = new ByteArrayOutputStream();
        EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(new PrintStream(out));
        ViewGenerator viewGenerator = mock(ViewGenerator.class);

        Embedder embedder = embedderWith(performableTree, embedderControls, monitor);
        EmbedderFailureStrategy failureStategy = mock(EmbedderFailureStrategy.class);
        embedder.useEmbedderFailureStrategy(failureStategy);
        embedder.configuration().useViewGenerator(viewGenerator);
        File outputDirectory = new File("target/output");
        List<String> formats = asList("html");
        Properties viewResources = new Properties();

        // When
        ReportsCount count = new ReportsCount(1, 0, 1, 2, 1, 1, 1, 1);
        when(viewGenerator.getReportsCount()).thenReturn(count);
        embedder.generateReportsView(outputDirectory, formats, viewResources);

        // Then
        verify(failureStategy).handleFailures(count);
    }

    @Test
    public void shouldAllowOverrideOfDefaultDependencies() throws Throwable {
        // Given
        PerformableTree performableTree = new PerformableTree();
        EmbedderControls embedderControls = new EmbedderControls();
        EmbedderMonitor monitor = new PrintStreamEmbedderMonitor();

        // When
        Embedder embedder = embedderWith(performableTree, embedderControls, monitor);
        assertThat(embedder.embedderControls(), is(sameInstance(embedderControls)));
        assertThat(embedder.performableTree(), is(sameInstance(performableTree)));
        assertThat(embedder.embedderMonitor(), is(sameInstance(monitor)));
        embedder.usePerformableTree(new PerformableTree());
        embedder.useEmbedderMonitor(new PrintStreamEmbedderMonitor());

        // Then
        assertThat(embedder.performableTree(), is(not(sameInstance(performableTree))));
        assertThat(embedder.embedderMonitor(), is(not(sameInstance(monitor))));
    }

    private Embedder embedderWith(PerformableTree performableTree, EmbedderControls embedderControls,
            EmbedderMonitor monitor) {
        Embedder embedder = new Embedder(new StoryMapper(), performableTree, monitor);
        embedder.useEmbedderControls(embedderControls);
        return embedder;
    }

    private Embedder embedderWith(StoryMapper mapper, PerformableTree performableTree,
            EmbedderControls embedderControls, EmbedderMonitor monitor) {
        Embedder embedder = new Embedder(mapper, performableTree, monitor);
        embedder.useEmbedderControls(embedderControls);
        return embedder;
    }

    @Test
    public void shouldFindAndReportMatchingSteps() {
        // Given
        Embedder embedder = new Embedder();
        embedder.useCandidateSteps(asList((CandidateSteps) new MySteps()));
        embedder.configuration().useStepFinder(new StepFinder());
        OutputStream out = new ByteArrayOutputStream();
        embedder.configuration().useStepdocReporter(new PrintStreamStepdocReporter(new PrintStream(out)));
        // When
        embedder.reportMatchingStepdocs("Given a given");
        // Then
        String expected = "Step 'Given a given' is matched by annotated patterns:\n" + "'Given a given'\n"
                + "org.jbehave.core.embedder.EmbedderBehaviour$MySteps.given()\n" + "from steps instances:\n"
                + "org.jbehave.core.embedder.EmbedderBehaviour$MySteps\n";
        assertThat(dos2unix(out.toString()), equalTo(expected));
    }

    @Test
    public void shouldReportNoMatchingStepdocsFoundWithStepProvided() {
        // Given
        Embedder embedder = new Embedder();
        embedder.useCandidateSteps(asList((CandidateSteps) new MySteps()));
        embedder.configuration().useStepFinder(new StepFinder());
        OutputStream out = new ByteArrayOutputStream();
        embedder.configuration().useStepdocReporter(new PrintStreamStepdocReporter(new PrintStream(out)));
        // When
        embedder.reportMatchingStepdocs("Given a non-defined step");
        // Then
        String expected = "Step 'Given a non-defined step' is not matched by any pattern\n" + "from steps instances:\n"
                + "org.jbehave.core.embedder.EmbedderBehaviour$MySteps\n";
        assertThat(dos2unix(out.toString()), equalTo(expected));
    }

    @Test
    public void shouldReportNoMatchingStepdocsFoundWhenNoStepsProvided() {
        // Given
        Embedder embedder = new Embedder();
        embedder.useCandidateSteps(asList(new CandidateSteps[] {}));
        embedder.configuration().useStepFinder(new StepFinder());
        OutputStream out = new ByteArrayOutputStream();
        embedder.configuration().useStepdocReporter(new PrintStreamStepdocReporter(new PrintStream(out)));
        // When
        embedder.reportMatchingStepdocs("Given a non-defined step");
        // Then
        String expected = "Step 'Given a non-defined step' is not matched by any pattern\n"
                + "as no steps instances are provided\n";
        assertThat(dos2unix(out.toString()), equalTo(expected));
    }

    @Test
    public void shouldReportAllStepdocs() {
        // Given
        Embedder embedder = new Embedder();
        embedder.useCandidateSteps(asList((CandidateSteps) new MySteps()));
        embedder.configuration().useStepFinder(new StepFinder());
        OutputStream out = new ByteArrayOutputStream();
        embedder.configuration().useStepdocReporter(new PrintStreamStepdocReporter(new PrintStream(out)));
        // When
        embedder.reportStepdocs();
        // Then
        String output = dos2unix(out.toString());
        assertThat(output, containsString("'Given a given'\n"
                + "org.jbehave.core.embedder.EmbedderBehaviour$MySteps.given()\n"));
        assertThat(output, containsString("'When a when'\n"
                + "org.jbehave.core.embedder.EmbedderBehaviour$MySteps.when()\n"));
        assertThat(output, containsString("'Then a then'\n"
                + "org.jbehave.core.embedder.EmbedderBehaviour$MySteps.then()\n"));
        assertThat(output,
                containsString("from steps instances:\norg.jbehave.core.embedder.EmbedderBehaviour$MySteps\n"));
    }

    @Test
    public void shouldAllowStringRepresentationOfEmbedder() throws Throwable {
        // Given
        Embedder embedder = new Embedder();
        assertThat(embedder.configuration(), instanceOf(MostUsefulConfiguration.class));
        
        // When
        String embedderAsString = embedder.toString();

        // Then
        assertThat(embedderAsString, containsString(MostUsefulConfiguration.class.getSimpleName()));
        assertThat(embedderAsString, containsString(PerformableTree.class.getSimpleName()));
        assertThat(embedderAsString, containsString(PrintStreamEmbedderMonitor.class.getSimpleName()));
    }

    @Test
    public void shouldGenerateCrossReferenceWhenAvailable() {

        Embedder embedder = new Embedder();
        embedder.useConfiguration(new MostUsefulConfiguration().useStoryReporterBuilder(new StoryReporterBuilder()
                .withCrossReference(new CrossReference())));

        // When
        embedder.generateCrossReference();

        // Then
        assertXrefExists(embedder.configuration().storyReporterBuilder(), "json");
        assertXrefExists(embedder.configuration().storyReporterBuilder(), "xml");

    }

    private void assertXrefExists(StoryReporterBuilder storyReporterBuilder, String ext) {
        assertThat(new File(storyReporterBuilder.outputDirectory(), "view/xref." + ext).exists(), is(true));
    }

    private String dos2unix(String string) {
        return string.replace("\r\n", "\n");
    }

    private static class MyStoryMaps extends JUnitStoryMaps {

        static boolean run = false;

        @Override
        public void run() throws Throwable {
            run = true;
        }

        @Override
        protected List<String> metaFilters() {
            return asList("+some property");
        }

        @Override
        protected List<String> storyPaths() {
            return asList("**/*.story");
        }

    }

    private Story mockStory(Meta meta) {
        Story story = mock(Story.class);
        when(story.getPath()).thenReturn("/a/path");
        when(story.getMeta()).thenReturn(meta);
        when(story.asMeta(Mockito.anyString())).thenReturn(meta);
        return story;
    }

    private class MyEmbeddable implements Embeddable {

        public void useEmbedder(Embedder embedder) {
        }

        public void run() throws Throwable {
        }
    }

    private class MyOtherEmbeddable implements Embeddable {

        public void useEmbedder(Embedder embedder) {
        }

        public void run() throws Throwable {
        }
    }

    private abstract class MyAbstractEmbeddable implements Embeddable {
    }

    private class MyStory extends JUnitStory {
    }

    private class MyFailingEmbeddable extends JUnitStory {

        @Override
        public void run() throws Throwable {
            throw new RuntimeException("Failed");
        }
    }

    @RunWith(AnnotatedEmbedderRunner.class)
    @Configure()
    @UsingEmbedder()
    public static class RunningWithAnnotatedEmbedderRunner extends InjectableEmbedder {

        static boolean hasRun;

        @Test
        public void run() {
            hasRun = true;
        }
    }

    @RunWith(AnnotatedEmbedderRunner.class)
    @Configure()
    @UsingEmbedder()
    public static class FailingWithAnnotatedEmbedderRunner extends InjectableEmbedder {

        @Test
        public void run() {
            throw new RuntimeException();
        }
    }

    @RunWith(AnnotatedEmbedderRunner.class)
    @Configure()
    @UsingEmbedder()
    public static class NotEmbeddableWithAnnotatedEmbedderRunner {

        static boolean hasRun;

        @Test
        public void run() {
            hasRun = true;
        }
    }

    public static class MySteps extends Steps {

        @Given("a given")
        public void given() {
        }

        @When("a when")
        public void when() {
        }

        @Then("a then")
        public void then() {
        }

    }
}
