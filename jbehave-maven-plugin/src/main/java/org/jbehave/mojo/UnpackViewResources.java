package org.jbehave.mojo;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
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
 * @goal unpack-view-resources
 * @phase process-resources
 * @requiresDependencyResolution test
 */
public class UnpackViewResources extends AbstractEmbedderMojo {

    /**
     * @parameter expression="${project}"
     * @readonly
     * @required
     */
    MavenProject project;

    /**
     * @component
     */
    ArchiverManager archiverManager;

    /**
     * @parameter
     */
    String[] resourceArtifactIds = new String[] { "jbehave-site-resources", "jbehave-core" };

    /**
     * @parameter
     */
    String[] resourceTypes = new String[] { "zip" };

    /**
     * @parameter
     */
    String resourceIncludes;

    /**
     * @parameter
     */
    String resourcesExcludes;

    /**
     * @parameter
     */
    File viewDirectory;

    @Override
    public void execute() throws MojoExecutionException {
        File destination = viewDirectory();
        for (Artifact artifact : resourceArtifacts()) {
            unpack(artifact.getFile(), destination, resourceIncludes, resourcesExcludes);
        }
    }

    private File viewDirectory() {
        if ( viewDirectory != null ){
            return viewDirectory;
        }
        StoryReporterBuilder storyReporterBuilder = newEmbedder().configuration().storyReporterBuilder();
        String build = project.getBuild().getDirectory();
        String output = storyReporterBuilder.outputDirectory().getName();
        String view = storyReporterBuilder.viewResources().getProperty("viewDirectory");
        return new File(build + "/" + output + "/" + view);
    }

    private Set<Artifact> resourceArtifacts() {
        Set<Artifact> artifacts = allArtifacts();
        CollectionUtils.filter(artifacts, new Predicate<Artifact>() {
            @Override
            public boolean evaluate(Artifact artifact) {
                return allowedBy("artifactId", artifact.getArtifactId(), resourceArtifactIds)
                        && allowedBy("type", artifact.getType(), resourceTypes);
            }
        });
        return artifacts;
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
            throw new MojoExecutionException("Failed unpacking " + file + " to " + destination, e);
        }
    }

}
