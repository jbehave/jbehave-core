package org.jbehave.mojo;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

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
import org.jbehave.core.failures.BatchFailures;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.reporters.Format;
import org.jbehave.core.reporters.ReportsCount;
import org.junit.Test;
import org.mockito.Mockito;

import static java.util.Arrays.asList;

import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EmbedderMojoBehaviour {

    private Embedder embedder = mock(Embedder.class);

    @Test
    public void shouldCreateNewEmbedderWithDefaultControls() {
        // Given
        AbstractEmbedderMojo mojo = new AbstractEmbedderMojo() {
            public void execute() throws MojoExecutionException, MojoFailureException {
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
        assertThat(embedderControls.storyTimeoutInSecs(), equalTo(300L));
        assertThat(embedderControls.threads(), equalTo(1));
    }

    @Test
    public void shouldCreateNewEmbedderWithGivenControls() {
        // Given
        AbstractEmbedderMojo mojo = new AbstractEmbedderMojo() {
            public void execute() throws MojoExecutionException, MojoFailureException {
            }
        };
        // When
        mojo.batch = true;
        mojo.generateViewAfterStories = false;
        mojo.ignoreFailureInStories = true;
        mojo.ignoreFailureInView = true;
        mojo.storyTimeoutInSecs = 60;
        mojo.threads = 2;
        mojo.skip = true;
        Embedder embedder = mojo.newEmbedder();
        // Then
        EmbedderControls embedderControls = embedder.embedderControls();
        assertThat(embedderControls.batch(), is(true));
        assertThat(embedderControls.generateViewAfterStories(), is(false));
        assertThat(embedderControls.ignoreFailureInStories(), is(true));
        assertThat(embedderControls.ignoreFailureInView(), is(true));
        assertThat(embedderControls.storyTimeoutInSecs(), is(60L));
        assertThat(embedderControls.threads(), is(2));
        assertThat(embedderControls.skip(), is(true));
    }

    @Test
    public void shouldCreateNewEmbedderWithMetaFilters() {
        // Given
        AbstractEmbedderMojo mojo = new AbstractEmbedderMojo() {
            public void execute() throws MojoExecutionException, MojoFailureException {
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
            public void execute() throws MojoExecutionException, MojoFailureException {
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
            public void execute() throws MojoExecutionException, MojoFailureException {
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
                        + " pending) containing " + scenarios + " scenarios (of which " + scenariosFailed
                        + " failed and " + scenariosPending + " pending)");
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
            public void execute() throws MojoExecutionException, MojoFailureException {
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
            public void execute() throws MojoExecutionException, MojoFailureException {
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
            public void execute() throws MojoExecutionException, MojoFailureException {
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
            public void execute() throws MojoExecutionException, MojoFailureException {
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
            public void execute() throws MojoExecutionException, MojoFailureException {
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

        public void run() throws Throwable {
        }

    }

    @Test
    public void shouldAllowSpecificationOfStoryFinderClass() {
        // Given
        AbstractEmbedderMojo mojo = new AbstractEmbedderMojo() {
            public void execute() throws MojoExecutionException, MojoFailureException {
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
    public void shouldMapStoriesAsEmbeddables() throws MojoExecutionException, MojoFailureException {
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

    @Test(expected = MojoFailureException.class)
    public void shouldReportFailuresInMappingStoriesAsEmbeddables() throws MojoExecutionException, MojoFailureException {
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
        mojo.execute();

        // Then fail as expected
    }

    @Test
    public void shouldMapStoriesAsPaths() throws MojoExecutionException, MojoFailureException {
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

    @Test(expected=MojoFailureException.class)
    public void shouldReportFailuresInMappingStoriesAsPaths() throws MojoExecutionException, MojoFailureException {
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
        mojo.execute();

        // Then fail as expected
    }

    @Test
    public void shouldGenerateStoriesView() throws MojoExecutionException, MojoFailureException {
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
    public void shouldReportStepdocs() throws MojoExecutionException, MojoFailureException {
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

    @Test(expected = MojoFailureException.class)
    public void shouldReportFailuresWhenReportingStepdocs() throws MojoExecutionException, MojoFailureException {
        // Given
        ReportStepdocs mojo = new ReportStepdocs() {
            @Override
            protected Embedder newEmbedder() {
                return embedder;
            }

        };

        // When
        doThrow(new RuntimeException()).when(embedder).reportStepdocs();
        mojo.execute();

        // Then fail as expected
    }

    @Test
    public void shouldRunStoriesAsEmbeddables() throws MojoExecutionException, MojoFailureException {
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

    @Test(expected = MojoFailureException.class)
    public void shouldReportFailuresInRunningStoriesAsEmbeddables() throws MojoExecutionException, MojoFailureException {
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
        mojo.execute();

        // Then fail as expected

    }

    @Test
    public void shouldRunStoriesAsPaths() throws MojoExecutionException, MojoFailureException {
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

    @Test(expected = MojoFailureException.class)
    public void shouldReportFailuresInRunningStoriesAsPaths() throws MojoExecutionException, MojoFailureException {
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
        mojo.execute();

        // Then fail as expected

    }

    @Test
    public void shouldRunStoriesWithAnnotatedEmbedderRunner() throws MojoExecutionException, MojoFailureException {
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

    @Test(expected = MojoFailureException.class)
    public void shouldReportFailuresInRunningStoriesWithAnnotatedEmbedderRunner() throws MojoExecutionException,
            MojoFailureException {
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

        // Then fail as expected
        mojo.execute();

    }

    @Test
    public void shouldUnpackViewResources() throws MojoExecutionException, MojoFailureException,
            NoSuchArchiverException, ArchiverException {
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

        Set<Artifact> allArtifacts = new HashSet<Artifact>();
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

        mojo.execute();

        // Then
        verify(coreArchiver).extract();
        verify(siteArchiver).extract();
    }

    @Test
    public void shouldNotUnpackViewResourcesThatDoNotMatchTheFilters() throws MojoExecutionException,
            MojoFailureException, NoSuchArchiverException, ArchiverException {
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

        Set<Artifact> allArtifacts = new HashSet<Artifact>();
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

    @Test(expected = MojoExecutionException.class)
    public void shouldNotIgnoreFailureInUnpackingViewResources() throws MojoExecutionException, MojoFailureException,
            NoSuchArchiverException, ArchiverException {
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

        Set<Artifact> allArtifacts = new HashSet<Artifact>();
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

        mojo.execute();

        // Then
        verify(coreArchiver).extract();
        // and fail as expected ...
    }

}
