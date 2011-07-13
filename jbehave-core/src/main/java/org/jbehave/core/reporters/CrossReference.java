package org.jbehave.core.reporters;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jbehave.core.io.StoryLocation;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Narrative;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.StepPattern;
import org.jbehave.core.model.Story;
import org.jbehave.core.steps.NullStepMonitor;
import org.jbehave.core.steps.StepMonitor;
import org.jbehave.core.steps.StepType;

public class CrossReference extends Format {

    private final XStream XSTREAM_FOR_XML = new XStream();
    private final XStream XSTREAM_FOR_JSON = new XStream(new JsonHierarchicalStreamDriver());
    private ThreadLocal<Story> currentStory = new ThreadLocal<Story>();
    private ThreadLocal<Long> currentStoryStart = new ThreadLocal<Long>();
    private ThreadLocal<String> currentScenarioTitle = new ThreadLocal<String>();
    private List<StoryHolder> stories = new ArrayList<StoryHolder>();
    private Map<String, Long> times = new HashMap<String, Long>();
    private Map<String, StepMatch> stepMatches = new HashMap<String, StepMatch>();
    private StepMonitor stepMonitor = new XRefStepMonitor();
    private Set<String> failingStories = new HashSet<String>();
    private Set<String> stepsPerformed = new HashSet<String>();
    private boolean doJson = true;
    private boolean doXml = true;
    private boolean excludeStoriesWithNoExecutedScenarios = false;
    private boolean outputAfterEachStory = false;
    private Format threadSafeDelegateFormat;

    public CrossReference() {
        this("XREF");
    }

    public CrossReference(String name) {
        super(name);
        configure(XSTREAM_FOR_XML);
        configure(XSTREAM_FOR_JSON);
    }

    public CrossReference withJsonOnly() {
        doJson = true;
        doXml = false;
        return this;
    }

    public CrossReference withXmlOnly() {
        doJson = false;
        doXml = true;
        return this;
    }

    public CrossReference withOutputAfterEachStory(boolean outputAfterEachStory) {
        this.outputAfterEachStory = outputAfterEachStory;
        return this;
    }

    public CrossReference withThreadSafeDelegateFormat(Format format) {
        this.threadSafeDelegateFormat = format;
        return this;
    }

    public CrossReference excludingStoriesWithNoExecutedScenarios(boolean exclude) {
        this.excludeStoriesWithNoExecutedScenarios = exclude;
        return this;
    }

    public StepMonitor getStepMonitor() {
        return stepMonitor;
    }

    /**
     * Output to JSON and/or XML files.  Could be at the end of the suite, or per story
     * In the case of the latter, synchronization is needed as two stories (on two threads) could
     * be completing concurrently, and we need to guard against ConcurrentModificationException
     * @param storyReporterBuilder the reporter to use
     */
    public synchronized void outputToFiles(StoryReporterBuilder storyReporterBuilder) {
        XRefRoot root = createXRefRoot(storyReporterBuilder, stories, failingStories);
        root.addStepMatches(stepMatches);
        if (doXml) {
            outputFile(fileName("xml"), XSTREAM_FOR_XML, root, storyReporterBuilder);
        }
        if (doJson) {
            outputFile(fileName("json"), XSTREAM_FOR_JSON, root, storyReporterBuilder);
        }
    }

    protected String fileName(String extension) {
        return name().toLowerCase() + "." + extension;
    }

    protected final XRefRoot createXRefRoot(StoryReporterBuilder storyReporterBuilder, List<StoryHolder> stories,
            Set<String> failingStories) {
        XRefRoot xrefRoot = newXRefRoot();
        xrefRoot.metaFilter = getMetaFilter();
        xrefRoot.setExcludeStoriesWithNoExecutedScenarios(excludeStoriesWithNoExecutedScenarios);
        xrefRoot.processStories(stories, stepsPerformed,  times, storyReporterBuilder, failingStories);
        return xrefRoot;
    }

    protected XRefRoot newXRefRoot() {
        return new XRefRoot();
    }
    
    private void outputFile(String name, XStream xstream, XRefRoot root, StoryReporterBuilder storyReporterBuilder) {

        File outputDir = new File(storyReporterBuilder.outputDirectory(), "view");
        outputDir.mkdirs();
        try {
            Writer writer = makeWriter(new File(outputDir, name));
            writer.write(xstream.toXML(root));
            writer.flush();
            writer.close();
        } catch (IOException e) {
            throw new XrefOutputFailed(name, e);
        }

    }

    /** Override this if the metaFilter is important to
     * you in the Story Navigator output */
    public String getMetaFilter() {
        return "";
    }

    @SuppressWarnings("serial")
    public static class XrefOutputFailed extends RuntimeException {

