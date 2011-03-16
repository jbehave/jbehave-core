package org.jbehave.core.reporters;

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
import org.jbehave.core.steps.StepMonitor;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;
import org.jbehave.core.steps.StepType;

public class CrossReference extends Format {

    private final XStream XSTREAM_FOR_XML = new XStream();
    private final XStream XSTREAM_FOR_JSON = new XStream(new JsonHierarchicalStreamDriver());
    private ThreadLocal<Story> currentStory = new ThreadLocal<Story>();
    private ThreadLocal<Long> currentStoryStart = new ThreadLocal<Long>();
    private ThreadLocal<String> currentScenarioTitle = new ThreadLocal<String>();
    private List<Story> stories = new ArrayList<Story>();
    private Map<Story, Long> times = new HashMap<Story, Long>();
    private Map<String, StepMatch> stepMatches = new HashMap<String, StepMatch>();
    private StepMonitor stepMonitor = new XRefStepMonitor();
    private Set<String> failingStories = new HashSet<String>();
    private boolean excludeStoriesWithNoExecutedScenarios;
    private Set<String> stepsPerformed = new HashSet<String>();
    private boolean doJson = true;
    private boolean doXml = true;
    private boolean outputAfterEachStory = false;
    private org.jbehave.core.reporters.Format threadSafeDelegateFormat;

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

    public CrossReference withThreadSafeDelegateFormat(org.jbehave.core.reporters.Format format) {
        this.threadSafeDelegateFormat = format;
        return this;
    }

    public CrossReference withXmlOnly() {
        doJson = false;
        doXml = true;
        return this;
    }

    public CrossReference writeCrossReferenceAfterEachStory() {
        outputAfterEachStory = true;
        return this;
    }


    public StepMonitor getStepMonitor() {
        return stepMonitor;
    }

    public void outputToFiles(StoryReporterBuilder storyReporterBuilder) {
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

    protected final XRefRoot createXRefRoot(StoryReporterBuilder storyReporterBuilder, List<Story> stories,
            Set<String> failingStories) {
        XRefRoot xrefRoot = newXRefRoot();
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

    public CrossReference excludeStoriesWithoutExecutedScenarios(boolean exclude) {
        this.excludeStoriesWithNoExecutedScenarios = exclude;
        return this;
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
                stories.add(story);
                currentStory.set(story);
                currentStoryStart.set(System.currentTimeMillis());
                super.beforeStory(story, givenStory);
            }

            @Override
            public void failed(String step, Throwable cause) {
                super.failed(step, cause);
                failingStories.add(currentStory.get().getPath());
                super.failed(step, cause);
            }

            @Override
            public void afterStory(boolean givenStory) {
                super.afterStory(givenStory);
                times.put(currentStory.get(), System.currentTimeMillis() - currentStoryStart.get());
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

    private class XRefStepMonitor extends StepMonitor.NULL {
        @Override
        public void performing(String step, boolean dryRun) {
            super.performing(step, dryRun);
            stepsPerformed.add(currentStory.get().getPath());
        }

        public void stepMatchesPattern(String step, boolean matches, StepPattern pattern, Method method,
                Object stepsInstance) {
            Story story = currentStory.get();

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
        protected long whenMade = currentTime();
        protected String createdBy = createdBy();

        private Set<String> meta = new HashSet<String>();
        private List<XRefStory> stories = new ArrayList<XRefStory>();
        private List<StepMatch> stepMatches = new ArrayList<StepMatch>();

        private transient boolean excludeStoriesWithNoExecutedScenarios;

        public void setExcludeStoriesWithNoExecutedScenarios(boolean exclude) {
            this.excludeStoriesWithNoExecutedScenarios = exclude;
        }

        protected long currentTime() {
            return System.currentTimeMillis();
        }

        protected String createdBy() {
            return "JBehave";
        }

        protected void processStories(List<Story> stories, Set<String> stepsPerformed, Map<Story, Long> times, StoryReporterBuilder builder, Set<String> failures) {
            for (Story story : stories) {
                if (someScenarios(story, stepsPerformed) || !excludeStoriesWithNoExecutedScenarios) {
                    XRefStory xRefStory = createXRefStory(builder, story, !failures.contains(story.getPath()), this);
                    xRefStory.duration = getTime(times, story);
                    this.stories.add(xRefStory);
                }
            }
        }

        protected Long getTime(Map<Story, Long> times, Story story) {
            return times.get(story);
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
            this.html = storyReporterBuilder.pathResolver().resolveName(new StoryLocation(null, story.getPath()),
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

}
