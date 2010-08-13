package org.jbehave.core.reporters;

import static java.util.Arrays.asList;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.io.StoryLocation;
import org.jbehave.core.reporters.FilePrintStreamFactory.FileConfiguration;
import org.jbehave.core.reporters.FilePrintStreamFactory.FilePathResolver;

/**
 * <p>
 * A <a href="http://en.wikipedia.org/wiki/Builder_pattern">Builder</a> for
 * {@link StoryReporter}s. It builds a {@link DelegatingStoryReporter} with
 * delegates for a number of formats - mostly file-based ones except
 * {@Format.CONSOLE}. It requires a
 * {@link FilePrintStreamFactory} and provides default delegate instances for
 * each format.
 * </p>
 * <p>
 * To build a reporter for a single story path with default and given formats:
 * 
 * <pre>
 * Class&lt;MyStory&gt; storyClass = MyStory.class;
 * StoryPathResolver resolver = new UnderscoredCamelCaseResolver();
 * String storyPath = resolver.resolve(storyClass);
 * StoryReporter reporter = new StoryReporterBuilder().withCodeLocation(StoryLocation.codeLocationFromClass(storyClass))
 *         .withDefaultFormats().withFormats(TXT, HTML, XML).build(storyPath);
 * </pre>
 * 
 * </p>
 * <p>
 * The builder is configured to build with the {@link Format#STATS} as default
 * format. To change the default formats the user can override the method:
 * 
 * <pre>
 * new StoryReporterBuilder() {
 *     protected StoryReporterBuilder withDefaultFormats() {
 *         return withFormats(STATS);
 *     }
 * }
 * </pre>
 * 
 * </p>
 * <p>
 * The builder configures the file-based reporters to output to the default file
 * directory {@link FileConfiguration#DIRECTORY} as relative to the code
 * location. In some case, e.g. with Ant class loader, the code source location
 * from class may not be properly set. In this case, we may specify it from a
 * file:
 * 
 * <pre>
 * new StoryReporterBuilder().withCodeLocation(StoryLocation.codeLocationFromFile(new File(&quot;target/classes&quot;)))
 *         .withDefaultFormats().withFormats(TXT, HTML, XML).build(storyPath);
 * </pre>
 * 
 * </p>
 * <p>
 * By default, the reporters will output minimal failure information, the single
 * line describing the failure cause and the outcomes if failures occur. To
 * configure the failure trace to be reported as well:
 * 
 * <pre>
 * new StoryReporterBuilder().withFailureTrace(true)
 * </pre>
 * 
 * </p>
 * 
 * <p>
 * To specify the use of keywords for a given locale:
 * 
 * <pre>
 * new StoryReporterBuilder().withKeywords(new LocalisedKeywords(Locale.IT)
 * </pre>
 * 
 * </p>
 * 
 * <p>
 * The builder provides default instances for all reporters, using the default
 * output patterns. To change the reporter for a specific instance, e.g. to
 * report format <b>TXT</b> to <b>.text</b> files and to inject other
 * non-default parameters, such as the custom output patterns:
 * 
 * <pre>
 * new StoryReporterBuilder(){
 *   public StoryReporter reporterFor(String storyPath, Format format){
 *       FilePrintStreamFactory factory = new FilePrintStreamFactory(new StoryLocation(storyPath, codeLocation));
 *       switch (format) {
 *           case TXT:
 *               factory.useConfiguration(new FileConfiguration("text"));
 *               Properties customPatterns = new Properties();
 *               customPatterns.setProperty("successful", "{0}(YEAH!!!)\n");
 *               return new TxtOutput(factory.createPrintStream(), customPatterns, keywords);
 *            default:
 *               return super.reporterFor(format);
 *   }
 * }
 * </pre>
 * 
 * </p>
 */
public class StoryReporterBuilder {

    public enum Format {
        CONSOLE, IDE_CONSOLE, TXT, HTML, XML, STATS
    }

    private List<Format> formats = new ArrayList<Format>();
    private String relativeDirectory = new FileConfiguration().getRelativeDirectory();
    private FilePathResolver pathResolver = new FileConfiguration().getPathResolver();
    private URL codeLocation = CodeLocations.codeLocationFromPath("target/classes");
    private Properties viewResources = FreemarkerViewGenerator.defaultViewProperties();
    private boolean reportFailureTrace = false;
    private Keywords keywords = new LocalizedKeywords();

