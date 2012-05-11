package org.jbehave.core.reporters;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.io.StoryLocation;
import org.jbehave.core.reporters.FilePrintStreamFactory.FileConfiguration;
import org.jbehave.core.reporters.FilePrintStreamFactory.FilePathResolver;

import static java.util.Arrays.asList;

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
 * StoryReporter reporter = new StoryReporterBuilder().withCodeLocation(CodeLocations.codeLocationFromClass(storyClass))
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
 * new StoryReporterBuilder().withCodeLocation(CodeLocations.codeLocationFromFile(new File(&quot;target/classes&quot;)))
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
 * <p>
 * If failure trace is reported, it is with the full stack trace. In some cases,
 * it's useful to have it compressed, eliminating unnecessary lines that are not
 * very informative:
 * 
 * <pre>
 * new StoryReporterBuilder().withFailureTraceCompression(true)
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
        CONSOLE(org.jbehave.core.reporters.Format.CONSOLE),
        IDE_CONSOLE(org.jbehave.core.reporters.Format.IDE_CONSOLE),
        TXT(org.jbehave.core.reporters.Format.TXT),
        HTML(org.jbehave.core.reporters.Format.HTML),
        XML(org.jbehave.core.reporters.Format.XML),
        STATS(org.jbehave.core.reporters.Format.STATS);

        private org.jbehave.core.reporters.Format realFormat;

        Format(org.jbehave.core.reporters.Format realFormat) {
            this.realFormat = realFormat;
        }

    }

    private List<org.jbehave.core.reporters.Format> formats = new ArrayList<org.jbehave.core.reporters.Format>();
    private String relativeDirectory = new FileConfiguration().getRelativeDirectory();
    private FilePathResolver pathResolver = new FileConfiguration().getPathResolver();
    private URL codeLocation = CodeLocations.codeLocationFromPath("target/classes");
    private Properties viewResources = new FreemarkerViewGenerator().defaultViewProperties();
    private boolean reportFailureTrace = false;
    private boolean compressFailureTrace = false;
    private Keywords keywords = new LocalizedKeywords();
    private CrossReference crossReference;
    private boolean multiThreading;

    public File outputDirectory() {
        return filePrintStreamFactory("").outputDirectory();
    }

    public String relativeDirectory() {
        return relativeDirectory;
    }

    public FilePathResolver pathResolver() {
        return pathResolver;
    }

    public URL codeLocation() {
        return codeLocation;
    }

    public List<org.jbehave.core.reporters.Format> formats() {
        return formats;
    }

    public List<String> formatNames(boolean toLowerCase) {
        Locale locale = Locale.getDefault();
        if (keywords instanceof LocalizedKeywords) {
            locale = ((LocalizedKeywords) keywords).getLocale();
        }
        List<String> names = new ArrayList<String>();
        for (org.jbehave.core.reporters.Format format : formats) {
            String name = format.name();
            if (toLowerCase) {
                name = name.toLowerCase(locale);
            }
            names.add(name);
        }
        return names;
    }

    public Keywords keywords() {
        return keywords;
    }

    public boolean multiThreading(){
        return multiThreading;
    }
    
    public boolean reportFailureTrace() {
        return reportFailureTrace;
    }

    public boolean compressFailureTrace() {
        return compressFailureTrace;
    }

    public Properties viewResources() {
        return viewResources;
    }

    public StoryReporterBuilder withRelativeDirectory(String relativeDirectory) {
        this.relativeDirectory = relativeDirectory;
        return this;
    }

    public StoryReporterBuilder withPathResolver(FilePathResolver pathResolver) {
        this.pathResolver = pathResolver;
        return this;
    }

    public StoryReporterBuilder withCodeLocation(URL codeLocation) {
        this.codeLocation = codeLocation;
        return this;
    }

    public CrossReference crossReference() {
        return crossReference;
    }

    public boolean hasCrossReference() {
        return crossReference != null;
    }

    public StoryReporterBuilder withCrossReference(CrossReference crossReference) {
        this.crossReference = crossReference;
        return this;
    }

    public StoryReporterBuilder withDefaultFormats() {
        return withFormats(Format.STATS);
    }

    /**
     * @deprecated Use {@link withFormats(org.jbehave.core.reporters.Format... formats)}
     */
    @Deprecated
    public StoryReporterBuilder withFormats(Format... formats) {
        List<org.jbehave.core.reporters.Format> formatz = new ArrayList<org.jbehave.core.reporters.Format>();
        for (Format format : formats) {
            formatz.add(format.realFormat);
        }
        this.formats.addAll(formatz);
        return this;
    }

    public StoryReporterBuilder withFormats(org.jbehave.core.reporters.Format... formats) {
        this.formats.addAll(asList(formats));
        return this;
    }

    public StoryReporterBuilder withReporters(StoryReporter... reporters) {
        for (StoryReporter reporter : reporters) {
            this.formats.add(new ProvidedFormat(reporter));
        }
        return this;
    }

    public StoryReporterBuilder withFailureTrace(boolean reportFailureTrace) {
        this.reportFailureTrace = reportFailureTrace;
        return this;
    }

    public StoryReporterBuilder withFailureTraceCompression(boolean compressFailureTrace) {
        this.compressFailureTrace = compressFailureTrace;
        return this;
    }

    public StoryReporterBuilder withKeywords(Keywords keywords) {
        this.keywords = keywords;
        return this;
    }

    public StoryReporterBuilder withMultiThreading(boolean multiThreading) {
        this.multiThreading = multiThreading;
        return this;
    }

    public StoryReporterBuilder withViewResources(Properties resources) {
        this.viewResources = resources;
        return this;
    }

    public StoryReporter build(String storyPath) {
        Map<org.jbehave.core.reporters.Format, StoryReporter> delegates = new HashMap<org.jbehave.core.reporters.Format, StoryReporter>();
        for (org.jbehave.core.reporters.Format format : formats) {
            delegates.put(format, reporterFor(storyPath, format));
        }

        DelegatingStoryReporter delegate = new DelegatingStoryReporter(delegates.values());
        return new ConcurrentStoryReporter(new NullStoryReporter(), delegate, multiThreading);
    }

    public Map<String, StoryReporter> build(List<String> storyPaths) {
        Map<String, StoryReporter> reporters = new HashMap<String, StoryReporter>();
        for (String storyPath : storyPaths) {
            reporters.put(storyPath, build(storyPath));
        }
        reporters.put("*", build("*"));
        return reporters;
    }

    public StoryReporter reporterFor(String storyPath, Format format) {
        return reporterFor(storyPath, format.realFormat);
    }

    public StoryReporter reporterFor(String storyPath, org.jbehave.core.reporters.Format format) {
        FilePrintStreamFactory factory = filePrintStreamFactory(storyPath);
        return format.createStoryReporter(factory, this);
    }

    protected FilePrintStreamFactory filePrintStreamFactory(String storyPath) {
        return new FilePrintStreamFactory(new StoryLocation(codeLocation, storyPath), fileConfiguration(""));
    }

    public FileConfiguration fileConfiguration(String extension) {
        return new FileConfiguration(relativeDirectory, extension, pathResolver);
    }

    /**
     * A Format that wraps a StoryReporter instance provided.
     */
    public static class ProvidedFormat extends org.jbehave.core.reporters.Format {

        private final StoryReporter reporter;

        public ProvidedFormat(StoryReporter reporter) {
            super(reporter.getClass().getSimpleName());
            this.reporter = reporter;
        }

        @Override
        public StoryReporter createStoryReporter(FilePrintStreamFactory factory,
                StoryReporterBuilder storyReporterBuilder) {
            return reporter;
        }
        
    }
}