        public XrefOutputFailed(String name, Throwable cause) {
            super(name, cause);
        }

    }

    protected Writer makeWriter(File file) throws IOException {
        return new FileWriter(file);
    }

    private void configure(XStream xstream) {
        xstream.setMode(XStream.NO_REFERENCES);
        aliasForXRefRoot(xstream);
        aliasForXRefStory(xstream);
        xstream.alias("stepMatch", StepMatch.class);
        xstream.alias("pattern", StepPattern.class);
        xstream.alias("use", StepUsage.class);
        xstream.omitField(ExamplesTable.class, "parameterConverters");
        xstream.omitField(ExamplesTable.class, "defaults");
    }

    protected void aliasForXRefStory(XStream xstream) {
        xstream.alias("story", XRefStory.class);
    }

    protected void aliasForXRefRoot(XStream xstream) {
        xstream.alias("xref", XRefRoot.class);
    }

    @Override
    public StoryReporter createStoryReporter(FilePrintStreamFactory factory, final StoryReporterBuilder storyReporterBuilder) {
        StoryReporter delegate;
        if (threadSafeDelegateFormat == null) {
            delegate = new NullStoryReporter();
        } else {
            delegate = threadSafeDelegateFormat.createStoryReporter(factory, storyReporterBuilder);
        }
        return new DelegatingStoryReporter(delegate) {

            @Override
            public void beforeStory(Story story, boolean givenStory) {
                synchronized (stories) {
                    stories.add(new StoryHolder(story));
                }
                currentStory.set(story);
                currentStoryStart.set(System.currentTimeMillis());
                super.beforeStory(story, givenStory);
            }

            @Override
            public void failed(String step, Throwable cause) {
                failingStories.add(currentStory.get().getPath());
                super.failed(step, cause);
            }

            @Override
            public void afterStory(boolean givenStory) {
                times.put(currentStory.get().getPath(), System.currentTimeMillis() - currentStoryStart.get());
                if (outputAfterEachStory) {
                    outputToFiles(storyReporterBuilder);
                }
                super.afterStory(givenStory);
            }

            @Override
            public void beforeScenario(String title) {
                currentScenarioTitle.set(title);
                super.beforeScenario(title);
            }
        };
    }

    private class XRefStepMonitor extends NullStepMonitor {
        @Override
        public void performing(String step, boolean dryRun) {
            super.performing(step, dryRun);
            stepsPerformed.add(currentStory.get().getPath());
        }

        public void stepMatchesPattern(String step, boolean matches, StepPattern pattern, Method method,
                Object stepsInstance) {
            Story story = currentStory.get();
            if (story == null) {
                throw new NullPointerException("story not setup for CrossReference");
            }

            if (matches) {
                String key = pattern.type() + pattern.annotated();
                StepMatch stepMatch = stepMatches.get(key);
                if (stepMatch == null) {
                    stepMatch = new StepMatch(pattern.type(), pattern.annotated(), pattern.resolved());
                    stepMatches.put(key, stepMatch);
                }
                // find canonical ref for same stepMatch
                stepMatch.usages.add(new StepUsage(story.getPath(), currentScenarioTitle.get(), step));
            }
            super.stepMatchesPattern(step, matches, pattern, method, stepsInstance);
        }
    }

    public static class XRefRoot {
        protected long whenMade = System.currentTimeMillis();
        protected String createdBy = createdBy();
        protected String metaFilter = "";

        private Set<String> meta = new HashSet<String>();
        private List<XRefStory> stories = new ArrayList<XRefStory>();
        private List<StepMatch> stepMatches = new ArrayList<StepMatch>();

        private transient boolean excludeStoriesWithNoExecutedScenarios;

        public XRefRoot() {
        }


        public void setExcludeStoriesWithNoExecutedScenarios(boolean exclude) {
            this.excludeStoriesWithNoExecutedScenarios = exclude;
        }

        protected String createdBy() {
            return "JBehave";
        }

        protected void processStories(List<StoryHolder> stories, Set<String> stepsPerformed, Map<String, Long> times, StoryReporterBuilder builder, Set<String> failures) {
            // Prevent Concurrent Modification Exception.
            synchronized (stories) {
                for (StoryHolder storyHolder : stories) {
                    Story story = storyHolder.story;
                    String path = story.getPath();
                    if (!path.equals("BeforeStories") && !path.equals("AfterStories")) {
                        if (someScenarios(story, stepsPerformed) || !excludeStoriesWithNoExecutedScenarios) {
                            XRefStory xRefStory = createXRefStory(builder, story, !failures.contains(path), this);
                            xRefStory.started = storyHolder.when;
                            xRefStory.duration = getTime(times, story);
                            this.stories.add(xRefStory);
                        }
                    }

                }
            }
        }

