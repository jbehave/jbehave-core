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
import org.jbehave.core.embedder.Embedder.RunningStoriesFailed;
import org.jbehave.core.embedder.Embedder.ViewGenerationFailed;
import org.jbehave.core.failures.BatchFailures;
import org.jbehave.core.io.StoryPathResolver;
import org.jbehave.core.io.UnderscoredCamelCaseResolver;
import org.jbehave.core.junit.AnnotatedEmbedderRunner;
import org.jbehave.core.junit.JUnitStory;
import org.jbehave.core.model.Story;
import org.jbehave.core.reporters.FreemarkerViewGenerator;
import org.jbehave.core.reporters.PrintStreamStepdocReporter;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.reporters.ViewGenerator;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.StepFinder;
import org.jbehave.core.steps.Steps;
import org.junit.Test;
import org.junit.runner.RunWith;

public class EmbedderBehaviour {

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
        embedder.useConfiguration(configuration);
        embedder.useCandidateSteps(asList(steps));
        embedder.runStoriesAsEmbeddables(classNames, classLoader);

        // Then
        for (Embeddable embeddable : embeddables) {
            assertThat(out.toString(), containsString("Running embeddable " + embeddable.getClass().getName()));
        }
        assertThatStoriesViewGenerated(out);
    }

    private void assertThatStoriesViewGenerated(OutputStream out) {
        assertThat(out.toString(), containsString("Generating stories view"));
        assertThat(out.toString(), containsString("Stories view generated"));
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

        Embedder embedder = embedderWith(runner, embedderControls, monitor);
        embedder.configuration().useStoryPathResolver(new UnderscoredCamelCaseResolver());
        embedder.runStoriesAsEmbeddables(classNames, classLoader);

        // Then
        for (Embeddable embeddable : embeddables) {
            assertThat(out.toString(), not(containsString("Running embeddable " + embeddable.getClass().getName())));
        }
        assertThat(out.toString(), not(containsString("Generating stories view")));
    }

    @Test(expected = RunningStoriesFailed.class)
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
        embedder.runStoriesAsEmbeddables(classNames, classLoader);

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
        embedder.runStoriesAsEmbeddables(classNames, classLoader);

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
        embedder.runStoriesAsEmbeddables(classNames, classLoader);

        // Then
        for (Embeddable story : embeddables) {
            String name = story.getClass().getName();
            assertThat(out.toString(), containsString("Running embeddable " + name));
        }
    }

    @Test(expected = RunningStoriesFailed.class)
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
        embedder.runStoriesAsEmbeddables(classNames, classLoader);

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
        embedder.runStoriesAsEmbeddables(classNames, classLoader);

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
        embedder.runStoriesAsEmbeddables(classNames, classLoader);

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
            StoryReporter storyReporter = mock(StoryReporter.class);
            configuration.useStoryReporter(storyPath, storyReporter);
            assertThat(configuration.storyReporter(storyPath), sameInstance(storyReporter));
        }
        
        // When
        embedder.runStoriesAsPaths(storyPaths);

        // Then
        for (String storyPath : storyPaths) {
            verify(runner).run(configuration, candidateSteps, stories.get(storyPath));
            assertThat(out.toString(), containsString("Running story " + storyPath));
        }
        assertThatStoriesViewGenerated(out);
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
            verify(runner, never()).run(configuration, candidateSteps, stories.get(storyPath));
            assertThat(out.toString(), not(containsString("Running story " + storyPath)));
        }
        assertThat(out.toString(), containsString("Skipped stories "+storyPaths));
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
            doThrow(new RuntimeException(storyPath + " failed")).when(runner).run(configuration, candidateSteps,
                    stories.get(storyPath));
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
            doThrow(new RuntimeException(storyPath + " failed")).when(runner).run(configuration, candidateSteps,
                    stories.get(storyPath));
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
            doNothing().when(runner).run(configuration, candidateSteps, stories.get(storyPath));
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
            doThrow(new RuntimeException(storyPath + " failed")).when(runner).run(configuration, candidateSteps,
                    stories.get(storyPath));
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
            doThrow(thrown).when(runner).run(configuration, candidateSteps,
                    stories.get(storyPath));
        }
        
        // When
        embedder.runStoriesAsPaths(storyPaths);

        // Then
        for (String storyPath : storyPaths) {
            assertThat(out.toString(), containsString("Running story " + storyPath));
        }
        assertThat(out.toString(), containsString("Failed to run batch "+failures));
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
            verify(runner).run(configuration, candidateSteps, stories.get(storyPath));
            assertThat(out.toString(), containsString("Running story " + storyPath));
        }
        assertThat(out.toString(), not(containsString("Generating stories view")));
        assertThat(out.toString(), not(containsString("Stories view generated")));
    }

    @Test
    public void shouldRunStoriesWithAnnotatedEmbedderRunnerIfEmbeddable() throws Throwable {
        // Given
        Embedder embedder = new Embedder();
        String runWithEmbedderRunner = RunningWithAnnotatedEmbedderRunner.class.getName();
        EmbedderClassLoader classLoader = new EmbedderClassLoader(this.getClass().getClassLoader());
        // When
        embedder.runStoriesWithAnnotatedEmbedderRunner(AnnotatedEmbedderRunner.class.getName(),
                asList(runWithEmbedderRunner), classLoader);
        // Then
        assertThat(RunningWithAnnotatedEmbedderRunner.hasRun, is(true));
    }

    @Test
    public void shouldNotRunStoriesWithAnnotatedEmbedderRunnerIfNotEmbeddable() throws Throwable {
        // Given
        Embedder embedder = new Embedder();
        String runWithEmbedderRunner = NotEmbeddableWithAnnotatedEmbedderRunner.class.getName();
        EmbedderClassLoader classLoader = new EmbedderClassLoader(this.getClass().getClassLoader());
        // When
        embedder.runStoriesWithAnnotatedEmbedderRunner(AnnotatedEmbedderRunner.class.getName(),
                asList(runWithEmbedderRunner), classLoader);
        // Then
        assertThat(NotEmbeddableWithAnnotatedEmbedderRunner.hasRun, is(false));
    }

    @Test(expected = RuntimeException.class)
    public void shouldRethowFailuresWhenRunningWithAnnotatedEmbedderRunner() throws Throwable {
        // Given
        Embedder embedder = new Embedder();
        String runWithEmbedderRunner = FailingWithAnnotatedEmbedderRunner.class.getName();
        EmbedderClassLoader classLoader = new EmbedderClassLoader(this.getClass().getClassLoader());
        // When
        embedder.runStoriesWithAnnotatedEmbedderRunner(AnnotatedEmbedderRunner.class.getName(),
                asList(runWithEmbedderRunner), classLoader);
        // Then fail as expected
    }

    @Test
    public void shouldGenerateStoriesView() throws Throwable {
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
        Properties viewResources = FreemarkerViewGenerator.defaultResources();
        when(viewGenerator.countStories()).thenReturn(2);
        when(viewGenerator.countScenarios()).thenReturn(2);
        when(viewGenerator.countFailedScenarios()).thenReturn(0);
        embedder.generateStoriesView(outputDirectory, formats, viewResources);

        // Then
        verify(viewGenerator).generateView(outputDirectory, formats, viewResources);
        assertThatStoriesViewGenerated(out);
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
        Properties viewResources = FreemarkerViewGenerator.defaultResources();
        embedder.generateStoriesView(outputDirectory, formats, viewResources);

        // Then
        verify(viewGenerator, never()).generateView(outputDirectory, formats, viewResources);
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
        Properties viewResources = FreemarkerViewGenerator.defaultResources();
        doThrow(new RuntimeException()).when(viewGenerator).generateView(outputDirectory, formats, viewResources);
        embedder.generateStoriesView(outputDirectory, formats, viewResources);

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
        Properties viewResources = FreemarkerViewGenerator.defaultResources();
        when(viewGenerator.countStories()).thenReturn(1);
        when(viewGenerator.countScenarios()).thenReturn(2);
        when(viewGenerator.countFailedScenarios()).thenReturn(1);
        embedder.generateStoriesView(outputDirectory, formats, viewResources);

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
        Properties viewResources = FreemarkerViewGenerator.defaultResources();
        when(viewGenerator.countStories()).thenReturn(1);
        when(viewGenerator.countScenarios()).thenReturn(2);
        when(viewGenerator.countFailedScenarios()).thenReturn(1);
        embedder.generateStoriesView(outputDirectory, formats, viewResources);

        // Then
        verify(viewGenerator).generateView(outputDirectory, formats, viewResources);
        assertThatStoriesViewGenerated(out);
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
        Embedder embedder = new Embedder(runner, monitor);
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

    private String dos2unix(String string) {
        return string.replace("\r\n", "\n");
    }

    private class MyStory extends JUnitStory {
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
