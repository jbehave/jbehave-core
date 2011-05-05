package org.jbehave.core.reporters;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.jbehave.core.io.StoryLocation;

/**
 * Creates {@link PrintStream} instances that write to a file identified by the
 * {@link StoryLocation}. {@link FileConfiguration} specifies directory and the
 * extension, providing useful default values.
 */
public class FilePrintStreamFactory implements PrintStreamFactory {

    private final StoryLocation storyLocation;
    private FileConfiguration configuration;
    private File outputFile;

    public FilePrintStreamFactory(StoryLocation storyLocation) {
        this(storyLocation, new FileConfiguration());
    }

    public FilePrintStreamFactory(StoryLocation storyLocation, FileConfiguration configuration) {
        this.storyLocation = storyLocation;
        this.configuration = configuration;
    }

    public PrintStream createPrintStream() {
        try {
            outputFile = outputFile();
            outputFile.getParentFile().mkdirs();
            return new FilePrintStream(outputFile, false);
        } catch (Exception e) {
            throw new PrintStreamCreationFailed(outputFile, e);
        }
    }

    public File getOutputFile() {
        return outputFile;
    }

    public void useConfiguration(FileConfiguration configuration) {
        this.configuration = configuration;
        this.outputFile = outputFile();
    }

    public FileConfiguration configuration() {
        return configuration;
    }

    protected File outputFile() {
        return new File(outputDirectory(), outputName());
    }

    /**
     * Return the file output directory, using the configured
     * {@link FilePathResolver}
     * 
     * @return The File representing the output directory
     */
    protected File outputDirectory() {
        return new File(configuration.getPathResolver().resolveDirectory(storyLocation,
                configuration.getRelativeDirectory()));
    }

    /**
     * Return the file output name, using the configured
     * {@link FilePathResolver}
     * 
     * @return The file output name
     */
    protected String outputName() {
        return configuration.getPathResolver().resolveName(storyLocation, configuration.getExtension());
    }

    public static interface FilePathResolver {

        String resolveDirectory(StoryLocation storyLocation, String relativeDirectory);

        String resolveName(StoryLocation storyLocation, String extension);

    }

    /**
     * Resolves directory from code location parent file.
     */
    public static abstract class AbstractPathResolver implements FilePathResolver {

        public String resolveDirectory(StoryLocation storyLocation, String relativeDirectory) {
            File parent = new File(storyLocation.getCodeLocation().getFile()).getParentFile();
            return parent.getPath().replace('\\', '/') + "/" + relativeDirectory;
        }

    }

    /**
     * Resolves story location path to java packaged name, replacing '/' with '.'
     */
    public static class ResolveToPackagedName extends AbstractPathResolver {

        public String resolveName(StoryLocation storyLocation, String extension) {
            String name = storyLocation.getPath().replace('/', '.');
            if (name.startsWith(".")) {
                name = name.substring(1);
            }
            return StringUtils.substringBeforeLast(name, ".") + "." + extension;
        }

    }

    /**
     * Resolves story location path to simple name, considering portion after last '/'.
     */
    public static class ResolveToSimpleName extends AbstractPathResolver {

        public String resolveName(StoryLocation storyLocation, String extension) {
            String name = storyLocation.getPath();
            if ( StringUtils.contains(name, '/') ){
                name = StringUtils.substringAfterLast(name, "/");
            }
            return StringUtils.substringBeforeLast(name, ".") + "." + extension;
        }

    }

    public static class FilePrintStream extends PrintStream {

        private final File outputFile;
        private final boolean append;

        public FilePrintStream(File outputFile, boolean append) throws FileNotFoundException {
            super(new FileOutputStream(outputFile, append));
            this.outputFile = outputFile;
            this.append = append;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append(outputFile).append(append)
                    .toString();
        }

    }

    /**
     * Configuration class for file print streams. Allows specification the
     * relative directory (relative to code location) and file extension.
     * Provides as defaults {@link #RELATIVE_DIRECTORY} and {@link #EXTENSION}.
     */
    public static class FileConfiguration {
        public static final String RELATIVE_DIRECTORY = "jbehave";
        public static final String EXTENSION = "html";

        private final String relativeDirectory;
        private final String extension;
        private final FilePathResolver pathResolver;

        public FileConfiguration() {
            this(EXTENSION);
        }

        public FileConfiguration(String extension) {
            this(RELATIVE_DIRECTORY, extension, new ResolveToPackagedName());
        }

        public FileConfiguration(String relativeDirectory, String extension, FilePathResolver pathResolver) {
            this.relativeDirectory = relativeDirectory;
            this.extension = extension;
            this.pathResolver = pathResolver;
        }

        public String getRelativeDirectory() {
            return relativeDirectory;
        }

        public String getExtension() {
            return extension;
        }

        public FilePathResolver getPathResolver() {
            return pathResolver;
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }

    }

    @SuppressWarnings("serial")
    public class PrintStreamCreationFailed extends RuntimeException {
        public PrintStreamCreationFailed(File file, Exception cause) {
            super("Failed to create print stream for file " + file, cause);
        }
    }
}