        protected Long getTime(Map<String, Long> times, Story story) {
            Long time = times.get(story.getPath());
            if (time == null) {
                return 0L;
            }
            return time;
        }

        protected boolean someScenarios(Story story, Set<String> stepsPerformed) {
            return stepsPerformed.contains(story.getPath());
        }

        /**
         * Ensure that XRefStory is instantiated completely, before secondary
         * methods are invoked (or overridden)
         */
        protected final XRefStory createXRefStory(StoryReporterBuilder storyReporterBuilder, Story story,
                boolean passed, XRefRoot root) {
            XRefStory xrefStory = createXRefStory(storyReporterBuilder, story, passed);
            xrefStory.processMetaTags(root);
            xrefStory.processScenarios();
            return xrefStory;
        }

        /**
         * Override this is you want to add fields to the JSON. Specifically,
         * create a subclass of XRefStory to return.
         * 
         * @param storyReporterBuilder the story reporter builder
         * @param story the story
         * @param passed the story passed (or failed)
         * @return An XRefStory
         */
        protected XRefStory createXRefStory(StoryReporterBuilder storyReporterBuilder, Story story, boolean passed) {
            return new XRefStory(story, storyReporterBuilder, passed);
        }

        protected void addStepMatches(Map<String, StepMatch> stepMatchMap) {
            for (String key : stepMatchMap.keySet()) {
                StepMatch stepMatch = stepMatchMap.get(key);
                stepMatches.add(stepMatch);
            }
        }
    }

    @SuppressWarnings("unused")
    public static class XRefStory {
        private transient Story story; // don't turn into JSON.
        private String description;
        private String narrative = "";
        private String name;
        private String path;
        private String html;
        private String meta = "";
        private String scenarios = "";
        private boolean passed;
        public long started;
        public long duration;

        public XRefStory(Story story, StoryReporterBuilder storyReporterBuilder, boolean passed) {
            this.story = story;
            Narrative narrative = story.getNarrative();
            if (!narrative.isEmpty()) {
                this.narrative = "In order to " + narrative.inOrderTo() + "\n" + "As a " + narrative.asA() + "\n"
                        + "I want to " + narrative.iWantTo() + "\n";
            }
            this.description = story.getDescription().asString();
            this.name = story.getName();
            this.path = story.getPath();
            this.passed = passed;
            this.html = storyReporterBuilder.pathResolver().resolveName(new StoryLocation(storyReporterBuilder.codeLocation(), story.getPath()),
                    "html");
        }

        protected void processScenarios() {
            for (Scenario scenario : story.getScenarios()) {
                String body = "Scenario:" + scenario.getTitle() + "\n";
                List<String> steps = scenario.getSteps();
                for (String step : steps) {
                    body = body + step + "\n";
                }
                scenarios = scenarios + body + "\n\n";
            }
        }

        protected void processMetaTags(XRefRoot root) {
            Meta storyMeta = story.getMeta();
            for (String next : storyMeta.getPropertyNames()) {
                String property = next + "=" + storyMeta.getProperty(next);
                addMetaProperty(property, root.meta);
                String newMeta = appendMetaProperty(property, this.meta);
                if (newMeta != null) {
                    this.meta = newMeta;
                }
            }
        }

        protected String appendMetaProperty(String property, String meta) {
            return meta + property + "\n";
        }

        protected void addMetaProperty(String property, Set<String> meta) {
            meta.add(property);
        }
    }

    @SuppressWarnings("unused")
    public static class StepUsage {
        private final String story;
        private final String scenario;
        private final String step;

        public StepUsage(String story, String scenario, String step) {
            this.story = story;
            this.scenario = scenario;
            this.step = step;
        }
    }

    public static class StepMatch {
        private final StepType type; // key
        private final String annotatedPattern; // key
        // these not in hashcode or equals()
        @SuppressWarnings("unused")
        private final String resolvedPattern;
        private final Set<StepUsage> usages = new HashSet<StepUsage>();

        public StepMatch(StepType type, String annotatedPattern, String resolvedPattern) {
            this.type = type;
            this.annotatedPattern = annotatedPattern;
            this.resolvedPattern = resolvedPattern;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            StepMatch stepMatch = (StepMatch) o;

            if (annotatedPattern != null ? !annotatedPattern.equals(stepMatch.annotatedPattern)
                    : stepMatch.annotatedPattern != null)
                return false;
            if (type != stepMatch.type)
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = type != null ? type.hashCode() : 0;
            result = 31 * result + (annotatedPattern != null ? annotatedPattern.hashCode() : 0);
            return result;
        }
    }

    private class StoryHolder {
        Story story;
        long when;

        private StoryHolder(Story story) {
            this.story = story;
            this.when = System.currentTimeMillis();
        }


    }
}
