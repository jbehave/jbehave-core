package org.jbehave.scenario.reporters;

import org.jbehave.scenario.RunnableScenario;
import org.jbehave.scenario.parser.ScenarioNameResolver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

/**
 * Creates {@link PrintStream} instances that write to a file. It also provides
 * useful defaults for the file directory and the extension.
 */
public class FilePrintStreamFactory implements PrintStreamFactory {

    private PrintStream printStream;
    private Class<? extends RunnableScenario> scenarioClass;
    private ScenarioNameResolver scenarioNameResolver;
    private FileConfiguration configuration;
    private File outputFile;

    public FilePrintStreamFactory(Class<? extends RunnableScenario> scenarioClass,
            ScenarioNameResolver scenarioNameResolver) {
        this(scenarioClass, scenarioNameResolver, new FileConfiguration());
    }

    public FilePrintStreamFactory(Class<? extends RunnableScenario> scenarioClass,
            ScenarioNameResolver scenarioNameResolver, FileConfiguration configuration) {
        this.scenarioClass = scenarioClass;
        this.scenarioNameResolver = scenarioNameResolver;
        this.configuration = configuration;
        this.outputFile = outputFile(scenarioClass, scenarioNameResolver, this.configuration);
    }

    public FilePrintStreamFactory(File outputFile) {        
        this.outputFile = outputFile;
    }

    public PrintStream getPrintStream() {
        try {
            outputFile.getParentFile().mkdirs();
            printStream = new PrintStream(new FileOutputStream(outputFile, true));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        return printStream;
    }

    public File getOutputFile() {
        return outputFile;
    }

    public void useConfiguration(FileConfiguration configuration) {
        this.configuration = configuration;
        this.outputFile = outputFile(scenarioClass, scenarioNameResolver, configuration);        
    }

    protected File outputFile(Class<? extends RunnableScenario> scenarioClass, ScenarioNameResolver scenarioNameResolver,
            FileConfiguration configuration) {
        File outputDirectory = outputDirectory(scenarioClass, configuration);
        String fileName = fileName(scenarioClass, scenarioNameResolver, configuration);
        return new File(outputDirectory, fileName);
    }

    protected File outputDirectory(Class<? extends RunnableScenario> scenarioClass, FileConfiguration configuration) {
        if ( configuration.isOutputDirectoryAbsolute() ){
            return new File(configuration.getOutputDirectory());
        }        
        return buildOutputDirectoryFromCodeLocation(scenarioClass, configuration);
    }

    private File buildOutputDirectoryFromCodeLocation(Class<? extends RunnableScenario> scenarioClass, FileConfiguration configuration) {
        File targetDirectory = new File(scenarioClass.getProtectionDomain().getCodeSource().getLocation().getFile()).getParentFile();
        return new File(targetDirectory, configuration.getOutputDirectory());
    }

    protected String fileName(Class<? extends RunnableScenario> scenarioClass, ScenarioNameResolver scenarioNameResolver,
            FileConfiguration configuration) {
        String scenarioName = scenarioNameResolver.resolve(scenarioClass).replace('/', '.');
        String name = scenarioName.substring(0, scenarioName.lastIndexOf("."));
        return name + "." + configuration.getExtension();
    }

    /**
     * Configuration class for file print streams. Allows specification the 
     * output directory (either absolute or relative to the code location) and
     * the file extension. Provides as defaults {@link #OUTPUT_DIRECTORY} (relative
     * to class code location) and {@link #HTML}. 
     */
    public static class FileConfiguration {
        public static final String OUTPUT_DIRECTORY = "jbehave-reports";
        public static final String HTML = "html";

        private final String outputDirectory;
        private final String extension;
        private final boolean outputAbsolute;
        
        public FileConfiguration() {
            this(HTML);
        }

        public FileConfiguration(String extension) {
            this(OUTPUT_DIRECTORY, false, extension);
        }

        public FileConfiguration(String outputDirectory, boolean outputAbsolute, String extension) {
            this.outputDirectory = outputDirectory;
            this.outputAbsolute = outputAbsolute;
            this.extension = extension;
        }

        public String getOutputDirectory() {
            return outputDirectory;
        }

        public String getExtension() {
            return extension;
        }

        public boolean isOutputDirectoryAbsolute() {
            return outputAbsolute;
        }
        
    }

}
