package org.jbehave.core.embedder;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
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

import org.jbehave.core.Embeddable;
import org.jbehave.core.InjectableEmbedder;
import org.jbehave.core.annotations.Configure;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.UsingEmbedder;
import org.jbehave.core.annotations.When;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.embedder.Embedder.AnnotatedEmbedderRunnerInstantiationFailed;
import org.jbehave.core.embedder.Embedder.RunningEmbeddablesFailed;
import org.jbehave.core.embedder.Embedder.RunningStoriesFailed;
import org.jbehave.core.embedder.Embedder.ViewGenerationFailed;
import org.jbehave.core.failures.BatchFailures;
import org.jbehave.core.io.StoryPathResolver;
import org.jbehave.core.io.UnderscoredCamelCaseResolver;
import org.jbehave.core.junit.AnnotatedEmbedderRunner;
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
import org.jbehave.core.steps.StepFinder;
import org.jbehave.core.steps.Steps;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;

public class EmbedderBehaviour {

    @Test
    public void shouldMapStoriesAsEmbeddables() throws Throwable {
        // Given
        StoryRunner runner = mock(StoryRunner.class);
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
        Embedder embedder = embedderWith(runner, new EmbedderControls(), monitor);
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
        StoryRunner runner = mock(StoryRunner.class);
        EmbedderControls embedderControls = new EmbedderControls();
        OutputStream out = new ByteArrayOutputStream();
        EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(new PrintStream(out));

        Embedder embedder = embedderWith(mapper, runner, embedderControls, monitor);
        Configuration configuration = embedder.configuration();
        List<? extends Class<? extends Embeddable>> embeddables = asList(MyStory.class, MyOtherStory.class);
        StoryPathResolver resolver = configuration.storyPathResolver();
        List<String> storyPaths = new ArrayList<String>();
        Map<String, Story> stories = new HashMap<String, Story>();
        for (Class<? extends Embeddable> embeddable : embeddables) {
            String storyPath = resolver.resolve(embeddable);
            storyPaths.add(storyPath);
            Story story = new Story(storyPath);
            stories.put(storyPath, story);
            when(runner.storyOfPath(configuration, storyPath)).thenReturn(story);
        }

        // When
        List<StoryMap> maps = asList(new StoryMap("filter", new HashSet<Story>(stories.values())));
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
        StoryRunner runner = mock(StoryRunner.class);
        EmbedderControls embedderControls = new EmbedderControls();
        OutputStream out = new ByteArrayOutputStream();
        EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(new PrintStream(out));
        String myStoryName = MyStory.class.getName();
        String myOtherStoryName = MyOtherStory.class.getName();
        List<String> classNames = asList(myStoryName, myOtherStoryName);
        Embeddable myStory = new MyStory();
        Embeddable myOtherStory = new MyOtherStory();
        List<Embeddable> embeddables = asList(myStory, myOtherStory);
        EmbedderClassLoader classLoader = mock(EmbedderClassLoader.class);
        when(classLoader.newInstance(Embeddable.class, myStoryName)).thenReturn(myStory);
        when(classLoader.newInstance(Embeddable.class, myOtherStoryName)).thenReturn(myOtherStory);

        // When
        Configuration configuration = new MostUsefulConfiguration();
        CandidateSteps steps = mock(CandidateSteps.class);
        Embedder embedder = embedderWith(runner, embedderControls, monitor);
        embedder.useClassLoader(classLoader);
        embedder.useConfiguration(configuration);
        embedder.useCandidateSteps(asList(steps));
        embedder.runAsEmbeddables(classNames);

        // Then
        for (Embeddable embeddable : embeddables) {
            assertThat(out.toString(), containsString("Running embeddable " + embeddable.getClass().getName()));
        }
        assertThatReportsViewGenerated(out);
    }

    private void assertThatReportsViewGenerated(OutputStream out) {
        assertThat(out.toString(), containsString("Generating reports view"));
        assertThat(out.toString(), containsString("Reports view generated"));
    }

