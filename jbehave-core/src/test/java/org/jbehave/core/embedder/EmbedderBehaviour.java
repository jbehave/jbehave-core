package org.jbehave.core.embedder;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Comparator;
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
import org.jbehave.core.reporters.PrintStreamStepdocReporter;
import org.jbehave.core.reporters.ReportsCount;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.reporters.ViewGenerator;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.core.steps.StepCollector.Stage;
import org.jbehave.core.steps.StepFinder;
import org.jbehave.core.steps.Steps;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mockito;

class EmbedderBehaviour {

    @Test
    void shouldMapStoriesAsEmbeddables() {
        // Given
        PerformableTree performableTree = mock(PerformableTree.class);
        OutputStream out = new ByteArrayOutputStream();
        EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(new PrintStream(out));
        String myEmbeddableName = MyStoryMaps.class.getName();
        List<String> classNames = singletonList(myEmbeddableName);
        Embeddable myEmbeddable = new MyStoryMaps();
        List<Embeddable> embeddables = singletonList(myEmbeddable);
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
    void shouldMapStoriesAsPaths() {
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
        List<StoryMap> maps = singletonList(new StoryMap("filter", new HashSet<>(stories.values())));
        StoryMaps storyMaps = new StoryMaps(maps);
        when(mapper.getStoryMaps()).thenReturn(storyMaps);
        embedder.mapStoriesAsPaths(storyPaths);

        // Then
        for (String storyPath : storyPaths) {
            verify(mapper).map(eq(stories.get(storyPath)), any(MetaFilter.class));
            assertThat(out.toString(), containsString("Mapping story " + storyPath));
        }
        assertThatMapsViewGenerated(out);
    }

    private void assertThatMapsViewGenerated(OutputStream out) {
        assertThat(out.toString(), containsString("Generating maps view"));
    }

    @Test
    void shouldRunStoriesAsEmbeddables() {
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
        InjectableStepsFactory stepsFactory = mock(InjectableStepsFactory.class);
        Embedder embedder = embedderWith(performableTree, embedderControls, monitor);
        embedder.useClassLoader(classLoader);
        embedder.useConfiguration(configuration);
        embedder.useStepsFactory(stepsFactory);
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
    void shouldNotRunStoriesAsEmbeddablesIfAbstract() {
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
    void shouldNotRunStoriesAsEmbeddablesIfSkipFlagIsSet() {
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

    @Test
    void shouldThrowExceptionUponFailingStoriesAsEmbeddablesIfIgnoreFailureInStoriesFlagIsNotSet() {
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

        // Then fail as expected
        assertThrows(RunningEmbeddablesFailed.class, () -> embedder.runAsEmbeddables(classNames));
    }

    @Test
    void shouldNotThrowExceptionUponFailingStoriesAsEmbeddablesIfIgnoreFailureFlagsAreSet() {
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
    void shouldRunStoriesAsEmbeddablesInBatchIfBatchFlagIsSet() {
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

    @Test
    void shouldThrowExceptionUponFailingStoriesAsEmbeddablesInBatchIfIgnoreFailureInStoriesFlagIsNotSet() {
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

        // Then fail as expected
        assertThrows(RunningEmbeddablesFailed.class, () -> embedder.runAsEmbeddables(classNames));
    }

    @Test
    void shouldRunFailingStoriesAsEmbeddablesInBatchIfBatchFlagIsSet() {
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
    void shouldNotGenerateViewWhenRunningStoriesAsEmbeddablesIfGenerateViewAfterStoriesFlagIsNotSet() {
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
    void shouldRunStoriesAsPaths() {
        // Given
        PerformableTree performableTree = mock(PerformableTree.class);
        EmbedderControls embedderControls = new EmbedderControls();
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
        configuration.useStoryExecutionComparator(Comparator.comparing(Story::getPath, Comparator.naturalOrder()));
        StoryPathResolver resolver = configuration.storyPathResolver();

        List<String> storyPaths = new ArrayList<>();
        Map<String, Story> stories = new HashMap<>();
        for (Class<? extends Embeddable> embeddable : embeddables) {
            String storyPath = resolver.resolve(embeddable);
            storyPaths.add(storyPath);
            Story story = mockStory(storyPath, Meta.EMPTY);
            stories.put(storyPath, story);
            when(performableTree.storyOfPath(configuration, storyPath)).thenReturn(story);
            assertThat(configuration.storyReporter(storyPath), sameInstance(storyReporter));
        }
        RunContext runContext = mockRunContext(performableTree, monitor, configuration, stepsFactory, filter);

        InOrder performOrder = inOrder(performableTree);

        // When
        embedder.runStoriesAsPaths(storyPaths);

        // Then
        performOrder.verify(performableTree).performBeforeOrAfterStories(isA(RunContext.class), eq(Stage.BEFORE));
        for (String storyPath : asList(resolver.resolve(MyOtherEmbeddable.class), resolver.resolve(MyStory.class))) {
            performOrder.verify(performableTree).perform(isA(RunContext.class), eq(stories.get(storyPath)));
            assertThat(out.toString(), containsString("Running story " + storyPath));
        }
        performOrder.verify(performableTree).performBeforeOrAfterStories(isA(RunContext.class), eq(Stage.AFTER));
        performOrder.verifyNoMoreInteractions();
        assertThatReportsViewGenerated(out);
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldRunStoriesApplyingFilter() {
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
        when(meta.inheritFrom(meta)).thenReturn(meta);
        for (Class<? extends Embeddable> embeddable : embeddables) {
            String storyPath = resolver.resolve(embeddable);
            storyPaths.add(storyPath);
            Story story = mockStory(storyPath, meta);
            stories.put(storyPath, story);
            when(performableTree.storyOfPath(configuration, storyPath)).thenReturn(story);
            assertThat(configuration.storyReporter(storyPath), sameInstance(storyReporter));
        }

        // When
        MetaFilter filter = mock(MetaFilter.class);
        when(filter.excluded(meta)).thenReturn(true);
        RunContext runContext = mockRunContext(performableTree, monitor, configuration, stepsFactory, filter);
        embedder.runStoriesAsPaths(storyPaths);

        // Then
        for (String storyPath : storyPaths) {
            verify(performableTree, never()).perform(runContext, stories.get(storyPath));
        }
        assertThatReportsViewGenerated(out);
        assertThat(embedder.hasExecutorService(), is(false));
        assertThat(embedder.storyManager, nullValue());
    }

    @Test
    void shouldProcessSystemProperties() {

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
    void shouldNotRunStoriesIfSkipFlagIsSet() {
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
            Story story = mockStory(storyPath, Meta.EMPTY);
            stories.put(storyPath, story);
            when(performableTree.storyOfPath(configuration, storyPath)).thenReturn(story);
        }
        RunContext runContext = mockRunContext(performableTree, monitor, configuration, stepsFactory, filter);

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
    void shouldNotThrowExceptionUponFailingStoriesAsPathsIfIgnoreFailureInStoriesFlagIsSet() throws Throwable {
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
            Story story = mockStory(storyPath, Meta.EMPTY);
            stories.put(storyPath, story);
            when(performableTree.storyOfPath(configuration, storyPath)).thenReturn(story);
        }
        RunContext runContext = mockRunContext(performableTree, monitor, configuration, stepsFactory, filter);

        for (String storyPath : storyPaths) {
            doThrow(new RuntimeException(storyPath + " failed")).when(performableTree).perform(runContext,
                    stories.get(storyPath));
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
    void shouldRunStoriesAsPathsInBatchIfBatchFlagIsSet() {
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
            Story story = mockStory(storyPath, Meta.EMPTY);
            stories.put(storyPath, story);
            when(performableTree.storyOfPath(configuration, storyPath)).thenReturn(story);
        }
        RunContext runContext = mockRunContext(performableTree, monitor, configuration, stepsFactory, filter);

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
    void shouldRunFailingStoriesAsPathsInBatchIfBatchFlagIsSet() {
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
            Story story = mockStory(storyPath, Meta.EMPTY);
            stories.put(storyPath, story);
            when(performableTree.storyOfPath(configuration, storyPath)).thenReturn(story);
        }
        RunContext runContext = mockRunContext(performableTree, monitor, configuration, stepsFactory, filter);

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
    void shouldNotGenerateViewWhenRunningStoriesAsPathsIfGenerateViewAfterStoriesFlagIsNotSet() {
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
            Story story = mockStory(storyPath, Meta.EMPTY);
            stories.put(storyPath, story);
            when(performableTree.storyOfPath(configuration, storyPath)).thenReturn(story);
        }
        RunContext runContext = mockRunContext(performableTree, monitor, configuration, stepsFactory, filter);

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
    void shouldRunStoriesWithAnnotatedEmbedderRunnerIfEmbeddable() {
        // Given
        Embedder embedder = new Embedder();
        embedder.useClassLoader(new EmbedderClassLoader(this.getClass().getClassLoader()));
        String runWithEmbedderRunner = RunningWithAnnotatedEmbedderRunner.class.getName();
        // When
        embedder.runStoriesWithAnnotatedEmbedderRunner(singletonList(runWithEmbedderRunner));
        // Then
        assertThat(RunningWithAnnotatedEmbedderRunner.hasRun, is(true));
    }

    @Test
    void shouldNotRunStoriesWithAnnotatedEmbedderRunnerIfNotEmbeddable() {
        // Given
        Embedder embedder = new Embedder();
        embedder.useClassLoader(new EmbedderClassLoader(this.getClass().getClassLoader()));
        String runWithEmbedderRunner = NotEmbeddableWithAnnotatedEmbedderRunner.class.getName();
        // When
        embedder.runStoriesWithAnnotatedEmbedderRunner(singletonList(runWithEmbedderRunner));
        // Then
        assertThat(NotEmbeddableWithAnnotatedEmbedderRunner.hasRun, is(false));
    }

    @Test
    void shouldRethowFailuresWhenRunningWithAnnotatedEmbedderRunner() {
        // Given
        Embedder embedder = new Embedder();
        embedder.useClassLoader(new EmbedderClassLoader(this.getClass().getClassLoader()));
        List<String> classNames = singletonList(FailingWithAnnotatedEmbedderRunner.class.getName());
        // When
        assertThrows(RuntimeException.class, () -> embedder.runStoriesWithAnnotatedEmbedderRunner(classNames));
        // Then fail as expected
    }

    @Test
    void shouldFailWhenRunningInexistingStoriesWithAnnotatedEmbedderRunner() {
        // Given
        Embedder embedder = new Embedder();
        embedder.useClassLoader(new EmbedderClassLoader(this.getClass().getClassLoader()));
        List<String> classNames = singletonList("UnexistingRunner");
        // When
        assertThrows(ClassLoadingFailed.class, () -> embedder.runStoriesWithAnnotatedEmbedderRunner(classNames));
        // Then fail as expected
    }

    @Test
    void shouldGenerateReportsViewFromExistingReports() {
        // Given
        PerformableTree performableTree = mock(PerformableTree.class);
        EmbedderControls embedderControls = new EmbedderControls().doGenerateViewAfterStories(false);
        OutputStream out = new ByteArrayOutputStream();
        EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(new PrintStream(out));
        ViewGenerator viewGenerator = mock(ViewGenerator.class);

        Embedder embedder = embedderWith(performableTree, embedderControls, monitor);
        embedder.configuration().useViewGenerator(viewGenerator);

        File outputDirectory = new File("target/output");
        List<String> formats = singletonList("html");
        Properties viewResources = new Properties();
        when(viewGenerator.getReportsCount()).thenReturn(new ReportsCount(2, 0, 1, 2, 0, 0, 1, 0));
        embedder.generateReportsView(outputDirectory, formats, viewResources);

        // Then
        verify(viewGenerator).generateReportsView(outputDirectory, formats, viewResources);
        assertThatReportsViewGenerated(out);
    }

    @Test
    void shouldFailWhenGeneratingReportsViewWithFailedSteps() {
        // Given
        PerformableTree performableTree = mock(PerformableTree.class);
        EmbedderControls embedderControls = new EmbedderControls().doGenerateViewAfterStories(false);
        OutputStream out = new ByteArrayOutputStream();
        EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(new PrintStream(out));
        ViewGenerator viewGenerator = mock(ViewGenerator.class);

        Embedder embedder = embedderWith(performableTree, embedderControls, monitor);
        embedder.configuration().useViewGenerator(viewGenerator);

        File outputDirectory = new File("target/output");
        List<String> formats = singletonList("html");
        Properties viewResources = new Properties();
        when(viewGenerator.getReportsCount()).thenReturn(new ReportsCount(2, 0, 0, 2, 1, 0, 0, 1));

        assertThrows(RunningStoriesFailed.class,
                () -> embedder.generateReportsView(outputDirectory, formats, viewResources));

        // Then
        verify(viewGenerator).generateReportsView(outputDirectory, formats, viewResources);
        assertThatReportsViewGenerated(out);
    }

    @Test
    void shouldFailWhenGeneratingReportsViewWithPendingSteps() {
        // Given
        PerformableTree performableTree = mock(PerformableTree.class);
        EmbedderControls embedderControls = new EmbedderControls().doGenerateViewAfterStories(false);
        OutputStream out = new ByteArrayOutputStream();
        EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(new PrintStream(out));
        ViewGenerator viewGenerator = mock(ViewGenerator.class);

        Embedder embedder = embedderWith(performableTree, embedderControls, monitor);
        embedder.configuration().useViewGenerator(viewGenerator).usePendingStepStrategy(new FailingUponPendingStep());

        File outputDirectory = new File("target/output");
        List<String> formats = singletonList("html");
        Properties viewResources = new Properties();
        when(viewGenerator.getReportsCount()).thenReturn(new ReportsCount(2, 0, 1, 2, 0, 0, 1, 0));
        assertThrows(RunningStoriesFailed.class,
                () -> embedder.generateReportsView(outputDirectory, formats, viewResources));

        // Then
        verify(viewGenerator).generateReportsView(outputDirectory, formats, viewResources);
        assertThatReportsViewGenerated(out);
    }

    @Test
    void shouldNotGenerateViewIfSkipFlagIsSet() {
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
        List<String> formats = singletonList("html");
        Properties viewResources = new Properties();
        embedder.generateReportsView(outputDirectory, formats, viewResources);

        // Then
        verify(viewGenerator, never()).generateReportsView(outputDirectory, formats, viewResources);
        assertThat(out.toString(), not(containsString("Generating stories view")));
        assertThat(out.toString(), not(containsString("Stories view generated")));
    }

    @Test
    void shouldThrowExceptionIfViewGenerationFails() {
        // Given
        PerformableTree performableTree = mock(PerformableTree.class);
        EmbedderControls embedderControls = new EmbedderControls();
        OutputStream out = new ByteArrayOutputStream();
        EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(new PrintStream(out));
        ViewGenerator viewGenerator = mock(ViewGenerator.class);

        Embedder embedder = embedderWith(performableTree, embedderControls, monitor);
        embedder.configuration().useViewGenerator(viewGenerator);
        File outputDirectory = new File("target/output");
        List<String> formats
                = singletonList("html");
        Properties viewResources = new Properties();
        doThrow(new RuntimeException()).when(viewGenerator)
                .generateReportsView(outputDirectory, formats, viewResources);
        assertThrows(ViewGenerationFailed.class,
                () -> embedder.generateReportsView(outputDirectory, formats, viewResources));
        // Then fail as expected
    }

    @Test
    void shouldThrowExceptionIfScenariosFailedAndIgnoreFlagIsNotSet() {
        // Given
        PerformableTree performableTree = mock(PerformableTree.class);
        EmbedderControls embedderControls = new EmbedderControls();
        OutputStream out = new ByteArrayOutputStream();
        EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(new PrintStream(out));
        ViewGenerator viewGenerator = mock(ViewGenerator.class);

        Embedder embedder = embedderWith(performableTree, embedderControls, monitor);
        embedder.configuration().useViewGenerator(viewGenerator);
        File outputDirectory = new File("target/output");
        List<String> formats = singletonList("html");
        Properties viewResources = new Properties();
        when(viewGenerator.getReportsCount()).thenReturn(new ReportsCount(1, 0, 1, 2, 1, 1, 1, 1));
        assertThrows(RunningStoriesFailed.class,
                () -> embedder.generateReportsView(outputDirectory, formats, viewResources));
        // Then fail as expected
    }

    @Test
    void shouldThrowExceptionIfNoScenariosRunForStoriesAndIgnoreFlagIsNotSet() {
        // Given
        PerformableTree performableTree = mock(PerformableTree.class);
        EmbedderControls embedderControls = new EmbedderControls();
        OutputStream out = new ByteArrayOutputStream();
        EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(new PrintStream(out));
        ViewGenerator viewGenerator = mock(ViewGenerator.class);

        Embedder embedder = embedderWith(performableTree, embedderControls, monitor);
        embedder.configuration().useViewGenerator(viewGenerator);
        File outputDirectory = new File("target/output");
        List<String> formats = singletonList("html");
        Properties viewResources = new Properties();
        when(viewGenerator.getReportsCount()).thenReturn(new ReportsCount(1, 0, 0, 0, 0, 0, 0, 1));
        assertThrows(RunningStoriesFailed.class,
                () -> embedder.generateReportsView(outputDirectory, formats, viewResources));
        // Then fail as expected
    }

    @Test
    void shouldNotThrowExceptionIfScenariosFailedAndIgnoreFlagIsSet() {
        // Given
        PerformableTree performableTree = mock(PerformableTree.class);
        EmbedderControls embedderControls = new EmbedderControls().doIgnoreFailureInView(true);
        OutputStream out = new ByteArrayOutputStream();
        EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(new PrintStream(out));
        ViewGenerator viewGenerator = mock(ViewGenerator.class);

        Embedder embedder = embedderWith(performableTree, embedderControls, monitor);
        embedder.configuration().useViewGenerator(viewGenerator);
        File outputDirectory = new File("target/output");
        List<String> formats = singletonList("html");
        Properties viewResources = new Properties();
        when(viewGenerator.getReportsCount()).thenReturn(new ReportsCount(1, 0, 1, 2, 1, 0, 1, 1));
        embedder.generateReportsView(outputDirectory, formats, viewResources);

        // Then
        verify(viewGenerator).generateReportsView(outputDirectory, formats, viewResources);
        assertThatReportsViewGenerated(out);
    }

    @Test
    void shouldHandleFailuresAccordingToStrategy() {
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
        List<String> formats = singletonList("html");
        Properties viewResources = new Properties();

        // When
        ReportsCount count = new ReportsCount(1, 0, 1, 2, 1, 1, 1, 1);
        when(viewGenerator.getReportsCount()).thenReturn(count);
        embedder.generateReportsView(outputDirectory, formats, viewResources);

        // Then
        verify(failureStategy).handleFailures(count);
    }

    @Test
    void shouldAllowOverrideOfDefaultDependencies() {
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
    void shouldFindAndReportMatchingSteps() {
        // Given
        Embedder embedder = new Embedder();
        embedder.useStepsFactory(new InstanceStepsFactory(embedder.configuration(), new MySteps()));
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
    void shouldReportNoMatchingStepdocsFoundWithStepProvided() {
        // Given
        Embedder embedder = new Embedder();
        embedder.useStepsFactory(new InstanceStepsFactory(embedder.configuration(), new MySteps()));
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
    void shouldReportNoMatchingStepdocsFoundWhenNoStepsProvided() {
        // Given
        Embedder embedder = new Embedder();
        embedder.useStepsFactory(new InstanceStepsFactory(embedder.configuration(), new CandidateSteps[] {}));
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
    void shouldReportAllStepdocs() {
        // Given
        Embedder embedder = new Embedder();
        embedder.useStepsFactory(new InstanceStepsFactory(embedder.configuration(), new MySteps()));
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
    void shouldAllowStringRepresentationOfEmbedder() {
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

    private RunContext mockRunContext(PerformableTree performableTree, EmbedderMonitor monitor,
            Configuration configuration, InjectableStepsFactory stepsFactory, MetaFilter filter) {
        List<CandidateSteps> candidateSteps = stepsFactory.createCandidateSteps();
        AllStepCandidates allStepCandidates = new AllStepCandidates(configuration.stepConditionMatcher(),
                candidateSteps);
        RunContext runContext = new RunContext(configuration, allStepCandidates, monitor, filter, new BatchFailures());
        when(performableTree
                .newRunContext(eq(configuration), isA(AllStepCandidates.class), eq(monitor), isA(MetaFilter.class),
                        isA(BatchFailures.class))).thenReturn(runContext);
        return runContext;
    }

    private String dos2unix(String string) {
        return string.replace("\r\n", "\n");
    }

    private static class MyStoryMaps extends JUnitStoryMaps {

        static boolean run = false;

        @Override
        public void run() {
            run = true;
        }

        @Override
        public List<String> metaFilters() {
            return singletonList("+some property");
        }

        @Override
        public List<String> storyPaths() {
            return singletonList("**/*.story");
        }

    }

    private Story mockStory(String path, Meta meta) {
        Story story = mock(Story.class);
        when(story.getPath()).thenReturn(path);
        when(story.getMeta()).thenReturn(meta);
        when(story.asMeta(Mockito.anyString())).thenReturn(meta);
        return story;
    }

    private static class MyEmbeddable implements Embeddable {

        @Override
        public void useEmbedder(Embedder embedder) {
        }

        @Override
        public void run() {
        }
    }

    private static class MyOtherEmbeddable implements Embeddable {

        @Override
        public void useEmbedder(Embedder embedder) {
        }

        @Override
        public void run() {
        }
    }

    private abstract static class MyAbstractEmbeddable implements Embeddable {
    }

    private static class MyStory extends JUnitStory {
    }

    private static class MyFailingEmbeddable extends JUnitStory {

        @Override
        public void run() {
            throw new RuntimeException("Failed");
        }
    }

    @RunWith(AnnotatedEmbedderRunner.class)
    @Configure()
    @UsingEmbedder()
    public static class RunningWithAnnotatedEmbedderRunner extends InjectableEmbedder {

        static boolean hasRun;

        @Override
        @org.junit.Test
        public void run() {
            hasRun = true;
        }
    }

    @RunWith(AnnotatedEmbedderRunner.class)
    @Configure()
    @UsingEmbedder()
    public static class FailingWithAnnotatedEmbedderRunner extends InjectableEmbedder {

        @Override
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

        @org.junit.Test
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
