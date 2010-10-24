package org.jbehave.mojo;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.components.io.fileselectors.IncludeExcludeFileSelector;
import org.codehaus.plexus.util.StringUtils;
import org.jbehave.core.reporters.StoryReporterBuilder;

/**
 * Mojo to unpack resources to view directory, whose location is derived from
 * the configured StoryReporterBuilder accessible from the Embedder.
 * 
 * @goal unpack-resources
 * @phase process-resources
 */
public class UnpackResources extends AbstractEmbedderMojo {

    /**
     * @parameter expression="${project}"
     * @readonly
     * @required
     */
    private MavenProject project;

    /**
     * @component
     */
    private ArchiverManager archiverManager;

    /**
     * @parameter
     */
    private String[] resourceTypes = new String[] { "zip" };

    /**
     * @parameter
     */
    private String resourceIncludes;

    /**
     * @parameter
     */
    private String resourcesExcludes;

    public void execute() throws MojoExecutionException {
        File destination = viewDirectory();
        for (Artifact artifact : resourceArtifacts()) {
            unpack(artifact.getFile(), destination, resourceIncludes, resourcesExcludes);
        }
    }

    private File viewDirectory() {
        StoryReporterBuilder storyReporterBuilder = newEmbedder().configuration().storyReporterBuilder();
        return new File(project.getBuild().getDirectory() + "/" + storyReporterBuilder.outputDirectory().getName()
                + "/" + storyReporterBuilder.viewResources().getProperty("viewDirectory"));
    }

    private Set<Artifact> resourceArtifacts() {
        Set<Artifact> artifacts = new HashSet<Artifact>();
        for (Artifact artifact : allArtifacts()) {
            if (isAllowed(artifact)) {
                artifacts.add(artifact);
            }
        }
        return artifacts;
    }

    private boolean isAllowed(Artifact artifact) {
        for (String type : resourceTypes) {
            if (type.equals(artifact.getType())) {
                return true;
            }
        }
        getLog().debug("Artifact " + artifact + " not allowed for resource types " + Arrays.asList(resourceTypes));
        return false;
    }

    @SuppressWarnings("unchecked")
    private Set<Artifact> allArtifacts() {
        return project.getArtifacts();
    }

    private void unpack(File file, File destination, String includes, String excludes) throws MojoExecutionException {
        try {
            destination.mkdirs();

            UnArchiver unArchiver = archiverManager.getUnArchiver(file);
            unArchiver.setSourceFile(file);
            unArchiver.setDestDirectory(destination);

            if (StringUtils.isNotEmpty(excludes) || StringUtils.isNotEmpty(includes)) {
                IncludeExcludeFileSelector[] selectors = new IncludeExcludeFileSelector[] { new IncludeExcludeFileSelector() };
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
            throw new MojoExecutionException("Failed unpacking file: " + file + " to " + destination, e);
        }
    }

}