    @Test
    public void shouldNotRunStoriesAsEmbeddablesIfAbstract() throws Throwable {
        // Given
        StoryRunner runner = mock(StoryRunner.class);
        EmbedderControls embedderControls = new EmbedderControls();
        OutputStream out = new ByteArrayOutputStream();
        EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(new PrintStream(out));
        String myStoryName = MyAbstractStory.class.getName();
        String myOtherStoryName = MyOtherStory.class.getName();
        List<String> classNames = asList(myStoryName, myOtherStoryName);
        Embeddable myStory = new MyStory();
        Embeddable myOtherStory = new MyOtherStory();
        EmbedderClassLoader classLoader = mock(EmbedderClassLoader.class);
        when(classLoader.isAbstract(myStoryName)).thenReturn(true);
        when(classLoader.newInstance(Embeddable.class, myStoryName)).thenReturn(myStory);
        when(classLoader.isAbstract(myOtherStoryName)).thenReturn(false);
        when(classLoader.newInstance(Embeddable.class, myOtherStoryName)).thenReturn(myOtherStory);

        // When
        Embedder embedder = embedderWith(runner, embedderControls, monitor);
        embedder.useClassLoader(classLoader);
        embedder.configuration().useStoryPathResolver(new UnderscoredCamelCaseResolver());
        embedder.runAsEmbeddables(classNames);

        // Then
        assertThat(out.toString(), not(containsString("Running embeddable " + myStoryName)));
        assertThat(out.toString(), containsString("Running embeddable " + myOtherStoryName));
        assertThat(out.toString(), containsString("Generating reports view"));
    }

    @Test
    public void shouldNotRunStoriesAsEmbeddablesIfSkipFlagIsSet() throws Throwable {
        // Given
        StoryRunner runner = mock(StoryRunner.class);
        EmbedderControls embedderControls = new EmbedderControls().doSkip(true);
        OutputStream out = new ByteArrayOutputStream();
        EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(new PrintStream(out));
        String myStoryName = MyStory.class.getName();
        String myOtherStoryName = MyOtherStory.class.getName();
        List<String> classNames = asList(myStoryName, myOtherStoryName);
        Embeddable myStory = new MyStory();
        Embeddable myOtherStory = new MyOtherStory();
        List<Embeddable> embeddables = asList(myStory, myOtherStory);
        EmbedderClassLoader classLoader = mock(EmbedderClassLoader.class);
        when(classLoader.newInstance(Embeddable.class, myStoryName)).thenReturn(myStory);
        when(classLoader.newInstance(Embeddable.class, myOtherStoryName)).thenReturn(myOtherStory);

        // When
        Embedder embedder = embedderWith(runner, embedderControls, monitor);
        embedder.useClassLoader(classLoader);
        embedder.configuration().useStoryPathResolver(new UnderscoredCamelCaseResolver());
        embedder.runAsEmbeddables(classNames);

        // Then
        for (Embeddable embeddable : embeddables) {
            assertThat(out.toString(), not(containsString("Running embeddable " + embeddable.getClass().getName())));
        }
        assertThat(out.toString(), not(containsString("Generating stories view")));
    }

    @Test(expected = RunningEmbeddablesFailed.class)
    public void shouldThrowExceptionUponFailingStoriesAsEmbeddablesIfIgnoreFailureInStoriesFlagIsNotSet()
            throws Throwable {
        // Given
        StoryRunner runner = mock(StoryRunner.class);
        EmbedderControls embedderControls = new EmbedderControls();
        OutputStream out = new ByteArrayOutputStream();
        EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(new PrintStream(out));
        String myStoryName = MyStory.class.getName();
        String myOtherStoryName = MyOtherStory.class.getName();
        List<String> classNames = asList(myStoryName, myOtherStoryName);
        Embeddable myStory = new MyFailingStory();
        Embeddable myOtherStory = new MyOtherStory();
        EmbedderClassLoader classLoader = mock(EmbedderClassLoader.class);
        when(classLoader.newInstance(Embeddable.class, myStoryName)).thenReturn(myStory);
        when(classLoader.newInstance(Embeddable.class, myOtherStoryName)).thenReturn(myOtherStory);

        // When
        Embedder embedder = embedderWith(runner, embedderControls, monitor);
        embedder.useClassLoader(classLoader);
        embedder.runAsEmbeddables(classNames);

        // Then fail as expected
    }

    @Test
    public void shouldNotThrowExceptionUponFailingStoriesAsEmbeddablesIfIgnoreFailureInStoriesFlagIsSet()
            throws Throwable {
        // Given
        StoryRunner runner = mock(StoryRunner.class);
        EmbedderControls embedderControls = new EmbedderControls().doIgnoreFailureInStories(true);
        OutputStream out = new ByteArrayOutputStream();
        EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(new PrintStream(out));
        String myStoryName = MyFailingStory.class.getName();
        String myOtherStoryName = MyOtherStory.class.getName();
        List<String> classNames = asList(myStoryName, myOtherStoryName);
        Embeddable myStory = new MyFailingStory();
        Embeddable myOtherStory = new MyOtherStory();
        EmbedderClassLoader classLoader = mock(EmbedderClassLoader.class);
        when(classLoader.newInstance(Embeddable.class, myStoryName)).thenReturn(myStory);
        when(classLoader.newInstance(Embeddable.class, myOtherStoryName)).thenReturn(myOtherStory);

        // When
        Embedder embedder = embedderWith(runner, embedderControls, monitor);
        embedder.useClassLoader(classLoader);
        embedder.runAsEmbeddables(classNames);

        // Then
        assertThat(out.toString(), containsString("Running embeddable " + myStoryName));
        assertThat(out.toString(), containsString("Failed to run embeddable " + myStoryName));
        assertThat(out.toString(), containsString("Running embeddable " + myOtherStoryName));
        assertThat(out.toString(), not(containsString("Failed to run embeddable " + myOtherStoryName)));

    }