    public File outputDirectory() {
        return filePrintStreamFactory("").outputDirectory();
    }

    public String relativeDirectory(){
        return relativeDirectory;
    }
    
    public FilePathResolver pathResolver(){
        return pathResolver;
    }
    
    public URL codeLocation() {
        return codeLocation;
    }

    public List<Format> formats() {
        return formats;
    }

    public List<String> formatNames(boolean toLowerCase) {
        List<String> names = new ArrayList<String>();
        for (Format format : formats) {
            String name = format.name();
            if (toLowerCase) {
                name = name.toLowerCase();
            }
            names.add(name);
        }
        return names;
    }

    public Keywords keywords() {
        return keywords;
    }

    public boolean reportFailureTrace() {
        return reportFailureTrace;
    }

    public Properties viewResources() {
        return viewResources;
    }

    public StoryReporterBuilder withRelativeDirectory(String relativeDirectory) {
        this.relativeDirectory = relativeDirectory;
        return this;
    }

    public StoryReporterBuilder withPathResolver(FilePathResolver pathResolver){
        this.pathResolver = pathResolver;
        return this;
    }

    public StoryReporterBuilder withCodeLocation(URL codeLocation) {
        this.codeLocation = codeLocation;
        return this;
    }

    public StoryReporterBuilder withDefaultFormats() {
        return withFormats(Format.STATS);
    }

    public StoryReporterBuilder withFormats(Format... formats) {
        this.formats.addAll(asList(formats));
        return this;
    }

    public StoryReporterBuilder withFailureTrace(boolean reportFailureTrace) {
        this.reportFailureTrace = reportFailureTrace;
        return this;
    }

    public StoryReporterBuilder withKeywords(Keywords keywords) {
        this.keywords = keywords;
        return this;
    }

    public StoryReporterBuilder withViewResources(Properties resources) {
        this.viewResources = resources;
        return this;
    }

    public StoryReporter build(String storyPath) {
        Map<Format, StoryReporter> delegates = new HashMap<Format, StoryReporter>();
        for (Format format : formats) {
            delegates.put(format, reporterFor(storyPath, format));
        }
        return new DelegatingStoryReporter(delegates.values());
    }

    public Map<String, StoryReporter> build(List<String> storyPaths) {
        Map<String, StoryReporter> reporters = new HashMap<String, StoryReporter>();
        for (String storyPath : storyPaths) {
            reporters.put(storyPath, build(storyPath));
        }
        return reporters;
    }

    public StoryReporter reporterFor(String storyPath, Format format) {
        FilePrintStreamFactory factory = filePrintStreamFactory(storyPath);
        switch (format) {
        case CONSOLE:
        default:
            return new ConsoleOutput(keywords).doReportFailureTrace(reportFailureTrace);
        case IDE_CONSOLE:
            return new IdeOnlyConsoleOutput(keywords).doReportFailureTrace(reportFailureTrace);
        case TXT:
            factory.useConfiguration(fileConfiguration("txt"));
            return new TxtOutput(factory.createPrintStream(), keywords).doReportFailureTrace(reportFailureTrace);
        case HTML:
            factory.useConfiguration(fileConfiguration("html"));
            return new HtmlOutput(factory.createPrintStream(), keywords).doReportFailureTrace(reportFailureTrace);
        case XML:
            factory.useConfiguration(fileConfiguration("xml"));
            return new XmlOutput(factory.createPrintStream(), keywords).doReportFailureTrace(reportFailureTrace);
        case STATS:
            factory.useConfiguration(fileConfiguration("stats"));
            return new PostStoryStatisticsCollector(factory.createPrintStream());
        }
    }

    protected FilePrintStreamFactory filePrintStreamFactory(String storyPath) {
        return new FilePrintStreamFactory(new StoryLocation(codeLocation, storyPath), fileConfiguration(""));
    }

    protected FileConfiguration fileConfiguration(String extension) {
        return new FileConfiguration(relativeDirectory, extension, pathResolver);
    }

}
