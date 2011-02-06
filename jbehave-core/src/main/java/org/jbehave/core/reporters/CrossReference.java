package org.jbehave.core.reporters;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jbehave.core.io.StoryLocation;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.Narrative;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.steps.StepMonitor;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;

public class CrossReference extends Format {

    private String currentStoryPath;
    private String currentScenarioTitle;
    private List<Story> stories = new ArrayList<Story>();
    private List<StepMatch> stepMatches = new ArrayList<StepMatch>();
    private StepMonitor stepMonitor = new XrefStepMonitor();

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
        XrefRoot root = new XrefRoot(stepMatches, stories, storyReporterBuilder);
        outputFile("xref.xml", new XStream(), root, storyReporterBuilder);
        outputFile("xref.json", new XStream(new JsonHierarchicalStreamDriver()), root, storyReporterBuilder);
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
        xstream.alias("xref", XrefRoot.class);
        xstream.alias("story", XrefStory.class);
        xstream.alias("stepMatch", StepMatch.class);
        xstream.omitField(ExamplesTable.class, "parameterConverters");
        xstream.omitField(ExamplesTable.class, "defaults");
        return xstream;
    }

    @Override
    public StoryReporter createStoryReporter(FilePrintStreamFactory factory, StoryReporterBuilder storyReporterBuilder) {
        return new NullStoryReporter() {

            @Override
            public void beforeStory(Story story, boolean givenStory) {
                stories.add(story);
                currentStoryPath = story.getPath();
            }

            @Override
            public void beforeScenario(String title) {
                currentScenarioTitle = title;
            }
        };
    }

    private class XrefStepMonitor extends StepMonitor.NULL {
        public void stepMatchesPattern(String step, boolean matches, String pattern, Method method, Object stepsInstance) {
            if (matches) {
                stepMatches.add(new StepMatch(currentStoryPath, currentScenarioTitle, step, pattern));
            }
            super.stepMatchesPattern(step, matches, pattern, method, stepsInstance);
        }
    };

    @SuppressWarnings("unused")
    private static class XrefRoot {
        private Set<String> meta = new HashSet<String>();
        private List<XrefStory> stories = new ArrayList<XrefStory>();
        private List<StepMatch> stepMatches = new ArrayList<StepMatch>();

        public XrefRoot(List<StepMatch> stepMatches, List<Story> stories, StoryReporterBuilder storyReporterBuilder) {
            this.stepMatches = stepMatches;
            for (Story story : stories) {
                this.stories.add(new XrefStory(story, this, storyReporterBuilder));
            }
        }
    }

    @SuppressWarnings("unused")
    private static class XrefStory {
        private String description;
        private String narrative = "";
        private String name;
        private String path;
        private String meta = "";
        private String scenarios = "";

        public XrefStory(Story story, XrefRoot root, StoryReporterBuilder storyReporterBuilder) {
            Narrative narrative = story.getNarrative();
            if (!narrative.isEmpty()) {
                this.narrative = "In order to " + narrative.inOrderTo() + "\n" + "As a " + narrative.asA() + "\n"
                        + "I want to " + narrative.iWantTo() + "\n";
            }
            this.description = story.getDescription().asString();
            this.name = story.getName();
            this.path = storyReporterBuilder.pathResolver().resolveName(new StoryLocation(null, story.getPath()), "html");
            for (String next : story.getMeta().getPropertyNames()) {
                String property = meta + next + "=" + story.getMeta().getProperty(next);
                root.meta.add(property);
                meta = property + "\n";

            }
            for (Scenario scenario : story.getScenarios()) {
                String body = "Scenario:" + scenario.getTitle() + "\n";
                List<String> steps = scenario.getSteps();
                for (String step : steps) {
                    body = body + step + "\n";
                }
                scenarios = scenarios + body + "\n\n";
            }
        }
    }

    @SuppressWarnings("unused")
    private static class StepMatch {
        private final String storyPath;
        private final String scenarioTitle;
        private final String step;
        private final String pattern;

        public StepMatch(String storyPath, String scenarioTitle, String step, String pattern) {
            this.storyPath = storyPath;
            this.scenarioTitle = scenarioTitle;
            this.step = step;
            this.pattern = pattern;
        }
    }

}