    @Test
    public void shouldRunStoriesAsEmbeddablesInBatchIfBatchFlagIsSet() throws Throwable {
        // Given
        StoryRunner runner = mock(StoryRunner.class);
        EmbedderControls embedderControls = new EmbedderControls().doBatch(true);
        OutputStream out = new ByteArrayOutputStream();
        EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(new PrintStream(out));
        String myStoryName = MyStory.class.getName();
        String myOtherStoryName = MyOtherStory.class.getName();
        List<String> classNames = asList(myStoryName, myOtherStoryName);
        Embeddable myStory = new MyStory();
        Embeddable myOtherStory = new MyOtherStory();
        List<Embeddable> embeddables = asList(myStory, myOtherStory);
        EmbedderClassLoader classLoader = mock(EmbedderClassLoader.class);
        when(classLoader.newInstance(Embeddable.class, myStoryName)).thenReturn(myStory);
        when(classLoader.newInstance(Embeddable.class, myOtherStoryName)).thenReturn(myOtherStory);

        // When
        Embedder embedder = embedderWith(runner, embedderControls, monitor);
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
        StoryRunner runner = mock(StoryRunner.class);
        EmbedderControls embedderControls = new EmbedderControls().doBatch(true);
        OutputStream out = new ByteArrayOutputStream();
        EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(new PrintStream(out));
        String myStoryName = MyStory.class.getName();
        String myOtherStoryName = MyOtherStory.class.getName();
        List<String> classNames = asList(myStoryName, myOtherStoryName);
        Embeddable myStory = new MyFailingStory();
        Embeddable myOtherStory = new MyOtherStory();
        EmbedderClassLoader classLoader = mock(EmbedderClassLoader.class);
        when(classLoader.newInstance(Embeddable.class, myStoryName)).thenReturn(myStory);
        when(classLoader.newInstance(Embeddable.class, myOtherStoryName)).thenReturn(myOtherStory);

        // When
        Embedder embedder = embedderWith(runner, embedderControls, monitor);
        embedder.useClassLoader(classLoader);
        embedder.runAsEmbeddables(classNames);

        // Then fail as expected

    }

