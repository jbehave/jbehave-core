package org.jbehave.core.reporters;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;
import org.apache.commons.collections.iterators.ArrayListIterator;
import org.jbehave.core.io.StoryLocation;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Narrative;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.steps.StepMonitor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.*;

public class CrossReference extends Format {

    private Story currentStory;
    private String currentScenarioTitle;
    private List<Story> stories = new ArrayList<Story>();
    private Map<String, StepMatch> stepMatches = new HashMap<String, StepMatch>();
    private StepMonitor stepMonitor = new XrefStepMonitor();
    private Set<String> failingStories = new HashSet<String>();

    public CrossReference() {
        this("XREF");
    }

    public CrossReference(String name) {
        super(name);
    }

    public StepMonitor getStepMonitor() {
        return stepMonitor;
    }

    public void outputToFiles(StoryReporterBuilder storyReporterBuilder) {
        XrefRoot root = createXRefRootNode(storyReporterBuilder, stories, failingStories);
        root.addStepMatches(stepMatches);
        outputFile("xref.xml", new XStream(), root, storyReporterBuilder);
        outputFile("xref.json", new XStream(new JsonHierarchicalStreamDriver()), root, storyReporterBuilder);
    }

    protected final XrefRoot createXRefRootNode(StoryReporterBuilder storyReporterBuilder, List<Story> stories, Set<String> failingStories) {
        XrefRoot xrefRoot = makeXRefRootNode();
        xrefRoot.processStories(stories, storyReporterBuilder, failingStories);
        return xrefRoot;
    }

    protected XrefRoot makeXRefRootNode() {
        return new XrefRoot();
    }

    private void outputFile(String name, XStream xstream, XrefRoot root, StoryReporterBuilder storyReporterBuilder){
        File outputDir = new File(storyReporterBuilder.outputDirectory(), "view");
        outputDir.mkdirs();
        try {
            Writer writer = makeWriter(new File(outputDir, name));
            writer.write(configure(xstream).toXML(root));
            writer.flush();
            writer.close();
        } catch (IOException e) {
            throw new XrefOutputFailed(name, e);
        }

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

    private XStream configure(XStream xstream) {
        xstream.setMode(XStream.NO_REFERENCES);
        xStreamAliasForXRefRoot(xstream);
        xStreamAliasForXRefStory(xstream);
        xstream.alias("stepMatch", StepMatch.class);
        xstream.alias("pattern", StepMonitor.Pattern.class);
        xstream.alias("use", StepMatchDetail.class);
        xstream.omitField(ExamplesTable.class, "parameterConverters");
        xstream.omitField(ExamplesTable.class, "defaults");
        return xstream;
    }

    protected void xStreamAliasForXRefStory(XStream xstream) {
        xstream.alias("story", XrefStory.class);
    }

    protected void xStreamAliasForXRefRoot(XStream xstream) {
        xstream.alias("xref", XrefRoot.class);
    }

    @Override
    public StoryReporter createStoryReporter(FilePrintStreamFactory factory, StoryReporterBuilder storyReporterBuilder) {
        return new NullStoryReporter() {

            @Override
            public void beforeStory(Story story, boolean givenStory) {
                stories.add(story);
                currentStory = story;
            }

            @Override
            public void failed(String step, Throwable cause) {
                super.failed(step, cause);
                failingStories.add(currentStory.getPath());
            }

            @Override
            public void beforeScenario(String title) {
                currentScenarioTitle = title;
            }
        };
    }

    private class XrefStepMonitor extends StepMonitor.NULL {
        public void stepMatchesPattern(String step, boolean matches, Pattern pattern, Method method, Object stepsInstance) {
            if (matches) {
                String key = pattern.getPseudoPattern();
                StepMatch val = stepMatches.get(key);
                if (val == null) {
                    val = new StepMatch(key, pattern.getPattern());
                    stepMatches.put(key, val);
                }
                // find canonical ref for same stepMatch
                val.usages.add(new StepMatchDetail(currentStory.getPath(), currentScenarioTitle, step));
            }
            super.stepMatchesPattern(step, matches, pattern, method, stepsInstance);
        }
    }

    @SuppressWarnings("unused")
    public static class XrefRoot {
        private Set<String> meta = new HashSet<String>();
        private List<XrefStory> stories = new ArrayList<XrefStory>();
        private List<StepMatch> stepMatches = new ArrayList<StepMatch>();

        public XrefRoot() {
        }

        protected void processStories(List<Story> stories, StoryReporterBuilder storyReporterBuilder, Set<String> failures) {
            for (Story story : stories) {
                this.stories.add(createXRefStoryNode(storyReporterBuilder, story, !failures.contains(story.getPath()), this));
            }
        }

        /*
         * Ensure that XrefStory is instantiated completely, before secondary methods are invoked (or overridden)
         */
        protected final XrefStory createXRefStoryNode(StoryReporterBuilder storyReporterBuilder, Story story, boolean passed, XrefRoot root) {
            XrefStory xrefStory = makeXRefStoryNode(storyReporterBuilder, story, passed);
            xrefStory.processMetaTags(root);
            xrefStory.processScenarios();
            return xrefStory;
        }

        /**
         * Override this is you want to add fields to the JSON.  Specifically, create a subclass of XrefStory to return.
         * @param storyReporterBuilder the story reporter builder
         * @param story the story
         * @param passed the story passed (or failed)
         * @return
         */
        protected XrefStory makeXRefStoryNode(StoryReporterBuilder storyReporterBuilder, Story story, boolean passed) {
            return new XrefStory(story, storyReporterBuilder, passed);
        }

        protected void addStepMatches(Map<String, StepMatch> stepMatchMap) {
            for (String key : stepMatchMap.keySet()) {
                StepMatch stepMatch = stepMatchMap.get(key);
                stepMatches.add(stepMatch);
            }
        }
    }

    @SuppressWarnings("unused")
    public static class XrefStory {
        private transient Story story; // don't turn into JSON.
        private String description;
        private String narrative = "";
        private String name;
        private String path;
        private String html;
        private String meta = "";
        private String scenarios = "";
        private boolean passed;

        public XrefStory(Story story, StoryReporterBuilder storyReporterBuilder, boolean passed) {
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
            this.html = storyReporterBuilder.pathResolver().resolveName(new StoryLocation(null, story.getPath()), "html");
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

        protected void processMetaTags(XrefRoot root) {
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
    public static class StepMatchDetail {
        private final String story;
        private final String scenario;
        private final String step;

        public StepMatchDetail(String story, String scenario, String step) {
            this.story = story;
            this.scenario = scenario;
            this.step = step;
        }
    }

    @SuppressWarnings("unused")
    public static class StepMatch {
        private final String pseudoPattern;
        private final String regexPattern;
        // not in hashcode or equals()
        private final Set<StepMatchDetail> usages = new HashSet<StepMatchDetail>();

        public StepMatch(String pseudoPattern, String regexPattern) {
            this.pseudoPattern = pseudoPattern;
            this.regexPattern = regexPattern;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            StepMatch stepMatch = (StepMatch) o;

            if (pseudoPattern != null ? !pseudoPattern.equals(stepMatch.pseudoPattern) : stepMatch.pseudoPattern != null)
                return false;
            if (regexPattern != null ? !regexPattern.equals(stepMatch.regexPattern) : stepMatch.regexPattern != null)
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = pseudoPattern != null ? pseudoPattern.hashCode() : 0;
            result = 31 * result + (regexPattern != null ? regexPattern.hashCode() : 0);
            return result;
        }
    }

}
