package org.jbehave.ant;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.Project;
import org.jbehave.core.InjectableEmbedder;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.embedder.EmbedderClassLoader;
import org.jbehave.core.embedder.EmbedderControls;
import org.jbehave.core.embedder.EmbedderMonitor;
import org.jbehave.core.embedder.executors.ExecutorServiceFactory;
import org.jbehave.core.failures.BatchFailures;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.reporters.Format;
import org.jbehave.core.reporters.ReportsCount;
import org.junit.Test;

import static java.util.Arrays.asList;
import static org.apache.tools.ant.Project.MSG_INFO;
import static org.apache.tools.ant.Project.MSG_WARN;

import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class EmbedderTaskBehaviour {

    private Embedder embedder = mock(Embedder.class);
    private static final ExecutorService EXECUTOR_SERVICE = mock(ExecutorService.class);

    @Test
    public void shouldCreateNewEmbedderWithDefaultControls() {
        // Given
        AbstractEmbedderTask task = new AbstractEmbedderTask() {
        };
        // When
        Embedder embedder = task.newEmbedder();
        // Then
        EmbedderControls embedderControls = embedder.embedderControls();
        assertThat(embedderControls.batch(), is(false));
        assertThat(embedderControls.generateViewAfterStories(), is(true));
        assertThat(embedderControls.ignoreFailureInStories(), is(false));
        assertThat(embedderControls.ignoreFailureInView(), is(false));
        assertThat(embedderControls.verboseFailures(), is(false));
        assertThat(embedderControls.verboseFiltering(), is(false));
        assertThat(embedderControls.skip(), is(false));
        assertThat(embedderControls.storyTimeoutInSecs(), equalTo(300L));
        assertThat(embedderControls.storyTimeoutInSecsByPath(), equalTo(""));
        assertThat(embedderControls.failOnStoryTimeout(), is(false));
        assertThat(embedderControls.threads(), equalTo(1));  
    }

    @Test
    public void shouldCreateNewEmbedderWithGivenControls() {
        // Given
        AbstractEmbedderTask task = new AbstractEmbedderTask() {
        };
        // When
        task.setBatch(true);
        task.setGenerateViewAfterStories(false);
        task.setIgnoreFailureInStories(true);
        task.setIgnoreFailureInView(true);
        task.setVerboseFailures(true);
        task.setVerboseFiltering(true);
        task.setSkip(true);
        task.setStoryTimeoutInSecs(60);
        task.setStoryTimeoutInSecsByPath("**/shorts/BddShortTest.story:5");
        task.setFailOnStoryTimeout(true);
        task.setThreads(2);
        Embedder embedder = task.newEmbedder();
        // Then
        EmbedderControls embedderControls = embedder.embedderControls();
        assertThat(embedderControls.batch(), is(true));
        assertThat(embedderControls.generateViewAfterStories(), is(false));
        assertThat(embedderControls.ignoreFailureInStories(), is(true));
        assertThat(embedderControls.ignoreFailureInView(), is(true));
        assertThat(embedderControls.verboseFailures(), is(true));
        assertThat(embedderControls.verboseFiltering(), is(true));
        assertThat(embedderControls.skip(), is(true));
        assertThat(embedderControls.storyTimeoutInSecs(), equalTo(60L));
        assertThat(embedderControls.storyTimeoutInSecsByPath(), equalTo("**/shorts/BddShortTest.story:5"));
        assertThat(embedderControls.failOnStoryTimeout(), is(true));
        assertThat(embedderControls.threads(), equalTo(2));        
    }

    @Test
    public void shouldCreateNewEmbedderWithAntMonitor() {
        // Given
        Project project = mock(Project.class);
        AbstractEmbedderTask task = new AbstractEmbedderTask() {
        };
        task.setProject(project);
        // When
        Embedder embedder = task.newEmbedder();
        // Then
        EmbedderMonitor embedderMonitor = embedder.embedderMonitor();
        assertThat(embedderMonitor.toString(), equalTo("AntEmbedderMonitor"));

        // and verify monitor calls are propagated to Project log
        BatchFailures failures = new BatchFailures();
        embedderMonitor.batchFailed(failures);
        verify(project).log(task, "Failed to run batch " + failures, MSG_WARN);

        String name = "name";
        Throwable cause = new RuntimeException();
        embedderMonitor.embeddableFailed(name, cause);
        verify(project).log(task, "Failed to run embeddable " + name, cause, MSG_WARN);

        List<String> classNames = asList("name1", "name2");
        embedderMonitor.embeddablesSkipped(classNames);
        verify(project).log(task, "Skipped embeddables " + classNames, MSG_INFO);

        embedderMonitor.runningEmbeddable(name);
        verify(project).log(task, "Running embeddable " + name, MSG_INFO);

        List<String> storyPaths = asList("/path1", "/path2");
        embedderMonitor.storiesSkipped(storyPaths);
        verify(project).log(task, "Skipped stories " + storyPaths, MSG_INFO);

        String path = "/path";
        embedderMonitor.storyFailed(path, cause);
        verify(project).log(task, "Failed to run story " + path, cause, MSG_WARN);

        embedderMonitor.runningStory(path);
        verify(project).log(task, "Running story " + path, MSG_INFO);

        Object annotatedInstance = new Object();
        Class<?> type = Object.class;
        embedderMonitor.annotatedInstanceNotOfType(annotatedInstance, type);
        verify(project).log(task, "Annotated instance " + annotatedInstance + " not of type " + type, MSG_WARN);

        File outputDirectory = new File("/dir");
        List<String> formats = asList(Format.CONSOLE.name(), Format.HTML.name());
        Properties viewProperties = new Properties();
        embedderMonitor.generatingReportsView(outputDirectory, formats, viewProperties);
        verify(project).log(
                task,
                "Generating reports view to '" + outputDirectory + "' using formats '" + formats + "'"
                        + " and view properties '" + viewProperties + "'", MSG_INFO);

        embedderMonitor.reportsViewGenerationFailed(outputDirectory, formats, viewProperties, cause);
        verify(project).log(
                task,
                "Failed to generate reports view to '" + outputDirectory + "' using formats '" + formats + "'"
                        + " and view properties '" + viewProperties + "'", cause, MSG_WARN);

        int stories = 2;
        int storiesNotAllowed = 1;
        int storiesPending = 1;
        int scenarios = 4;
        int scenariosFailed = 1;
        int scenariosNotAllowed = 0;
        int scenariosPending = 1;
        int stepsFailed = 1;
        embedderMonitor.reportsViewGenerated(new ReportsCount(stories, storiesNotAllowed, storiesPending, scenarios,
                scenariosFailed, scenariosNotAllowed, scenariosPending, stepsFailed));
        verify(project).log(
                task,
                "Reports view generated with " + stories + " stories (of which "+storiesPending+" pending) containing " + scenarios
                        + " scenarios (of which " + scenariosPending + " pending)", MSG_INFO);
        verify(project).log(
                task,
                "Meta filters excluded " + storiesNotAllowed + " stories and  " + scenariosNotAllowed
                        + " scenarios", MSG_INFO);

        embedderMonitor.reportsViewNotGenerated();
        verify(project).log(task, "Reports view not generated", MSG_INFO);

    }

    @Test
    public void shouldCreateNewEmbedderWithSystemProperties() throws IOException {
        // Given
        AbstractEmbedderTask task = new AbstractEmbedderTask() {
        };
        // When
        Properties systemProperties = new Properties();
        systemProperties.setProperty("one", "1");
        systemProperties.setProperty("two", "2");        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        systemProperties.store(out, "");
        task.setSystemProperties(out.toString());
        Embedder embedder = task.newEmbedder();
        // Then
        assertThat(embedder.systemProperties(), equalTo(systemProperties));
    }
    
    @Test
    public void shouldAllowTestScopedSearchDirectory() {
        // Given
        AbstractEmbedderTask task = new AbstractEmbedderTask() {
        };
        // When
        task.setTestSourceDirectory("src/test");
        task.setTestOutputDirectory("target/test-classes");
        task.setScope("test");
        // Then
        assertThat(task.searchDirectory(), equalTo("src/test"));
    }

    @Test
    public void shouldAllowSpecificationOfEmbedderClass() {
        // Given
        AbstractEmbedderTask task = new AbstractEmbedderTask() {
        };
        // When
        task.setEmbedderClass(MyEmbedder.class.getName());
        Embedder embedder = task.newEmbedder();
        // Then
        assertThat(embedder.getClass().getName(), equalTo(MyEmbedder.class.getName()));
    }

    @Test
    public void shouldCreateNewEmbedderWithExecutors() {
        // Given
        AbstractEmbedderTask task = new AbstractEmbedderTask() {
        };
        // When
        task.setExecutorsClass(MyExecutors.class.getName());
        Embedder embedder = task.newEmbedder();
        // Then
        assertThat(embedder.executorService(), sameInstance(EXECUTOR_SERVICE));
    }

    public static class MyEmbedder extends Embedder {

    }

    @Test
    public void shouldAllowSpecificationOfInjectableEmbedderClass() {
        // Given
        AbstractEmbedderTask task = new AbstractEmbedderTask() {
        };
        // When
        task.setInjectableEmbedderClass(MyInjectableEmbedder.class.getName());
        Embedder embedder = task.newEmbedder();
        // Then
        assertThat(embedder.getClass().getName(), equalTo(MyEmbedder.class.getName()));
    }

    public static class MyInjectableEmbedder extends InjectableEmbedder {

        public MyInjectableEmbedder() {
            useEmbedder(new MyEmbedder());
        }

        public void run() throws Throwable {
        }

    }

    @Test
    public void shouldAllowSpecificationOfStoryFinderClass() {
        // Given
        AbstractEmbedderTask task = new AbstractEmbedderTask() {
        };
        // When
        task.setStoryFinderClass(MyStoryFinder.class.getName());
        StoryFinder storyFinder = task.newStoryFinder();
        // Then
        assertThat(storyFinder.getClass().getName(), equalTo(MyStoryFinder.class.getName()));
    }

    public static class MyStoryFinder extends StoryFinder {

    }

    @Test
    public void shouldMapStoriesAsEmbeddables() {
        // Given
        final EmbedderClassLoader classLoader = new EmbedderClassLoader(this.getClass().getClassLoader());
        MapStoriesAsEmbeddables task = new MapStoriesAsEmbeddables() {
            @Override
            protected Embedder newEmbedder() {
                return embedder;
            }

            @Override
            protected EmbedderClassLoader classLoader() {
                return classLoader;
            }

        };
        String searchInDirectory = "src/test/java/";
        task.setSourceDirectory(searchInDirectory);
        task.setOutputDirectory("target/test-classes");
        List<String> includes = asList("**/*StoryMaps.java");
        task.setIncludes(StringUtils.join(includes, "'"));
        List<String> excludes = asList();
        task.setExcludes(StringUtils.join(excludes, "'"));
        List<String> classNames = new StoryFinder().findClassNames(searchInDirectory, includes, excludes);

        // When
        task.execute();

        // Then
        verify(embedder).runAsEmbeddables(classNames);
    }

    @Test
    public void shouldMapStoriesAsPaths() {
        // Given
        final EmbedderClassLoader classLoader = new EmbedderClassLoader(this.getClass().getClassLoader());
        MapStoriesAsPaths task = new MapStoriesAsPaths() {
            @Override
            protected Embedder newEmbedder() {
                return embedder;
            }

            @Override
            protected EmbedderClassLoader classLoader() {
                return classLoader;
            }

        };
        String searchInDirectory = "src/test/java/";
        task.setSourceDirectory(searchInDirectory);
        List<String> includes = asList("**/stories/*.story");
        task.setIncludes(StringUtils.join(includes, "'"));
        List<String> excludes = asList();
        task.setExcludes(StringUtils.join(excludes, "'"));
        List<String> storyPaths = new StoryFinder().findPaths(searchInDirectory, includes, excludes);

        // When
        task.execute();

        // Then
        verify(embedder).mapStoriesAsPaths(storyPaths);
    }

    @Test
    public void shouldGenerateStoriesView() {
        // Given
        GenerateStoriesView task = new GenerateStoriesView() {
            @Override
            protected Embedder newEmbedder() {
                return embedder;
            }

        };
        // When
        task.execute();

        // Then
        verify(embedder).generateReportsView();
    }

    @Test
    public void shouldReportStepdocs() {
        // Given
        ReportStepdocs task = new ReportStepdocs() {
            @Override
            protected Embedder newEmbedder() {
                return embedder;
            }

        };
        // When
        task.execute();

        // Then
        verify(embedder).reportStepdocs();
    }

    @Test
    public void shouldRunStoriesAsEmbeddables() {
        // Given
        final EmbedderClassLoader classLoader = new EmbedderClassLoader(this.getClass().getClassLoader());
        RunStoriesAsEmbeddables task = new RunStoriesAsEmbeddables() {
            @Override
            protected Embedder newEmbedder() {
                return embedder;
            }

            @Override
            protected EmbedderClassLoader classLoader() {
                return classLoader;
            }

        };
        String searchInDirectory = "src/test/java/";
        task.setSourceDirectory(searchInDirectory);
        List<String> includes = asList("**/stories/*.java");
        task.setIncludes(StringUtils.join(includes, "'"));
        List<String> excludes = asList();
        task.setExcludes(StringUtils.join(excludes, "'"));
        List<String> classNames = new StoryFinder().findClassNames(searchInDirectory, includes, excludes);

        // When
        task.execute();

        // Then
        verify(embedder).runAsEmbeddables(classNames);
    }

    @Test
    public void shouldRunStoriesAsPaths() {
        // Given
        final EmbedderClassLoader classLoader = new EmbedderClassLoader(this.getClass().getClassLoader());
        RunStoriesAsPaths task = new RunStoriesAsPaths() {
            @Override
            protected Embedder newEmbedder() {
                return embedder;
            }

            @Override
            protected EmbedderClassLoader classLoader() {
                return classLoader;
            }

        };
        String searchInDirectory = "src/test/java/";
        task.setSourceDirectory(searchInDirectory);
        String outputDirectory = "target/test-classes";
        task.setOutputDirectory(outputDirectory);
        List<String> includes = asList("**/stories/*.story");
        task.setIncludes(StringUtils.join(includes, "'"));
        List<String> excludes = asList();
        task.setExcludes(StringUtils.join(excludes, "'"));
        List<String> storyPaths = new StoryFinder().findPaths(searchInDirectory, includes, excludes);

        // When
        task.execute();

        // Then
        verify(embedder).runStoriesAsPaths(storyPaths);
        assertThat(task.codeLocation().toString(), containsString(outputDirectory));
    }

    @Test
    public void shouldRunStoriesWithAnnotatedEmbedderRunner() {
        // Given
        final EmbedderClassLoader classLoader = new EmbedderClassLoader(this.getClass().getClassLoader());
        RunStoriesWithAnnotatedEmbedderRunner task = new RunStoriesWithAnnotatedEmbedderRunner() {
            @Override
            protected Embedder newEmbedder() {
                return embedder;
            }

            @Override
            protected EmbedderClassLoader classLoader() {
                return classLoader;
            }

        };
        String searchInDirectory = "src/test/java/";
        task.setSourceDirectory(searchInDirectory);
        List<String> includes = asList("**/stories/*.java");
        task.setIncludes(StringUtils.join(includes, "'"));
        List<String> excludes = asList();
        task.setExcludes(StringUtils.join(excludes, "'"));
        List<String> classNames = new StoryFinder().findClassNames(searchInDirectory, includes, excludes);

        // When
        task.execute();

        // Then
        verify(embedder).runStoriesWithAnnotatedEmbedderRunner(classNames);
    }

    public static class MyExecutors implements ExecutorServiceFactory {

        public ExecutorService create(EmbedderControls controls) {
            return EXECUTOR_SERVICE;
        }
        
    }

}