    @Test
    public void shouldRunFailingStoriesAsEmbeddablesInBatchIfBatchFlagIsSet() throws Throwable {
        // Given
        StoryRunner runner = mock(StoryRunner.class);
        EmbedderControls embedderControls = new EmbedderControls().doBatch(true).doIgnoreFailureInStories(true);
        OutputStream out = new ByteArrayOutputStream();
        EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(new PrintStream(out));
        String myStoryName = MyFailingStory.class.getName();
        String myOtherStoryName = MyOtherStory.class.getName();
        List<String> classNames = asList(myStoryName, myOtherStoryName);
        Embeddable myStory = new MyFailingStory();
        Embeddable myOtherStory = new MyOtherStory();
        List<Embeddable> embeddables = asList(myStory, myOtherStory);
        EmbedderClassLoader classLoader = mock(EmbedderClassLoader.class);
        when(classLoader.newInstance(Embeddable.class, myStoryName)).thenReturn(myStory);
        when(classLoader.newInstance(Embeddable.class, myOtherStoryName)).thenReturn(myOtherStory);

        // When
        Embedder embedder = embedderWith(runner, embedderControls, monitor);
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
        StoryRunner runner = mock(StoryRunner.class);
        EmbedderControls embedderControls = new EmbedderControls().doGenerateViewAfterStories(false);
        OutputStream out = new ByteArrayOutputStream();
        EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(new PrintStream(out));
        String myStoryName = MyStory.class.getName();
        String myOtherStoryName = MyOtherStory.class.getName();
        List<String> classNames = asList(myStoryName, myOtherStoryName);
        Embeddable myStory = new MyStory();
        Embeddable myOtherStory = new MyOtherStory();
        List<Embeddable> embeddables = asList(myStory, myOtherStory);
        EmbedderClassLoader classLoader = mock(EmbedderClassLoader.class);
        when(classLoader.newInstance(Embeddable.class, myStoryName)).thenReturn(myStory);
        when(classLoader.newInstance(Embeddable.class, myOtherStoryName)).thenReturn(myOtherStory);

        // When
        Embedder embedder = embedderWith(runner, embedderControls, monitor);
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
        StoryRunner runner = mock(StoryRunner.class);
        EmbedderControls embedderControls = new EmbedderControls();
        OutputStream out = new ByteArrayOutputStream();
        EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(new PrintStream(out));
        List<? extends Class<? extends Embeddable>> embeddables = asList(MyStory.class, MyOtherStory.class);

        Embedder embedder = embedderWith(runner, embedderControls, monitor);
        final StoryReporter storyReporter = mock(StoryReporter.class);
        MostUsefulConfiguration configuration = new MostUsefulConfiguration() {
            @Override
            public StoryReporter storyReporter(String storyPath) {
                return storyReporter;
            }
        };
        embedder.useConfiguration(configuration);
        List<CandidateSteps> candidateSteps = embedder.candidateSteps();
        StoryPathResolver resolver = configuration.storyPathResolver();
        List<String> storyPaths = new ArrayList<String>();
        Map<String, Story> stories = new HashMap<String, Story>();
        for (Class<? extends Embeddable> embeddable : embeddables) {
            String storyPath = resolver.resolve(embeddable);
            storyPaths.add(storyPath);
            Story story = mock(Story.class);
            stories.put(storyPath, story);
            when(runner.storyOfPath(configuration, storyPath)).thenReturn(story);
            assertThat(configuration.storyReporter(storyPath), sameInstance(storyReporter));
        }

        // When
        embedder.runStoriesAsPaths(storyPaths);

        // Then
        for (String storyPath : storyPaths) {
            verify(runner).run(Matchers.eq(configuration), Matchers.eq(candidateSteps),
                    Matchers.eq(stories.get(storyPath)), Matchers.isA(MetaFilter.class));
            assertThat(out.toString(), containsString("Running story " + storyPath));
        }
        assertThatReportsViewGenerated(out);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldRunStoriesApplyingFilter() throws Throwable {
        // Given
        StoryRunner runner = mock(StoryRunner.class);
        EmbedderControls embedderControls = new EmbedderControls();
        OutputStream out = new ByteArrayOutputStream();
        EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(new PrintStream(out));
        List<? extends Class<? extends Embeddable>> embeddables = asList(MyStory.class, MyOtherStory.class);

        Embedder embedder = embedderWith(runner, embedderControls, monitor);
        final StoryReporter storyReporter = mock(StoryReporter.class);

        Configuration configuration = new MostUsefulConfiguration() {
            @Override
            public StoryReporter storyReporter(String storyPath) {
                return storyReporter;
            }
        };
        embedder.useConfiguration(configuration);
        List<CandidateSteps> candidateSteps = embedder.candidateSteps();
        StoryPathResolver resolver = configuration.storyPathResolver();
        List<String> storyPaths = new ArrayList<String>();
        Map<String, Story> stories = new HashMap<String, Story>();
        Meta meta = mock(Meta.class);
        for (Class<? extends Embeddable> embeddable : embeddables) {
            String storyPath = resolver.resolve(embeddable);
            storyPaths.add(storyPath);
            Story story = mock(Story.class);
            when(story.getMeta()).thenReturn(meta);
            stories.put(storyPath, story);
            when(runner.storyOfPath(configuration, storyPath)).thenReturn(story);
            assertThat(configuration.storyReporter(storyPath), sameInstance(storyReporter));
        }

        // When
        MetaFilter filter = mock(MetaFilter.class);
        when(filter.allow(meta)).thenReturn(false);
        embedder.runStoriesAsPaths(storyPaths);

        // Then
        for (String storyPath : storyPaths) {
            verify(runner).run(Matchers.eq(configuration), Matchers.eq(candidateSteps),
                    Matchers.eq(stories.get(storyPath)), Matchers.isA(MetaFilter.class));
            assertThat(out.toString(), containsString("Running story " + storyPath));
        }
        assertThatReportsViewGenerated(out);
    }

    @Test
    public void shouldProcessSystemProperties() throws Throwable {

        // Given
        StoryRunner runner = mock(StoryRunner.class);
        EmbedderControls embedderControls = new EmbedderControls();
        OutputStream out = new ByteArrayOutputStream();
        EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(new PrintStream(out));
        Embedder embedder = embedderWith(runner, embedderControls, monitor);

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
        StoryRunner runner = mock(StoryRunner.class);
        EmbedderControls embedderControls = new EmbedderControls().doSkip(true);
        OutputStream out = new ByteArrayOutputStream();
        EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(new PrintStream(out));
        List<? extends Class<? extends Embeddable>> embeddables = asList(MyStory.class, MyOtherStory.class);

        Embedder embedder = embedderWith(runner, embedderControls, monitor);
        Configuration configuration = embedder.configuration();
        List<CandidateSteps> candidateSteps = embedder.candidateSteps();
        StoryPathResolver resolver = configuration.storyPathResolver();
        List<String> storyPaths = new ArrayList<String>();
        Map<String, Story> stories = new HashMap<String, Story>();
        for (Class<? extends Embeddable> embeddable : embeddables) {
            String storyPath = resolver.resolve(embeddable);
            storyPaths.add(storyPath);
            Story story = mock(Story.class);
            stories.put(storyPath, story);
            when(runner.storyOfPath(configuration, storyPath)).thenReturn(story);
        }

        // When
        embedder.runStoriesAsPaths(storyPaths);

        // Then
        for (String storyPath : storyPaths) {
            verify(runner, never()).run(configuration, candidateSteps, stories.get(storyPath), MetaFilter.EMPTY);
            assertThat(out.toString(), not(containsString("Running story " + storyPath)));
        }
        assertThat(out.toString(), containsString("Skipped stories " + storyPaths));
    }

    @SuppressWarnings("unchecked")
    @Test(expected = RunningStoriesFailed.class)
    public void shouldThrowExceptionUponFailingStoriesAsPathsIfIgnoreFailureInStoriesFlagIsNotSet() throws Throwable {
        // Given
        StoryRunner runner = mock(StoryRunner.class);
        EmbedderControls embedderControls = new EmbedderControls();
        OutputStream out = new ByteArrayOutputStream();
        EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(new PrintStream(out));
        List<? extends Class<? extends Embeddable>> embeddables = asList(MyStory.class, MyOtherStory.class);

        Embedder embedder = embedderWith(runner, embedderControls, monitor);
        Configuration configuration = embedder.configuration();
        List<CandidateSteps> candidateSteps = embedder.candidateSteps();
        StoryPathResolver resolver = configuration.storyPathResolver();
        List<String> storyPaths = new ArrayList<String>();
        Map<String, Story> stories = new HashMap<String, Story>();
        for (Class<? extends Embeddable> embeddable : embeddables) {
            String storyPath = resolver.resolve(embeddable);
            storyPaths.add(storyPath);
            Story story = mock(Story.class);
            stories.put(storyPath, story);
            when(runner.storyOfPath(configuration, storyPath)).thenReturn(story);
        }
        for (String storyPath : storyPaths) {
            doThrow(new RuntimeException(storyPath + " failed")).when(runner).run(Matchers.eq(configuration),
                    Matchers.eq(candidateSteps), Matchers.eq(stories.get(storyPath)), Matchers.any(MetaFilter.class));
        }
        // When
        embedder.runStoriesAsPaths(storyPaths);

        // Then fail as expected

    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldNotThrowExceptionUponFailingStoriesAsPathsIfIgnoreFailureInStoriesFlagIsSet() throws Throwable {
        // Given
        StoryRunner runner = mock(StoryRunner.class);
        EmbedderControls embedderControls = new EmbedderControls().doIgnoreFailureInStories(true);
        OutputStream out = new ByteArrayOutputStream();
        EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(new PrintStream(out));
        List<? extends Class<? extends Embeddable>> embeddables = asList(MyStory.class, MyOtherStory.class);

        Embedder embedder = embedderWith(runner, embedderControls, monitor);
        Configuration configuration = embedder.configuration();
        List<CandidateSteps> candidateSteps = embedder.candidateSteps();
        StoryPathResolver resolver = configuration.storyPathResolver();
        List<String> storyPaths = new ArrayList<String>();
        Map<String, Story> stories = new HashMap<String, Story>();
        for (Class<? extends Embeddable> embeddable : embeddables) {
            String storyPath = resolver.resolve(embeddable);
            storyPaths.add(storyPath);
            Story story = mock(Story.class);
            stories.put(storyPath, story);
            when(runner.storyOfPath(configuration, storyPath)).thenReturn(story);
        }
        for (String storyPath : storyPaths) {
            doThrow(new RuntimeException(storyPath + " failed")).when(runner).run(Matchers.eq(configuration),
                    Matchers.eq(candidateSteps), Matchers.eq(stories.get(storyPath)), Matchers.any(MetaFilter.class));
        }

        // When
        embedder.runStoriesAsPaths(storyPaths);

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
        StoryRunner runner = mock(StoryRunner.class);
        EmbedderControls embedderControls = new EmbedderControls().doBatch(true);
        OutputStream out = new ByteArrayOutputStream();
        EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(new PrintStream(out));
        List<? extends Class<? extends Embeddable>> embeddables = asList(MyStory.class, MyOtherStory.class);

        Embedder embedder = embedderWith(runner, embedderControls, monitor);
        Configuration configuration = embedder.configuration();
        List<CandidateSteps> candidateSteps = embedder.candidateSteps();
        StoryPathResolver resolver = configuration.storyPathResolver();
        List<String> storyPaths = new ArrayList<String>();
        Map<String, Story> stories = new HashMap<String, Story>();
        for (Class<? extends Embeddable> embeddable : embeddables) {
            String storyPath = resolver.resolve(embeddable);
            storyPaths.add(storyPath);
            Story story = mock(Story.class);
            stories.put(storyPath, story);
            when(runner.storyOfPath(configuration, storyPath)).thenReturn(story);
        }
        for (String storyPath : storyPaths) {
            doNothing().when(runner).run(configuration, candidateSteps, stories.get(storyPath), MetaFilter.EMPTY);
        }

        // When
        embedder.runStoriesAsPaths(storyPaths);

        // Then
        for (String storyPath : storyPaths) {
            assertThat(out.toString(), containsString("Running story " + storyPath));
        }
    }

    @SuppressWarnings("unchecked")
    @Test(expected = RunningStoriesFailed.class)
    public void shouldThrowExceptionUponFailingStoriesAsPathsInBatchIfIgnoreFailureInStoriesFlagIsNotSet()
            throws Throwable {
        // Given
        StoryRunner runner = mock(StoryRunner.class);
        EmbedderControls embedderControls = new EmbedderControls().doBatch(true);
        OutputStream out = new ByteArrayOutputStream();
        EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(new PrintStream(out));
        List<? extends Class<? extends Embeddable>> embeddables = asList(MyStory.class, MyOtherStory.class);

        Embedder embedder = embedderWith(runner, embedderControls, monitor);
        Configuration configuration = embedder.configuration();
        List<CandidateSteps> candidateSteps = embedder.candidateSteps();
        StoryPathResolver resolver = configuration.storyPathResolver();
        List<String> storyPaths = new ArrayList<String>();
        Map<String, Story> stories = new HashMap<String, Story>();
        for (Class<? extends Embeddable> embeddable : embeddables) {
            String storyPath = resolver.resolve(embeddable);
            storyPaths.add(storyPath);
            Story story = mock(Story.class);
            stories.put(storyPath, story);
            when(runner.storyOfPath(configuration, storyPath)).thenReturn(story);
        }
        for (String storyPath : storyPaths) {
            doThrow(new RuntimeException(storyPath + " failed")).when(runner).run(Matchers.eq(configuration),
                    Matchers.eq(candidateSteps), Matchers.eq(stories.get(storyPath)), Matchers.any(MetaFilter.class));
        }

        // When
        embedder.runStoriesAsPaths(storyPaths);

        // Then fail as expected

    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldRunFailingStoriesAsPathsInBatchIfBatchFlagIsSet() throws Throwable {
        // Given
        StoryRunner runner = mock(StoryRunner.class);
        EmbedderControls embedderControls = new EmbedderControls().doBatch(true).doIgnoreFailureInStories(true);
        OutputStream out = new ByteArrayOutputStream();
        EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(new PrintStream(out));
        List<? extends Class<? extends Embeddable>> embeddables = asList(MyStory.class, MyOtherStory.class);

        Embedder embedder = embedderWith(runner, embedderControls, monitor);
        Configuration configuration = embedder.configuration();
        List<CandidateSteps> candidateSteps = embedder.candidateSteps();
        StoryPathResolver resolver = configuration.storyPathResolver();
        List<String> storyPaths = new ArrayList<String>();
        Map<String, Story> stories = new HashMap<String, Story>();
        for (Class<? extends Embeddable> embeddable : embeddables) {
            String storyPath = resolver.resolve(embeddable);
            storyPaths.add(storyPath);
            Story story = mock(Story.class);
            stories.put(storyPath, story);
            when(runner.storyOfPath(configuration, storyPath)).thenReturn(story);
        }
        BatchFailures failures = new BatchFailures();
        for (String storyPath : storyPaths) {
            RuntimeException thrown = new RuntimeException(storyPath + " failed");
            failures.put(storyPath, thrown);
            doThrow(thrown).when(runner).run(Matchers.eq(configuration), Matchers.eq(candidateSteps),
                    Matchers.eq(stories.get(storyPath)), Matchers.any(MetaFilter.class));
        }

        // When
        embedder.runStoriesAsPaths(storyPaths);

        // Then
        for (String storyPath : storyPaths) {
            assertThat(out.toString(), containsString("Running story " + storyPath));
        }
        assertThat(out.toString(), containsString("Failed to run batch " + failures));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldNotGenerateViewWhenRunningStoriesAsPathsIfGenerateViewAfterStoriesFlagIsNotSet() throws Throwable {
        // Given
        StoryRunner runner = mock(StoryRunner.class);
        EmbedderControls embedderControls = new EmbedderControls().doGenerateViewAfterStories(false);
        OutputStream out = new ByteArrayOutputStream();
        EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(new PrintStream(out));
        List<? extends Class<? extends Embeddable>> embeddables = asList(MyStory.class, MyOtherStory.class);

        Embedder embedder = embedderWith(runner, embedderControls, monitor);
        Configuration configuration = embedder.configuration();
        List<CandidateSteps> candidateSteps = embedder.candidateSteps();
        StoryPathResolver resolver = configuration.storyPathResolver();
        List<String> storyPaths = new ArrayList<String>();
        Map<String, Story> stories = new HashMap<String, Story>();
        for (Class<? extends Embeddable> embeddable : embeddables) {
            String storyPath = resolver.resolve(embeddable);
            storyPaths.add(storyPath);
            Story story = mock(Story.class);
            stories.put(storyPath, story);
            when(runner.storyOfPath(configuration, storyPath)).thenReturn(story);
        }

        // When
        embedder.runStoriesAsPaths(storyPaths);

        // Then
        for (String storyPath : storyPaths) {
            verify(runner).run(Matchers.eq(configuration), Matchers.eq(candidateSteps),
                    Matchers.eq(stories.get(storyPath)), Matchers.any(MetaFilter.class));
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
        embedder.runStoriesWithAnnotatedEmbedderRunner(AnnotatedEmbedderRunner.class.getName(),
                asList(runWithEmbedderRunner));
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
        embedder.runStoriesWithAnnotatedEmbedderRunner(AnnotatedEmbedderRunner.class.getName(),
                asList(runWithEmbedderRunner));
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
        embedder.runStoriesWithAnnotatedEmbedderRunner(AnnotatedEmbedderRunner.class.getName(),
                asList(runWithEmbedderRunner));
        // Then fail as expected
    }

    @Test(expected = AnnotatedEmbedderRunnerInstantiationFailed.class)
    public void shouldFailWhenRunningInexistingStoriesWithAnnotatedEmbedderRunner() throws Throwable {
        // Given
        Embedder embedder = new Embedder();
        embedder.useClassLoader(new EmbedderClassLoader(this.getClass().getClassLoader()));
        String runWithEmbedderRunner = "InexistingRunner";
        // When
        embedder.runStoriesWithAnnotatedEmbedderRunner(AnnotatedEmbedderRunner.class.getName(),
                asList(runWithEmbedderRunner));
        // Then fail as expected
    }

    @Test
    public void shouldGenerateReportsViewFromExistingReports() throws Throwable {
        // Given
        StoryRunner runner = mock(StoryRunner.class);
        EmbedderControls embedderControls = new EmbedderControls().doGenerateViewAfterStories(false);
        OutputStream out = new ByteArrayOutputStream();
        EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(new PrintStream(out));
        ViewGenerator viewGenerator = mock(ViewGenerator.class);

        Embedder embedder = embedderWith(runner, embedderControls, monitor);
        embedder.configuration().useViewGenerator(viewGenerator);

        File outputDirectory = new File("target/output");
        List<String> formats = asList("html");
        Properties viewResources = new Properties();
        when(viewGenerator.getReportsCount()).thenReturn(new ReportsCount(2, 0, 1, 2, 0, 0, 1));
        embedder.generateReportsView(outputDirectory, formats, viewResources);

        // Then
        verify(viewGenerator).generateReportsView(outputDirectory, formats, viewResources);
        assertThatReportsViewGenerated(out);
    }

    @Test
    public void shouldNotGenerateViewIfSkipFlagIsSet() throws Throwable {
        // Given
        StoryRunner runner = mock(StoryRunner.class);
        EmbedderControls embedderControls = new EmbedderControls().doSkip(true);
        OutputStream out = new ByteArrayOutputStream();
        EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(new PrintStream(out));
        ViewGenerator viewGenerator = mock(ViewGenerator.class);

        Embedder embedder = embedderWith(runner, embedderControls, monitor);
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
        StoryRunner runner = mock(StoryRunner.class);
        EmbedderControls embedderControls = new EmbedderControls();
        OutputStream out = new ByteArrayOutputStream();
        EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(new PrintStream(out));
        ViewGenerator viewGenerator = mock(ViewGenerator.class);

        Embedder embedder = embedderWith(runner, embedderControls, monitor);
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
        StoryRunner runner = mock(StoryRunner.class);
        EmbedderControls embedderControls = new EmbedderControls();
        OutputStream out = new ByteArrayOutputStream();
        EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(new PrintStream(out));
        ViewGenerator viewGenerator = mock(ViewGenerator.class);

        Embedder embedder = embedderWith(runner, embedderControls, monitor);
        embedder.configuration().useViewGenerator(viewGenerator);
        File outputDirectory = new File("target/output");
        List<String> formats = asList("html");
        Properties viewResources = new Properties();
        when(viewGenerator.getReportsCount()).thenReturn(new ReportsCount(1, 0, 1, 2, 1, 1, 1));
        embedder.generateReportsView(outputDirectory, formats, viewResources);

        // Then fail as expected
    }

    @Test
    public void shouldNotThrowExceptionIfScenariosFailedAndIgnoreFlagIsSet() throws Throwable {
        // Given
        StoryRunner runner = mock(StoryRunner.class);
        EmbedderControls embedderControls = new EmbedderControls().doIgnoreFailureInView(true);
        OutputStream out = new ByteArrayOutputStream();
        EmbedderMonitor monitor = new PrintStreamEmbedderMonitor(new PrintStream(out));
        ViewGenerator viewGenerator = mock(ViewGenerator.class);

        Embedder embedder = embedderWith(runner, embedderControls, monitor);
        embedder.configuration().useViewGenerator(viewGenerator);
        File outputDirectory = new File("target/output");
        List<String> formats = asList("html");
        Properties viewResources = new Properties();
        when(viewGenerator.getReportsCount()).thenReturn(new ReportsCount(1, 0, 1, 2, 1, 0, 1));
        embedder.generateReportsView(outputDirectory, formats, viewResources);

        // Then
        verify(viewGenerator).generateReportsView(outputDirectory, formats, viewResources);
        assertThatReportsViewGenerated(out);
    }

    @Test
    public void shouldAllowOverrideOfDefaultDependencies() throws Throwable {
        // Given
        StoryRunner runner = new StoryRunner();
        EmbedderControls embedderControls = new EmbedderControls();
        EmbedderMonitor monitor = new PrintStreamEmbedderMonitor();

        // When
        Embedder embedder = embedderWith(runner, embedderControls, monitor);
        assertThat(embedder.embedderControls(), is(sameInstance(embedderControls)));
        assertThat(embedder.storyRunner(), is(sameInstance(runner)));
        assertThat(embedder.embedderMonitor(), is(sameInstance(monitor)));
        embedder.useStoryRunner(new StoryRunner());
        embedder.useEmbedderMonitor(new PrintStreamEmbedderMonitor());

        // Then
        assertThat(embedder.storyRunner(), is(not(sameInstance(runner))));
        assertThat(embedder.embedderMonitor(), is(not(sameInstance(monitor))));
    }

    private Embedder embedderWith(StoryRunner runner, EmbedderControls embedderControls, EmbedderMonitor monitor) {
        Embedder embedder = new Embedder(new StoryMapper(), runner, monitor);
        embedder.useEmbedderControls(embedderControls);
        return embedder;
    }

    private Embedder embedderWith(StoryMapper mapper, StoryRunner runner, EmbedderControls embedderControls,
            EmbedderMonitor monitor) {
        Embedder embedder = new Embedder(mapper, runner, monitor);
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

        // When
        String embedderAsString = embedder.toString();

        // Then
        assertThat(embedderAsString, containsString(MostUsefulConfiguration.class.getSimpleName()));
        assertThat(embedderAsString, containsString(StoryRunner.class.getSimpleName()));
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
        assertThat(new File(storyReporterBuilder.outputDirectory(),"view/xref."+ext).exists(), is(true));
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

    private class MyStory extends JUnitStory {
    }

    private abstract class MyAbstractStory extends JUnitStory {
    }

    private class MyFailingStory extends JUnitStory {

        @Override
        public void run() throws Throwable {
            throw new RuntimeException("Failed");
        }
    }

    private class MyOtherStory extends JUnitStory {
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
