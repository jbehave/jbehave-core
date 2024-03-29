package org.jbehave.mojo;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.components.io.fileselectors.IncludeExcludeFileSelector;
import org.codehaus.plexus.util.StringUtils;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.reporters.StoryReporterBuilder;

/**
 * Mojo to unpack resources to view directory, whose location is derived from
 * the configured {@link StoryReporterBuilder} accessible from the {@link Embedder}.
 */
@Mojo(name = "unpack-view-resources", defaultPhase = LifecyclePhase.PROCESS_RESOURCES,
        requiresDependencyResolution = ResolutionScope.TEST)
public class UnpackViewResources extends AbstractEmbedderMojo {

    @Component
    ArchiverManager archiverManager;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    MavenProject project;

    @Parameter
    String[] resourceArtifactIds = new String[] { "jbehave-site-resources", "jbehave-core" };

    @Parameter
    String[] resourceTypes = new String[] { "zip" };

    @Parameter
    String resourceIncludes;

    @Parameter
    String resourcesExcludes;

    @Parameter
    File viewDirectory;

    @Override
    public void execute() throws MojoExecutionException {
        File destination = viewDirectory();
        for (Artifact artifact : resourceArtifacts()) {
            unpack(artifact.getFile(), destination, resourceIncludes, resourcesExcludes);
        }
    }

    private File viewDirectory() {
        if (viewDirectory != null) {
            return viewDirectory;
        }
        StoryReporterBuilder storyReporterBuilder = newEmbedder().configuration().storyReporterBuilder();
        String build = project.getBuild().getDirectory();
        String output = storyReporterBuilder.outputDirectory().getName();
        String view = storyReporterBuilder.viewResources().getProperty("viewDirectory");
        return new File(build + "/" + output + "/" + view);
    }

    private Set<Artifact> resourceArtifacts() {
        return allArtifacts().stream()
                .filter(artifact -> allowedBy("artifactId", artifact.getArtifactId(), resourceArtifactIds)
                        && allowedBy("type", artifact.getType(), resourceTypes))
                .collect(Collectors.toSet());
    }

    private boolean allowedBy(String name, String property, String[] values) {
        boolean allowed = false;
        if (values.length > 0) {
            for (String value : values) {
                if (property.equals(value)) {
                    allowed = true;
                    break;
                }
            }
        } else {
            allowed = true;
        }
        if (!allowed) {
            getLog().debug("Artifact property " + name + " not allowed by values " + Arrays.asList(values));
        }
        return allowed;
    }

    @SuppressWarnings("unchecked")
    private Set<Artifact> allArtifacts() {
        return new HashSet<Artifact>(project.getArtifacts());
    }

    private void unpack(File file, File destination, String includes, String excludes) throws MojoExecutionException {
        try {
            destination.mkdirs();

            UnArchiver unArchiver = archiverManager.getUnArchiver(file);
            unArchiver.setSourceFile(file);
            unArchiver.setDestDirectory(destination);

            if (StringUtils.isNotEmpty(excludes) || StringUtils.isNotEmpty(includes)) {
                IncludeExcludeFileSelector[] selectors = new IncludeExcludeFileSelector[] {
                    new IncludeExcludeFileSelector() };
                if (StringUtils.isNotEmpty(excludes)) {
                    selectors[0].setExcludes(excludes.split(","));
                }
                if (StringUtils.isNotEmpty(includes)) {
                    selectors[0].setIncludes(includes.split(","));
                }
                unArchiver.setFileSelectors(selectors);
            }

            unArchiver.extract();

            getLog().info("Unpacked " + file + " to " + destination);
        } catch (Exception e) {
            throw new MojoExecutionException("Failed unpacking " + file + " to " + destination, e);
        }
    }

}
