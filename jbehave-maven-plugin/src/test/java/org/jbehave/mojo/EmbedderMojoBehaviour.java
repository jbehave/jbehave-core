package org.jbehave.mojo;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;
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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class EmbedderMojoBehaviour {

    private Embedder embedder = mock(Embedder.class);
    private static final ExecutorService EXECUTOR_SERVICE = mock(ExecutorService.class);


    @Test
    public void shouldCreateNewEmbedderWithDefaultControls() {
        // Given
        AbstractEmbedderMojo mojo = new AbstractEmbedderMojo() {
            @Override
            public void execute() {
            }
        };
        // When
        Embedder embedder = mojo.newEmbedder();
        // Then
        EmbedderControls embedderControls = embedder.embedderControls();
        assertThat(embedderControls.batch(), is(false));
        assertThat(embedderControls.generateViewAfterStories(), is(true));
        assertThat(embedderControls.ignoreFailureInStories(), is(false));
        assertThat(embedderControls.ignoreFailureInView(), is(false));
        assertThat(embedderControls.threads(), is(1));
        assertThat(embedderControls.skip(), is(false));
        assertThat(embedderControls.storyTimeouts(), equalTo("300"));
        assertThat(embedderControls.failOnStoryTimeout(), is(false));
        assertThat(embedderControls.threads(), equalTo(1));
    }

    @Test
    public void shouldCreateNewEmbedderWithGivenControls() {
        // Given
        AbstractEmbedderMojo mojo = new AbstractEmbedderMojo() {
            @Override
            public void execute() {
            }
        };
        // When
        mojo.batch = true;
        mojo.generateViewAfterStories = false;
        mojo.ignoreFailureInStories = true;
        mojo.ignoreFailureInView = true;
        mojo.skip = true;
        mojo.storyTimeouts = "**/longs/BddTest2Long.feature:7";
        mojo.failOnStoryTimeout = true;
        mojo.threads = 2;
        Embedder embedder = mojo.newEmbedder();
        // Then
        EmbedderControls embedderControls = embedder.embedderControls();
        assertThat(embedderControls.batch(), is(true));
        assertThat(embedderControls.generateViewAfterStories(), is(false));
        assertThat(embedderControls.ignoreFailureInStories(), is(true));
        assertThat(embedderControls.ignoreFailureInView(), is(true));
        assertThat(embedderControls.skip(), is(true));
        assertThat(embedderControls.storyTimeouts(), is("**/longs/BddTest2Long.feature:7"));
        assertThat(embedderControls.threads(), is(2));
    }

    @Test
    public void shouldCreateNewEmbedderWithExecutors() {
        // Given
        AbstractEmbedderMojo mojo = new AbstractEmbedderMojo() {
            @Override
            public void execute() {
            }
        };
        // When
        mojo.executorsClass = MyExecutors.class.getName();
        Embedder embedder = mojo.newEmbedder();
        // Then
        assertThat(embedder.executorService(), sameInstance(EXECUTOR_SERVICE));
    }

    @Test
    public void shouldCreateNewEmbedderWithMetaFilters() {
        // Given
        AbstractEmbedderMojo mojo = new AbstractEmbedderMojo() {
            @Override
            public void execute() {
            }
        };
        // When
        mojo.metaFilters = new String[] { "filter1", "filter2" };
        Embedder embedder = mojo.newEmbedder();
        // Then
        assertThat(embedder.metaFilters(), equalTo(asList("filter1", "filter2")));
    }

    @Test
    public void shouldCreateNewEmbedderIgnoringNullMetaFilters() {
        // Given
        AbstractEmbedderMojo mojo = new AbstractEmbedderMojo() {
            @Override
            public void execute() {
            }
        };
        // When
        mojo.metaFilters = new String[] { "filter1", null };
        Embedder embedder = mojo.newEmbedder();
        // Then
        assertThat(embedder.metaFilters(), equalTo(asList("filter1")));
    }

    @Test
    public void shouldCreateNewEmbedderWithMavenMonitor() {
        // Given
        Log log = mock(Log.class);
        AbstractEmbedderMojo mojo = new AbstractEmbedderMojo() {
            @Override
            public void execute() {
            }
        };
        mojo.setLog(log);
        // When
        Embedder embedder = mojo.newEmbedder();
        // Then
        EmbedderMonitor embedderMonitor = embedder.embedderMonitor();
        assertThat(embedderMonitor.toString(), containsString("MavenEmbedderMonitor"));

        // and verify monitor calls are propagated to Mojo Log
        BatchFailures failures = new BatchFailures();
        embedderMonitor.batchFailed(failures);
        verify(log).warn("Failed to run batch " + failures);

        String name = "name";
        Throwable cause = new RuntimeException();
        embedderMonitor.embeddableFailed(name, cause);
        verify(log).warn("Failed to run embeddable " + name, cause);

        List<String> classNames = asList("name1", "name2");
        embedderMonitor.embeddablesSkipped(classNames);
        verify(log).info("Skipped embeddables " + classNames);

        embedderMonitor.runningEmbeddable(name);
        verify(log).info("Running embeddable " + name);

        List<String> storyPaths = asList("/path1", "/path2");
        embedderMonitor.storiesSkipped(storyPaths);
        verify(log).info("Skipped stories " + storyPaths);

        String path = "/path";
        embedderMonitor.storyFailed(path, cause);
        verify(log).warn("Failed to run story " + path, cause);

        embedderMonitor.runningStory(path);
        verify(log).info("Running story " + path);

        Object annotatedInstance = new Object();
        Class<?> type = Object.class;
        embedderMonitor.annotatedInstanceNotOfType(annotatedInstance, type);
        verify(log).warn("Annotated instance " + annotatedInstance + " not of type " + type);

        File outputDirectory = new File("/dir");
        List<String> formats = asList(Format.CONSOLE.name(), Format.HTML.name());
        Properties viewProperties = new Properties();
        embedderMonitor.generatingReportsView(outputDirectory, formats, viewProperties);
        verify(log).info(
                "Generating reports view to '" + outputDirectory + "' using formats '" + formats + "'"
                        + " and view properties '" + viewProperties + "'");

        embedderMonitor.reportsViewGenerationFailed(outputDirectory, formats, viewProperties, cause);
        verify(log).warn(
                "Failed to generate reports view to '" + outputDirectory + "' using formats '" + formats + "'"
                        + " and view properties '" + viewProperties + "'", cause);

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
        verify(log).info(
                "Reports view generated with " + stories + " stories (of which " + storiesPending
                        + " pending) containing " + scenarios + " scenarios (of which " + scenariosPending
                        + " pending)");
        verify(log).info(
                "Meta filters excluded " + storiesNotAllowed + " stories and  " + scenariosNotAllowed
                        + " scenarios");
        embedderMonitor.reportsViewNotGenerated();
        verify(log).info("Reports view not generated");

    }

    @Test
    public void shouldCreateNewEmbedderWithSystemProperties() {
        // Given
        AbstractEmbedderMojo mojo = new AbstractEmbedderMojo() {
            @Override
            public void execute() {
            }
        };
        // When
        Properties systemProperties = new Properties();
        systemProperties.setProperty("one", "1");
        systemProperties.setProperty("two", "2");
        mojo.systemProperties = systemProperties;
        Embedder embedder = mojo.newEmbedder();
        // Then
        assertThat(embedder.systemProperties(), equalTo(systemProperties));
    }

    @Test
    public void shouldAllowTestScopedSearchDirectory() {
        // Given
        AbstractEmbedderMojo mojo = new AbstractEmbedderMojo() {
            @Override
            public void execute() {
            }
        };
        // When
        mojo.testSourceDirectory = "src/test";
        mojo.scope = "test";
        // Then
        assertThat(mojo.searchDirectory(), equalTo("src/test"));
    }

    @Test
    public void shouldAllowTestScopedClasspathElements() {
        // Given
        AbstractEmbedderMojo mojo = new AbstractEmbedderMojo() {
            @Override
            public void execute() {
            }
        };
        // When
        List<String> classpathElements = asList("target/test-classes");
        mojo.testClasspathElements = classpathElements;
        mojo.scope = "test";
        // Then
        assertThat(mojo.classpathElements(), equalTo(classpathElements));
    }

    @Test
    public void shouldAllowSpecificationOfEmbedderClass() {
        // Given
        AbstractEmbedderMojo mojo = new AbstractEmbedderMojo() {
            @Override
            public void execute() {
            }
        };
        // When
        mojo.embedderClass = MyEmbedder.class.getName();
        Embedder embedder = mojo.newEmbedder();
        // Then
        assertThat(embedder.getClass().getName(), equalTo(MyEmbedder.class.getName()));
    }

    public static class MyEmbedder extends Embedder {

    }

    @Test
    public void shouldAllowSpecificationOfInjectableEmbedderClass() {
        // Given
        AbstractEmbedderMojo mojo = new AbstractEmbedderMojo() {
            @Override
            public void execute() {
            }
        };
        // When
        mojo.injectableEmbedderClass = MyInjectableEmbedder.class.getName();
        Embedder embedder = mojo.newEmbedder();
        // Then
        assertThat(embedder.getClass().getName(), equalTo(MyEmbedder.class.getName()));
    }

    public static class MyInjectableEmbedder extends InjectableEmbedder {

        public MyInjectableEmbedder() {
            useEmbedder(new MyEmbedder());
        }

        @Override
        public void run() {
        }

    }

    @Test
    public void shouldAllowSpecificationOfStoryFinderClass() {
        // Given
        AbstractEmbedderMojo mojo = new AbstractEmbedderMojo() {
            @Override
            public void execute() {
            }
        };
        // When
        mojo.storyFinderClass = MyStoryFinder.class.getName();
        StoryFinder storyFinder = mojo.newStoryFinder();
        // Then
        assertThat(storyFinder.getClass().getName(), equalTo(MyStoryFinder.class.getName()));
    }

    public static class MyStoryFinder extends StoryFinder {

    }

    @Test
    public void shouldMapStoriesAsEmbeddables() throws MojoFailureException {
        // Given
        final EmbedderClassLoader classLoader = new EmbedderClassLoader(this.getClass().getClassLoader());
        MapStoriesAsEmbeddables mojo = new MapStoriesAsEmbeddables() {
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
        mojo.sourceDirectory = searchInDirectory;
        List<String> includes = asList("**/*StoryMaps.java");
        mojo.includes = includes;
        List<String> excludes = asList();
        mojo.excludes = excludes;
        List<String> classNames = new StoryFinder().findClassNames(searchInDirectory, includes, excludes);

        // When
        mojo.execute();

        // Then
        verify(embedder).runAsEmbeddables(classNames);
    }

    @Test
    public void shouldReportFailuresInMappingStoriesAsEmbeddables() throws MojoFailureException {
        // Given
        final EmbedderClassLoader classLoader = new EmbedderClassLoader(this.getClass().getClassLoader());
        MapStoriesAsEmbeddables mojo = new MapStoriesAsEmbeddables() {
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
        mojo.sourceDirectory = searchInDirectory;
        List<String> includes = asList("**/*StoryMaps.java");
        mojo.includes = includes;
        List<String> excludes = asList();
        mojo.excludes = excludes;
        List<String> classNames = new StoryFinder().findClassNames(searchInDirectory, includes, excludes);

        // When
        doThrow(new RuntimeException()).when(embedder).runAsEmbeddables(classNames);
        assertThrows(MojoFailureException.class, mojo::execute);
        // Then fail as expected
    }

    @Test
    public void shouldMapStoriesAsPaths() throws MojoFailureException {
        // Given
        final EmbedderClassLoader classLoader = new EmbedderClassLoader(this.getClass().getClassLoader());
        MapStoriesAsPaths mojo = new MapStoriesAsPaths() {
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
        mojo.sourceDirectory = searchInDirectory;
        mojo.outputDirectory = "target/test-classes";
        List<String> includes = asList("**/stories/*.story");
        mojo.includes = includes;
        List<String> excludes = asList();
        mojo.excludes = excludes;
        List<String> storyPaths = new StoryFinder().findPaths(searchInDirectory, includes, excludes);

        // When
        mojo.execute();

        // Then
        verify(embedder).mapStoriesAsPaths(storyPaths);
        assertThat(mojo.codeLocation().toString(), containsString(mojo.outputDirectory));
    }

    @Test
    public void shouldReportFailuresInMappingStoriesAsPaths() throws MojoFailureException {
        // Given
        final EmbedderClassLoader classLoader = new EmbedderClassLoader(this.getClass().getClassLoader());
        MapStoriesAsPaths mojo = new MapStoriesAsPaths() {
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
        mojo.sourceDirectory = searchInDirectory;
        mojo.outputDirectory = "target/test-classes";
        List<String> includes = asList("**/stories/*.story");
        mojo.includes = includes;
        List<String> excludes = asList();
        mojo.excludes = excludes;
        List<String> storyPaths = new StoryFinder().findPaths(searchInDirectory, includes, excludes);

        // When
        doThrow(new RuntimeException()).when(embedder).mapStoriesAsPaths(storyPaths);
        assertThrows(MojoFailureException.class, mojo::execute);
        // Then fail as expected
    }

    @Test
    public void shouldGenerateStoriesView() throws MojoFailureException {
        // Given
        GenerateStoriesView mojo = new GenerateStoriesView() {
            @Override
            protected Embedder newEmbedder() {
                return embedder;
            }

        };
        // When
        mojo.execute();

        // Then
        verify(embedder).generateReportsView();
    }

    @Test
    public void shouldReportStepdocs() throws MojoFailureException {
        // Given
        ReportStepdocs mojo = new ReportStepdocs() {
            @Override
            protected Embedder newEmbedder() {
                return embedder;
            }

        };
        // When
        mojo.execute();

        // Then
        verify(embedder).reportStepdocs();
    }

    @Test
    public void shouldReportFailuresWhenReportingStepdocs() throws MojoFailureException {
        // Given
        ReportStepdocs mojo = new ReportStepdocs() {
            @Override
            protected Embedder newEmbedder() {
                return embedder;
            }

        };

        // When
        doThrow(new RuntimeException()).when(embedder).reportStepdocs();
        assertThrows(MojoFailureException.class, mojo::execute);
        // Then fail as expected
    }

    @Test
    public void shouldRunStoriesAsEmbeddables() throws MojoFailureException {
        // Given
        final EmbedderClassLoader classLoader = new EmbedderClassLoader(this.getClass().getClassLoader());
        RunStoriesAsEmbeddables mojo = new RunStoriesAsEmbeddables() {
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
        mojo.sourceDirectory = searchInDirectory;
        List<String> includes = asList("**/stories/*.java");
        mojo.includes = includes;
        List<String> excludes = asList();
        mojo.excludes = excludes;
        List<String> classNames = new StoryFinder().findClassNames(searchInDirectory, includes, excludes);

        // When
        mojo.execute();

        // Then
        verify(embedder).runAsEmbeddables(classNames);
    }

    @Test
    public void shouldReportFailuresInRunningStoriesAsEmbeddables() throws MojoFailureException {
        // Given
        final EmbedderClassLoader classLoader = new EmbedderClassLoader(this.getClass().getClassLoader());
        RunStoriesAsEmbeddables mojo = new RunStoriesAsEmbeddables() {
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
        mojo.sourceDirectory = searchInDirectory;
        List<String> includes = asList("**/stories/*.java");
        mojo.includes = includes;
        List<String> excludes = asList();
        mojo.excludes = excludes;
        List<String> classNames = new StoryFinder().findClassNames(searchInDirectory, includes, excludes);

        // When
        doThrow(new RuntimeException()).when(embedder).runAsEmbeddables(classNames);
        assertThrows(MojoFailureException.class, mojo::execute);
        // Then fail as expected
    }

    @Test
    public void shouldRunStoriesAsPaths() throws MojoFailureException {
        // Given
        final EmbedderClassLoader classLoader = new EmbedderClassLoader(this.getClass().getClassLoader());
        RunStoriesAsPaths mojo = new RunStoriesAsPaths() {
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
        mojo.sourceDirectory = searchInDirectory;
        List<String> includes = asList("**/stories/*.story");
        mojo.includes = includes;
        List<String> excludes = asList();
        mojo.excludes = excludes;
        List<String> storyPaths = new StoryFinder().findPaths(searchInDirectory, includes, excludes);

        // When
        mojo.execute();

        // Then
        verify(embedder).runStoriesAsPaths(storyPaths);
    }

    @Test
    public void shouldReportFailuresInRunningStoriesAsPaths() throws MojoFailureException {
        // Given
        final EmbedderClassLoader classLoader = new EmbedderClassLoader(this.getClass().getClassLoader());
        RunStoriesAsPaths mojo = new RunStoriesAsPaths() {
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
        mojo.sourceDirectory = searchInDirectory;
        List<String> includes = asList("**/stories/*.story");
        mojo.includes = includes;
        List<String> excludes = asList();
        mojo.excludes = excludes;
        List<String> storyPaths = new StoryFinder().findPaths(searchInDirectory, includes, excludes);

        // When
        doThrow(new RuntimeException()).when(embedder).runStoriesAsPaths(storyPaths);
        assertThrows(MojoFailureException.class, mojo::execute);
        // Then fail as expected
    }

    @Test
    public void shouldRunStoriesWithAnnotatedEmbedderRunner() throws MojoFailureException {
        // Given
        final EmbedderClassLoader classLoader = new EmbedderClassLoader(this.getClass().getClassLoader());
        RunStoriesWithAnnotatedEmbedderRunner mojo = new RunStoriesWithAnnotatedEmbedderRunner() {
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
        mojo.sourceDirectory = searchInDirectory;
        List<String> includes = asList("**/stories/*.java");
        mojo.includes = includes;
        List<String> excludes = asList();
        mojo.excludes = excludes;
        List<String> classNames = new StoryFinder().findClassNames(searchInDirectory, includes, excludes);

        // When
        mojo.execute();

        // Then
        verify(embedder).runStoriesWithAnnotatedEmbedderRunner(classNames);
    }

    @Test
    @Disabled("The assertThrows does not seem to work")
    public void shouldReportFailuresInRunningStoriesWithAnnotatedEmbedderRunner() throws MojoFailureException {
        // Given
        final EmbedderClassLoader classLoader = new EmbedderClassLoader(this.getClass().getClassLoader());
        RunStoriesWithAnnotatedEmbedderRunner mojo = new RunStoriesWithAnnotatedEmbedderRunner() {
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
        mojo.sourceDirectory = searchInDirectory;
        List<String> includes = asList("**/stories/*.java");
        mojo.includes = includes;
        List<String> excludes = asList();
        mojo.excludes = excludes;
        List<String> classNames = new StoryFinder().findClassNames(searchInDirectory, includes, excludes);

        // When
        doThrow(new RuntimeException()).when(embedder).runStoriesWithAnnotatedEmbedderRunner(classNames);
        mojo.execute();

        assertThrows(MojoFailureException.class, mojo::execute);
        // Then fail as expected
    }

    @Test
    @Disabled("Cannot mock MavenProject")
    public void shouldUnpackViewResources() throws MojoExecutionException, NoSuchArchiverException, ArchiverException {
        // Given
        UnpackViewResources mojo = new UnpackViewResources() {
            @Override
            protected Embedder newEmbedder() {
                return new Embedder();
            }

        };
        ArchiverManager archiveManager = mock(ArchiverManager.class);
        MavenProject project = mock(MavenProject.class);

        File coreFile = new File("core");
        Artifact coreResources = mock(Artifact.class);
        when(coreResources.getArtifactId()).thenReturn("jbehave-core");
        when(coreResources.getType()).thenReturn("zip");
        when(coreResources.getFile()).thenReturn(coreFile);
        File siteFile = new File("site");
        Artifact siteResources = mock(Artifact.class);
        when(siteResources.getArtifactId()).thenReturn("jbehave-site-resources");
        when(siteResources.getType()).thenReturn("zip");
        when(siteResources.getFile()).thenReturn(siteFile);

        Set<Artifact> allArtifacts = new HashSet<>();
        allArtifacts.add(coreResources);
        allArtifacts.add(siteResources);

        String buildDirectory = "target";
        Build build = new Build();
        build.setDirectory(buildDirectory);

        UnArchiver coreArchiver = mock(UnArchiver.class);
        UnArchiver siteArchiver = mock(UnArchiver.class);

        // When
        mojo.project = project;
        mojo.archiverManager = archiveManager;
        mojo.resourceIncludes = "ftl/*";
        mojo.resourcesExcludes = "com/*";
        when(project.getArtifacts()).thenReturn(allArtifacts);
        when(project.getBuild()).thenReturn(build);
        when(archiveManager.getUnArchiver(coreFile)).thenReturn(coreArchiver);
        when(archiveManager.getUnArchiver(siteFile)).thenReturn(siteArchiver);

        unpackTo(mojo, null); // default view directory
        unpackTo(mojo, new File(System.getProperty("java.io.tmpdir")+"/jbehave/view"));

        // Then
        verify(coreArchiver, times(2)).extract();
        verify(siteArchiver, times(2)).extract();
    }

    private void unpackTo(UnpackViewResources mojo, File file) throws MojoExecutionException {
        mojo.viewDirectory = file;
        mojo.execute();
    }

    @Test
    @Disabled("Cannot mock MavenProject")
    public void shouldNotUnpackViewResourcesThatDoNotMatchTheFilters() throws MojoExecutionException,
            NoSuchArchiverException {
        // Given
        UnpackViewResources mojo = new UnpackViewResources() {
            @Override
            protected Embedder newEmbedder() {
                return new Embedder();
            }

        };
        ArchiverManager archiveManager = mock(ArchiverManager.class);
        MavenProject project = mock(MavenProject.class);

        File resourcesFile = new File("some");
        Artifact someResources = mock(Artifact.class);
        when(someResources.getArtifactId()).thenReturn("some-resources");
        when(someResources.getType()).thenReturn("jar");
        when(someResources.getFile()).thenReturn(resourcesFile);

        Set<Artifact> allArtifacts = new HashSet<>();
        allArtifacts.add(someResources);

        String buildDirectory = "target";
        Build build = new Build();
        build.setDirectory(buildDirectory);

        // When
        mojo.project = project;
        mojo.archiverManager = archiveManager;
        mojo.resourceIncludes = "ftl/*";
        mojo.resourcesExcludes = "com/*";
        when(project.getArtifacts()).thenReturn(allArtifacts);
        when(project.getBuild()).thenReturn(build);

        mojo.execute();

        // Then
        verify(archiveManager, Mockito.never()).getUnArchiver(resourcesFile);
    }

    @Test
    @Disabled("Cannot mock MavenProject")
    public void shouldNotIgnoreFailureInUnpackingViewResources() throws MojoExecutionException, NoSuchArchiverException, ArchiverException {
        // Given
        UnpackViewResources mojo = new UnpackViewResources() {
            @Override
            protected Embedder newEmbedder() {
                return new Embedder();
            }

        };
        ArchiverManager archiveManager = mock(ArchiverManager.class);
        MavenProject project = mock(MavenProject.class);

        File coreFile = new File("core");
        Artifact coreResources = mock(Artifact.class);
        when(coreResources.getArtifactId()).thenReturn("jbehave-core");
        when(coreResources.getType()).thenReturn("zip");
        when(coreResources.getFile()).thenReturn(coreFile);
        File siteFile = new File("site");
        Artifact siteResources = mock(Artifact.class);
        when(siteResources.getArtifactId()).thenReturn("jbehave-site-resources");
        when(siteResources.getType()).thenReturn("zip");
        when(siteResources.getFile()).thenReturn(siteFile);

        Set<Artifact> allArtifacts = new HashSet<>();
        allArtifacts.add(coreResources);
        allArtifacts.add(siteResources);

        String buildDirectory = "target";
        Build build = new Build();
        build.setDirectory(buildDirectory);

        UnArchiver coreArchiver = mock(UnArchiver.class);
        UnArchiver siteArchiver = mock(UnArchiver.class);

        // When
        mojo.project = project;
        mojo.archiverManager = archiveManager;
        when(project.getArtifacts()).thenReturn(allArtifacts);
        when(project.getBuild()).thenReturn(build);
        when(archiveManager.getUnArchiver(coreFile)).thenReturn(coreArchiver);
        when(archiveManager.getUnArchiver(siteFile)).thenReturn(siteArchiver);
        Mockito.doThrow(new ArchiverException("bum")).when(siteArchiver).extract();

        assertThrows(MojoFailureException.class, mojo::execute);
        // Then
        verify(coreArchiver).extract();
        // and fail as expected ...
    }

    public static class MyExecutors implements ExecutorServiceFactory {

        @Override
        public ExecutorService create(EmbedderControls controls) {
            return EXECUTOR_SERVICE;
        }
        
    }
}
